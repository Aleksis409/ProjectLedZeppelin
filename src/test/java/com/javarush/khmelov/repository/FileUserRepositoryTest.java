package com.javarush.khmelov.repository;

import com.javarush.khmelov.entity.User;
import org.junit.jupiter.api.*;

import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FileUserRepositoryTest {

    private Path tempFile;
    private FileUserRepository repository;

    @BeforeEach
    void setup() throws Exception {
        tempFile = Files.createTempFile("users", ".json");
        repository = new FileUserRepository(tempFile);
    }

    @AfterEach
    void cleanup() throws Exception {
        Files.deleteIfExists(tempFile);
    }

    @Test
    void saveAndFindUserSuccessTestOk() {
        User user = new User("testuser", "pass");
        repository.save(user);

        Optional<User> loaded = repository.findByUsername("testuser");
        assertTrue(loaded.isPresent());
        assertEquals("testuser", loaded.get().getUsername());
        assertEquals("pass", loaded.get().getPassword());
    }

    @Test
    void findByUsernameNotFoundReturnsEmptyTestOk() {
        Optional<User> user = repository.findByUsername("unknown");
        assertTrue(user.isEmpty());
    }

    @Test
    void loadWhenFileDoesNotExistTestOk() throws Exception {
        Path nonExistent = Path.of("nonexistentfile.json");
        FileUserRepository repo = new FileUserRepository(nonExistent);

        assertTrue(repo.findByUsername("any").isEmpty());
    }
}
