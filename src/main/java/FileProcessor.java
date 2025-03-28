import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import org.apache.commons.io.FileUtils;
import constants.Headers;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * This class handles file related processes
 */
public class FileProcessor {

    /**
     * This method iterates through list of files to mutate and store them in the output directory
     * @param files is the list of files that needs to be mutated.
     */
    public static void processFiles(List<File> files){
        try {
            File outputDir = new File(Paths.get(files.getFirst().getAbsolutePath()).getParent().getParent().toString(), "output");
            if (outputDir.exists()) {
                FileUtils.deleteDirectory(outputDir);  // Deletes existing output directory and all its contents
                Logger.log("Output directory cleared...");
            }
            if (outputDir.mkdirs()) {
                Logger.log("Output directory created at " + outputDir.getPath());

                for (File file : files) {
                    String javaHeader = checkForHeader(file);
                    String javaCode = readJavaCodeWithoutComments(file);

                    if (javaCode.isEmpty()) {
                        Logger.error("No valid Java code found in " + file.getName());
                        return;
                    } else {
                        Logger.log("Java code extracted from " + file.getName());
                    }

                    // Generate and print mutated code
                    String mutatedCode = OpenAIService.askOpenAI(javaCode);
                    //System.out.println("GPT Response:\n" + mutatedCode); // Commented to avoid printing entire response.

                    if (!mutatedCode.isEmpty()) {

                        // Extract Java and Save to File.
                        String extractedJavaCode = extractJavaCodeFromResponse(mutatedCode);
                        if (!extractedJavaCode.isEmpty()) {

                            saveToFile(javaHeader + extractedJavaCode, outputDir.getPath() + "/" + file.getName());
                        } else {
                            Logger.error("Java code block not found in response. LLM Mutation for " + file.getName() + " failed...");
                        }
                    } else {
                        Logger.error("No Mutation response returned for " + file.getName());
                    }
                }
            }
        } catch (IOException e) {
            Logger.log(e.getMessage());
        }
    }

    /**
     * Reads Java file and removes all kinds of comments
     * @param file : file to be mutated
     * @return Java code without comments
     */
    public static String readJavaCodeWithoutComments(File file) {
        Logger.log("Extracting Java Code from file...");
        CompilationUnit cu = new CompilationUnit();
        try {
            cu = StaticJavaParser.parse(file);

            // Remove all comments
            cu.getAllComments().forEach(Comment::remove);
            Logger.log("Removing Java Comments...");

        } catch (Exception e) {
            Logger.log(e.getMessage());
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


    /**
     * Checks for java licence headers
     * @param file : file under check
     * @return header if found in list of constant headers
     */
    public static String checkForHeader(File file) {
        try {
            String content = Files.readString(file.toPath());

            for (String header : Headers.getAllHeaders()) {
                if (content.contains(header)) {
                    return header + "\n";
                }
            }
        } catch (Exception e) {
            Logger.log("Error reading file: " + e.getMessage());
        }

        return "";
    }
}
