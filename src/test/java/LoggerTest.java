import Interfaces.LoggerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class LoggerTest {

    LoggerService mockLoggerService;
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        mockLoggerService = new Logger();
    }


    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void log() {
        mockLoggerService.log("Test message.");
        String output = outContent.toString();
        assertTrue(output.contains("[INFO] : Test message."));
    }

    @Test
    void error() {
        mockLoggerService.error("Test error message.");
        String output = outContent.toString();
        assertTrue(output.contains("[ERROR] : Test error message."));
    }

    @Test
    void debug() {
        mockLoggerService.debug("Test debug.");
        String output = outContent.toString();
        assertTrue(output.contains("[DEBUG] : Test debug."));
    }
}