package com.javarush.khmelov.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.InputStreamList;
import liquibase.resource.ResourceAccessor;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.DatabaseConnection;
import lombok.Setter;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Custom Liquibase task to import steps and step options from a JSON file
 * into the database tables 'step' and 'step_option'.
 */
public class ImportStepsChange implements CustomTaskChange {

    private ResourceAccessor resourceAccessor;

    @Setter
    private String file;

    /**
     * Executes the import of steps from the specified JSON file into the database.
     * <p>
     * Reads the JSON file, parses each step and its options, and inserts them
     * into the database using JDBC batch statements.
     *
     * @param database the Liquibase Database object
     * @throws CustomChangeException if an error occurs during import
     */
    @Override
    public void execute(Database database) throws CustomChangeException {
        try {
            DatabaseConnection dbConn = database.getConnection();
            Connection connection;
            if (dbConn instanceof JdbcConnection jdbcConn) {
                connection = jdbcConn.getUnderlyingConnection();
            } else {
                throw new CustomChangeException("DatabaseConnection is not a JdbcConnection");
            }

            // Open InputStream using ResourceAccessor (deprecated openStreams)
            InputStreamList streams = resourceAccessor.openStreams("", file);
            if (streams == null || !streams.iterator().hasNext()) {
                throw new CustomChangeException("File not found: " + file);
            }
            InputStream input = streams.iterator().next();

            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> steps = mapper.readValue(input, new TypeReference<>() {
            });

            String insertStepSQL = "INSERT INTO quest1.step (id, text, finish, win) VALUES (?, ?, ?, ?)";
            String insertOptionSQL = "INSERT INTO quest1.step_option (step_id, option_text, next_step_id) VALUES (?, ?, ?)";

            try (PreparedStatement stepStmt = connection.prepareStatement(insertStepSQL);
                 PreparedStatement optStmt = connection.prepareStatement(insertOptionSQL)) {

                for (Map<String, Object> step : steps) {
                    int id = ((Number) step.get("id")).intValue();
                    String text = (String) step.get("text");
                    boolean finish = (boolean) step.getOrDefault("finish", false);
                    boolean win = (boolean) step.getOrDefault("win", false);

                    stepStmt.setInt(1, id);
                    stepStmt.setString(2, text);
                    stepStmt.setBoolean(3, finish);
                    stepStmt.setBoolean(4, win);
                    stepStmt.addBatch();

                    Object optionsObj = step.get("options");
                    if (optionsObj instanceof Map<?, ?> rawMap) {
                        Map<String, Integer> options = rawMap.entrySet().stream()
                                .filter(e -> e.getKey() instanceof String && e.getValue() instanceof Number)
                                .collect(Collectors.toMap(
                                        e -> (String) e.getKey(),
                                        e -> ((Number) e.getValue()).intValue()
                                ));

                        for (Map.Entry<String, Integer> opt : options.entrySet()) {
                            optStmt.setInt(1, id);
                            optStmt.setString(2, opt.getKey());
                            optStmt.setInt(3, opt.getValue());
                            optStmt.addBatch();
                        }
                    }
                }

                stepStmt.executeBatch();
                optStmt.executeBatch();
            }

        } catch (Exception e) {
            throw new CustomChangeException("Error importing steps.json", e);
        }
    }

    /**
     * Returns a confirmation message after the change has been executed.
     *
     * @return the confirmation message
     */
    @Override
    public String getConfirmationMessage() {
        return "Steps imported successfully from " + file;
    }

    /**
     * Performs setup before executing the change.
     * This implementation does nothing.
     *
     * @throws SetupException if setup fails
     */
    @Override
    public void setUp() throws SetupException {
        // no setup required
    }

    /**
     * Sets the ResourceAccessor for accessing files.
     *
     * @param resourceAccessor the ResourceAccessor to use
     */
    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
    }

    /**
     * Validates the change before execution.
     *
     * @param database the Liquibase Database object
     * @return a ValidationErrors object containing validation results
     */
    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }
}



