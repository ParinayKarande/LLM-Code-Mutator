import Interfaces.LLMApiService;
import Interfaces.LoggerService;
import constants.Headers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileProcessorTest {

    LoggerService mockLogger;
    List<String> logs;
    InitProcessor initProcessor;
    LLMApiService mockOpenAIService;
    List<File> files;
    Path tempFile;
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        logs = new LinkedList<>();
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
        tempDir = Files.createTempDirectory("TempDir");
        tempFile = Files.createTempFile(tempDir,"TestFile", ".java");
        String[] args = {"-f", tempFile.toString()};
        initProcessor = new InitProcessor(args,mockLogger);
        files = initProcessor.getJavaFiles();
        mockOpenAIService = new LLMApiService() {
            @Override
            public String askOpenAI(String javaCode) {
                return  "```java Mutated Code ```";
            }
        };
    }

    @AfterEach
    void tearDown() {
        try {
            if (Files.exists(tempDir)) {
                if (Files.isDirectory(tempDir)) {
                    Files.walk(tempDir)
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                } else {
                    Files.deleteIfExists(tempDir);
                }
                mockLogger.log(!Files.exists(tempDir) ? "Deleted temp directory at " + tempDir.toAbsolutePath() : "Failed to delete temp directory at " + tempDir.toAbsolutePath());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void processSingleJavaFile() {
        FileProcessor fileProcessor = new FileProcessor(mockLogger, mockOpenAIService);
        fileProcessor.processJavaFiles(files,initProcessor.getOutputPath());

        assertTrue(logs.stream().anyMatch(log -> log.contains("0 file(s) remaining...")));
    }

    @Test
    void processSingleJavaFileWithDataNoResponse() throws IOException {
        Files.writeString(tempFile, "public class TempFile { public static void main(String[] args) {} }");
        mockOpenAIService = new LLMApiService() {
            @Override
            public String askOpenAI(String javaCode) {
                return  "";
            }
        };
        FileProcessor fileProcessor = new FileProcessor(mockLogger, mockOpenAIService);
        fileProcessor.processJavaFiles(files,initProcessor.getOutputPath());


        assertTrue(logs.stream().anyMatch(log -> log.contains("No Mutation response returned for " + tempFile.getFileName())));
    }

    @Test
    void processSingleJavaFileWithDataNoExtractedJavaCode() throws IOException {
        Files.writeString(tempFile, "public class TempFile { public static void main(String[] args) {} }");
        mockOpenAIService = new LLMApiService() {
            @Override
            public String askOpenAI(String javaCode) {
                return  "No Java Code to Extract";
            }
        };
        FileProcessor fileProcessor = new FileProcessor(mockLogger, mockOpenAIService);
        fileProcessor.processJavaFiles(files,initProcessor.getOutputPath());


        assertTrue(logs.stream().anyMatch(log -> log.contains("Java code block not found in response. LLM Mutation for " + tempFile.getFileName() + " failed...")));
    }

    @Test
    void processSingleJavaFileWithData() throws IOException {
        Files.writeString(tempFile, "public class TempFile { public static void main(String[] args) {} }");
        FileProcessor fileProcessor = new FileProcessor(mockLogger, mockOpenAIService);
        fileProcessor.processJavaFiles(files,initProcessor.getOutputPath());

        assertTrue(logs.stream().anyMatch(log -> log.contains("Java code extracted from " + tempFile.getFileName())));
        assertTrue(logs.stream().anyMatch(log -> log.contains("CSV file created successfully!")));
    }

    @Test
    void processNullPath(){
        FileProcessor fileProcessor = new FileProcessor(mockLogger, mockOpenAIService);
        try{
            fileProcessor.processJavaFiles(files,null);
        }
        catch (Exception e){
            assertEquals(NullPointerException.class, e.getClass());
        }
    }

    @Test
    void readJavaCodeWithoutCommentsTest() throws IOException {
        Files.writeString(tempFile,
                """
                        //single comment
                        public class TempFile {
                            /*  multiline\s
                                comment */
                            public static void main(String[] args) {
                                // inline comment
                            }
                        }
                   \s""");
        FileProcessor fileProcessor = new FileProcessor(mockLogger, mockOpenAIService);
        String javaCode = fileProcessor.readJavaCodeWithoutComments(tempFile.toFile());

        assertNotNull(javaCode);
        assertNotNull(javaCode);
        assertFalse(javaCode.contains("//"));
        assertFalse(javaCode.contains("/*"));
        assertTrue(javaCode.contains("public class TempFile"));
    }

    @Test
    void readJavaCodeWithoutCommentsTestNull() {
        FileProcessor fileProcessor = new FileProcessor(mockLogger, mockOpenAIService);
        try {
            String javaCode = fileProcessor.readJavaCodeWithoutComments(null);
        }
        catch (Exception e){
            assertEquals(NullPointerException.class, e.getClass());
        }
    }

    @Test
    void checkForHeaderTest() throws IOException {
        Files.writeString(tempFile, Headers.APACHE_LICENCE +
                """
                        //single comment
                        public class TempFile {
                            /*  multiline\s
                                comment */
                            public static void main(String[] args) {
                                // inline comment
                            }
                        }
                   \s""");
        FileProcessor fileProcessor = new FileProcessor(mockLogger, mockOpenAIService);
        String header = fileProcessor.checkForHeader(tempFile.toFile());

        assertNotNull(header);
        assertEquals(Headers.APACHE_LICENCE.trim(),header.trim());
    }

    @Test
    void checkForHeaderTestNull() {
        FileProcessor fileProcessor = new FileProcessor(mockLogger, mockOpenAIService);
        try {
            String header = fileProcessor.checkForHeader(null);
        }
        catch (Exception e){
            assertEquals(NullPointerException.class, e.getClass());
        }
    }

    @Test
    void getAppliedMutatorsWithCommentsTest() {
        String mutatedCode = """
                                // Replaced '+' with '-'
                                public class Mutated {
                                    public static void main(String[] args) {
                                        int x = 5 - 3; // Changed constant from 2 to 3
                                    }
                                }
                            """;
        FileProcessor fileProcessor = new FileProcessor(mockLogger, mockOpenAIService);
        String result = fileProcessor.getAppliedMutators(mutatedCode);

        assertNotNull(result);
        assertTrue(result.contains("Replaced '+' with '-'"));
        assertTrue(result.contains("Changed constant from 2 to 3"));
    }

    @Test
    void getAppliedMutatorsWithNullInputTest() {
        FileProcessor fileProcessor = new FileProcessor(mockLogger, mockOpenAIService);
        String result = fileProcessor.getAppliedMutators(null);

        assertNotNull(result);
        assertEquals("", result);
    }


}