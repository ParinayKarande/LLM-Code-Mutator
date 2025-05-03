import Interfaces.LoggerService;

/**
 * This class logs messages in the console.
 */
public class Logger implements LoggerService {
    @Override
    public void log(String message) {
        System.out.println("[INFO] : " + message);
    }

    @Override
    public void error(String message) {
        System.out.println("[ERROR] : " + message);
    }

    @Override
    public void debug(String message) {
        System.out.println("[DEBUG] : " + message);
    }
}
