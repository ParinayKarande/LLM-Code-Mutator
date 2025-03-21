import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

public class FileProcessor {

    private static final Log log = LogFactory.getLog(FileProcessor.class);

    public static String readJavaCodeWithoutComments(File file) {
        log.info("Extracting Java Code from file...");
        CompilationUnit cu = new CompilationUnit();
        try {
            cu = StaticJavaParser.parse(file);

            // Remove all comments
            cu.getAllComments().forEach(Comment::remove);
            log.info("Removing Java Comments...");

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return cu.toString();
    }


    public static String extractJavaCodeFromResponse(String response) {
        String startTag = "```java";
        String endTag = "```";

        int startIndex = response.indexOf(startTag);
        int endIndex = response.indexOf(endTag, startIndex + startTag.length());

        if (startIndex != -1 && endIndex != -1) {
            return response.substring(startIndex + startTag.length(), endIndex).trim();
        }
        return "";
    }

    public static void saveToFile(String code, String filePath) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(code);
            Logger.log("Mutated Java code saved to: " + filePath);
        } catch (IOException e) {
            Logger.error("Failed to save mutated code to file: " + e.getMessage());
        }
    }


}
