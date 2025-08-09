package com.javarush.khmelov.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.khmelov.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileUserRepository implements UserRepository {

    private static final Logger log = LoggerFactory.getLogger(FileUserRepository.class);

    private final Path filePath;
    private final ObjectMapper mapper = new ObjectMapper();
    private Map<String, User> users = new HashMap<>();

    public FileUserRepository(Path filePath) {
        this.filePath = filePath;
        load();
    }

    private void load() {
        try {
            if (Files.exists(filePath)) {
                users = mapper.readValue(filePath.toFile(), new TypeReference<>() {});
                log.info("Загружено {} пользователей из {}", users.size(), filePath);
            } else {
                log.warn("Файл {} не найден. Будет создан новый при сохранении.", filePath);
            }
        } catch (IOException e) {
            log.error("Ошибка при загрузке пользователей из файла {}", filePath, e);
            users = new HashMap<>();
        }
    }

    private void saveToFile() {
        try {
            Files.createDirectories(filePath.getParent());
            mapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), users);
            log.debug("Сохранено {} пользователей в {}", users.size(), filePath);
        } catch (IOException e) {
            log.error("Ошибка при сохранении пользователей в файл {}", filePath, e);
        }
    }

    @Override
    public void save(User user) {
        users.put(user.getUsername(), user);
        saveToFile();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }
}
