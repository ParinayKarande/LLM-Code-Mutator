/**
 * This class logs messages in the console.
 */
public class Logger {
    public static void log(String message) {
        System.out.println("[INFO] : " + message);
    }

    public static void error(String message) {
        System.out.println("[ERROR] : " + message);
    }

    public static void debug(String message) {
        System.out.println("[DEBUG] : " + message);
    }
}
