import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import constants.Headers;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * This class handles file related processes
 */
public class FileProcessor {

    public static Map<String, String> mutationOperators = new HashMap<>();

    /**
     * This method iterates through list of files to mutate and store them in the output directory
     * @param files is the list of files that needs to be mutated.
     */
    public static void processFiles(List<File> files){
        try {
            Path rootDir = Paths.get(files.getFirst().getAbsolutePath()).getParent().getParent();
            File outputDir = new File(rootDir.toFile(), "output");
            if (outputDir.exists()) {
                Files.walkFileTree(outputDir.toPath(), new SimpleFileVisitor<>(){
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);  // delete file
                        return FileVisitResult.CONTINUE;
                    }
                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);  // delete empty dir after contents
                        return FileVisitResult.CONTINUE;
                    }
                });
                Logger.log("Output directory cleared...");
            }

            if (outputDir.mkdirs()) {
                Logger.log("Output directory created at " + outputDir.getPath());
                int fileCount = files.size();
                Logger.log(fileCount + " File(s) to mutate...");

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
                            Path relativePath = rootDir.relativize(file.toPath());
                            Path outputFilePath = outputDir.toPath().resolve(relativePath);

                            // Create parent dirs if it doesn't exist
                            Files.createDirectories(outputFilePath.getParent());
                            saveToFile(javaHeader + extractedJavaCode, outputFilePath.toString());

                            mutationOperators.put(file.getName(),getAppliedMutators(extractedJavaCode));
                        } else {
                            Logger.error("Java code block not found in response. LLM Mutation for " + file.getName() + " failed...");
                        }
                    } else {
                        Logger.error("No Mutation response returned for " + file.getName());
                    }
                    fileCount -= 1;
                    Logger.log(fileCount + " file(s) remaining...");
                }
                if(!mutationOperators.isEmpty()){ //Saves all the used mutators in Excel format.
                    reportMutators(outputDir + "/MutationOperators.csv");
                }
            }
        } catch (IOException e) {
            Logger.error(e.getMessage());
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

    /**
     * Extracts all mutation operators used to mutate the java file.
     * @param mutatedJavaCode mutated code with comments of operators used.
     * @return list of comments (operators)
     */
    public static String getAppliedMutators(String mutatedJavaCode){
        StringBuilder operations = new StringBuilder();
        try {
            CompilationUnit cu = StaticJavaParser.parse(mutatedJavaCode);
            List<Comment> comments = cu.getAllComments();
            for(Comment comment : comments){
                operations.append(comment.getContent().trim()).append("\n");
            }
        } catch (Exception e) {
            Logger.log(e.getMessage());
        }
        return operations.toString();
    }

    /**
     * Saves list of mutators used for each java mutated file in an Excel format
     * @param excelFilePath : Path for Excel report file.
     */
    public static void reportMutators(String excelFilePath) {
        Logger.log("Writing Mutation comments to Excel file: " + excelFilePath);

        try (FileWriter writer = new FileWriter(excelFilePath)) {
            writer.append("Java File Name,Mutation Comments\n");

            for (Map.Entry<String, String> entry : mutationOperators.entrySet()) {
                String sanitizedComment = entry.getValue().replace("\"", "\"\"");
                writer.append(entry.getKey()).append(",\"").append(sanitizedComment).append("\"\n");
            }
            Logger.log("CSV file created successfully!");
        } catch (IOException e) {
            Logger.log(e.getMessage());
        }

    }
}
