import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
                    File[] dirFiles = dir.listFiles((dir1, name) -> name.endsWith(".java"));
                    if (dirFiles != null) {
                        Collections.addAll(files, dirFiles);
                    } else {
                        Logger.error("No Java files found in the directory.");
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
