package com.javarush.khmelov.config;

import com.javarush.khmelov.repository.FileUserRepository;
import com.javarush.khmelov.repository.UserRepository;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@WebListener
public class ContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String dataDirPath = sce.getServletContext().getRealPath("/data");
        if (dataDirPath == null) {
            throw new IllegalStateException("Cannot resolve real path for /data");
        }
        try {
            Path dataDir = Paths.get(dataDirPath);
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
            Path usersFile = dataDir.resolve("users.json");

            UserRepository userRepository = new FileUserRepository(usersFile);
            sce.getServletContext().setAttribute("userRepository", userRepository);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize user repository", e);
        }
    }
}
