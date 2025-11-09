package com.javarush.khmelov.utils;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.resource.InputStreamList;
import liquibase.resource.ResourceAccessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportStepsChangeTest {

    @Mock
    private ResourceAccessor resourceAccessor;

    @Mock
    private Database database;

    @Test
    void executeSuccessfullyImportsStepsWithOptions() throws Exception {
        ImportStepsChange change = new ImportStepsChange();
        change.setFile("steps.json");
        change.setFileOpener(resourceAccessor);

        String json = """
                [
                  {"id":1,"text":"Step 1","finish":false,"win":false,"options":{"Option A":2,"Option B":3}}
                ]
                """;

        InputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        InputStreamList inputStreamList = mock(InputStreamList.class);
        when(inputStreamList.iterator()).thenReturn(Collections.singletonList(inputStream).iterator());
        when(resourceAccessor.openStreams("", "steps.json")).thenReturn(inputStreamList);

        JdbcConnection mockJdbc = mock(JdbcConnection.class);
        Connection mockConn = mock(Connection.class);
        PreparedStatement stepStmt = mock(PreparedStatement.class);
        PreparedStatement optStmt = mock(PreparedStatement.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(stepStmt).thenReturn(optStmt);
        when(mockJdbc.getUnderlyingConnection()).thenReturn(mockConn);
        when(database.getConnection()).thenReturn(mockJdbc);

        assertDoesNotThrow(() -> change.execute(database));

        verify(stepStmt).addBatch();
        verify(optStmt, times(2)).addBatch();
        verify(stepStmt).executeBatch();
        verify(optStmt).executeBatch();
    }

    @Test
    void executeSuccessfullyImportsStepsWithoutOptions() throws Exception {
        ImportStepsChange change = new ImportStepsChange();
        change.setFile("steps.json");
        change.setFileOpener(resourceAccessor);

        String json = """
                [
                  {"id":1,"text":"Step 1","finish":true,"win":true}
                ]
                """;

        InputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        InputStreamList inputStreamList = mock(InputStreamList.class);
        when(inputStreamList.iterator()).thenReturn(Collections.singletonList(inputStream).iterator());
        when(resourceAccessor.openStreams("", "steps.json")).thenReturn(inputStreamList);

        JdbcConnection mockJdbc = mock(JdbcConnection.class);
        Connection mockConn = mock(Connection.class);
        PreparedStatement stepStmt = mock(PreparedStatement.class);
        PreparedStatement optStmt = mock(PreparedStatement.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(stepStmt).thenReturn(optStmt);
        when(mockJdbc.getUnderlyingConnection()).thenReturn(mockConn);
        when(database.getConnection()).thenReturn(mockJdbc);

        assertDoesNotThrow(() -> change.execute(database));

        verify(stepStmt).addBatch();
        verify(optStmt, never()).addBatch(); // options missing
        verify(stepStmt).executeBatch();
        verify(optStmt).executeBatch();
    }

    @Test
    void executeThrowsWhenFileNotFound() throws Exception {
        ImportStepsChange change = new ImportStepsChange();
        change.setFile("steps.json");
        change.setFileOpener(resourceAccessor);

        JdbcConnection mockJdbc = mock(JdbcConnection.class);
        when(database.getConnection()).thenReturn(mockJdbc);

        when(resourceAccessor.openStreams("", "steps.json")).thenReturn(null);

        CustomChangeException ex = assertThrows(CustomChangeException.class,
                () -> change.execute(database));

        assertNotNull(ex.getCause());
        assertTrue(ex.getCause().getMessage().contains("File not found") ||
                ex.getCause().getMessage().contains("Не удалось найти файл"));
    }

    @Test
    void executeThrowsWhenDatabaseConnectionIsNotJdbc() {
        ImportStepsChange change = new ImportStepsChange();
        change.setFile("steps.json");
        change.setFileOpener(resourceAccessor);

        DatabaseConnection invalidConn = mock(DatabaseConnection.class);
        Database mockDb = mock(Database.class);
        when(mockDb.getConnection()).thenReturn(invalidConn);

        CustomChangeException ex = assertThrows(CustomChangeException.class,
                () -> change.execute(mockDb));

        assertNotNull(ex.getCause());
        assertTrue(ex.getCause().getMessage().contains("JdbcConnection") ||
                ex.getCause().getMessage().contains("не является JdbcConnection"));
    }

    @Test
    void getConfirmationMessageReturnsCorrectMessage() {
        ImportStepsChange change = new ImportStepsChange();
        change.setFile("my_steps.json");
        assertEquals("Steps imported successfully from my_steps.json",
                change.getConfirmationMessage());
    }

    @Test
    void validateReturnsEmptyErrors() {
        ImportStepsChange change = new ImportStepsChange();
        assertNotNull(change.validate(database));
        assertEquals(0, change.validate(database).getErrorMessages().size());
    }

    @Test
    void setUpDoesNotThrow() {
        ImportStepsChange change = new ImportStepsChange();
        assertDoesNotThrow(change::setUp);
    }
}
