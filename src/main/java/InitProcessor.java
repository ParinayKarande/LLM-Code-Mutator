import java.io.File;
import java.nio.file.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import java.io.IOException;

/**
 * Initial Processor method to parse arguments
 */
public class InitProcessor {
    public static List<File> processArguments(String[] args){

        List<File> files = new LinkedList<>();
        try {
            // Default values
            String filePath = null;
            String dirPath = null;

            // Process command-line arguments
            for (int i = 0; i < args.length; i++) {
                if ("-f".equals(args[i])) {
                    filePath = args[i + 1];
                } else if ("-dir".equals(args[i])) {
                    dirPath = args[i + 1];
                }
            }

            if (filePath != null) {
                // Return a single file
                File file = new File(filePath);
                files.add(file);
            }
            else if (dirPath != null){
                File dir = new File(dirPath);
                if (dir.exists() && dir.isDirectory()) {
                    try (Stream<Path> paths = Files.walk(dir.toPath())) {
                        paths.filter(Files::isRegularFile)
                                .filter(path -> path.toString().endsWith(".java"))
                                .forEach(path -> files.add(path.toFile()));
                    } catch (IOException e) {
                        Logger.error("Error walking through directory: " + e.getMessage());
                    }
                } else {
                    Logger.error("Invalid directory path.");
                }
            }
            else {
                Logger.error("Please specify a file or directory.");
            }

        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
        return files;
    }

}
