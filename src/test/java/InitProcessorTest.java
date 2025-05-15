import Interfaces.ArgumentParser;
import Interfaces.LLMApiService;
import Interfaces.LoggerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InitProcessorTest {
    LoggerService mockLogger;
    List<String> logs = new LinkedList<>();

    @BeforeEach
    void setUp() {
        mockLogger = new LoggerService(){
            @Override
            public void log(String message) {
                logs.add(message);
            }

            @Override
            public void error(String message) {
                logs.add(message);
            }

            @Override
            public void debug(String message) {
                logs.add(message);
            }
        };
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getSingleJavaFile() throws IOException {
        Path tempFile = Files.createTempFile("TestFile", ".java");
        String[] args = {"-f", tempFile.toString()};
        InitProcessor initProcessor = new InitProcessor(args,mockLogger);

        List<File> javaFiles = initProcessor.getJavaFiles();

        assertEquals(1, javaFiles.size());
        assertTrue(javaFiles.getFirst().getName().endsWith(".java"));

        Files.deleteIfExists(tempFile);
    }

    @Test
    void getJavaFilesFromDir() throws IOException {
        Path tempDir = Files.createTempDirectory("tempDir");
        Path file1 = Files.createFile(tempDir.resolve("File1.java"));
        Path file2 = Files.createFile(tempDir.resolve("File2.java"));
        String[] args = {"-dir", tempDir.toString()};
        InitProcessor initProcessor = new InitProcessor(args, mockLogger);

        List<File> javaFiles = initProcessor.getJavaFiles();

        assertEquals(2, javaFiles.size());
        assertTrue(javaFiles.get(0).getName().endsWith(".java"));
        assertTrue(javaFiles.get(1).getName().endsWith(".java"));

        Files.deleteIfExists(file1);
        Files.deleteIfExists(file2);
        Files.deleteIfExists(tempDir);
    }

    @Test
    void getJavaFilesFromEmptyDir() throws IOException {
        Path tempDir = Files.createTempDirectory("emptyDir");
        String[] args = {"-dir", tempDir.toString()};
        InitProcessor initProcessor = new InitProcessor(args, mockLogger);

        List<File> javaFiles = initProcessor.getJavaFiles();

        assertTrue(javaFiles.isEmpty());

        Files.deleteIfExists(tempDir);
    }

    @Test
    void getNonExistentFile() {
        String[] args = {"-f", "/test/path/to/non/existent/file.java"};
        InitProcessor initProcessor = new InitProcessor(args, mockLogger);

        List<File> javaFiles = initProcessor.getJavaFiles();

        assertTrue(javaFiles.isEmpty());

        assertTrue(logs.stream().anyMatch(log -> log.contains("File does not exist or is not a valid file: /test/path/to/non/existent/file.java")));

    }

    @Test
    void getNonJavaFile() throws IOException {
        Path tempFile = Files.createTempFile("TestFile", ".txt");
        String[] args = {"-f", tempFile.toString()};
        InitProcessor initProcessor = new InitProcessor(args, mockLogger);

        List<File> javaFiles = initProcessor.getJavaFiles();

        assertTrue(javaFiles.isEmpty());
        assertTrue(logs.stream().anyMatch(log -> log.contains("File does not exist or is not a valid file: " + tempFile.toAbsolutePath())));

        Files.deleteIfExists(tempFile);
    }

    @Test
    void getNoArguments() {
        String[] args = {};
        InitProcessor initProcessor = new InitProcessor(args, mockLogger);

        List<File> javaFiles = initProcessor.getJavaFiles();
        assertTrue(javaFiles.isEmpty());
        assertTrue(logs.stream().anyMatch(log -> log.contains("Please specify a file or directory.")));
    }

    @Test
    void getJavaFilesFromInvalidDir() {
        String[] args = {"-dir", "/path/to/nonexistent/dir"};
        InitProcessor initProcessor = new InitProcessor(args, mockLogger);

        List<File> javaFiles = initProcessor.getJavaFiles();

        assertTrue(javaFiles.isEmpty());
        assertTrue(logs.stream().anyMatch(log -> log.contains("Invalid directory path.")));
    }

    @Test
    void getModelDefault() {
        String[] args = {};
        InitProcessor processor = new InitProcessor(args, mockLogger);

        String model = processor.getModel();

        assertEquals("gpt4omini", model);
    }

    @Test
    void getModelLowerCase() {
        String[] args = {"-dir", "/path/to/dir", "-model", "gpt4"};
        InitProcessor processor = new InitProcessor(args, mockLogger);

        String model = processor.getModel();

        assertEquals("gpt4", model);
    }

    @Test
    void getModelEmpty() {
        String[] args = {"-model"};
        InitProcessor processor = new InitProcessor(args, mockLogger);

        String model = processor.getModel();

        assertEquals("gpt4omini", model);
    }

    @Test
    void createAPIService() {
        String[] args = {"-dir", "/path/to/dir"};
        InitProcessor processor = new InitProcessor(args, mockLogger);

        LLMApiService openAIService = processor.createAPIService();

        assertNotNull(openAIService);
    }

    @Test
    void getOutputPath() {
        try {
            Path tempFile = Files.createTempFile("TestFile", ".java");
            String[] args = {"-f", tempFile.toString()};
            InitProcessor initProcessor = new InitProcessor(args,mockLogger);

            List<File> javaFiles = initProcessor.getJavaFiles(); //to set private variable 'OutputPath'
            Path outputPath = initProcessor.getOutputPath();

            assertEquals(tempFile.getParent().toAbsolutePath().toString(), outputPath.toString());

            Files.deleteIfExists(tempFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}