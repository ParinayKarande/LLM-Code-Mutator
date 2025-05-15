import Interfaces.LLMApiService;
import Interfaces.LoggerService;
import Interfaces.MutationProcessor;
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
public class FileProcessor implements MutationProcessor {
    private final LoggerService logger;
    private final Map<String, String> mutationOperators;
    private final LLMApiService openAIService;
    private File outputDir;
    private Path rootDir;

    public FileProcessor(LoggerService logger, LLMApiService openAIService){
        this.logger = logger;
        this.mutationOperators = new HashMap<>();
        this.openAIService = openAIService;
    }

    /**
     * This method iterates through list of files to mutate and store them in the output directory
     * @param files is the list of files that needs to be mutated.
     */
    @Override
    public void processJavaFiles(List<File> files, Path outputPath){
        try {
            rootDir = outputPath;
            if (prepareOutputDirectory(rootDir)) {
                logger.log("Output directory created at " + outputDir.getPath());
                int fileCount = files.size();
                logger.log(fileCount + " File(s) to mutate...");
                for (File file : files) {
                    processFile(file);
                    fileCount -= 1;
                    logger.log(fileCount + " file(s) remaining...");
                }

                if(!mutationOperators.isEmpty()){ //Saves all the used mutators in Excel format.
                    reportMutators(outputDir + "/MutationOperators.csv");
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Reads Java file and removes all kinds of comments
     * @param file : file to be mutated
     * @return Java code without comments
     */
    @Override
    public String readJavaCodeWithoutComments(File file) {
        logger.log("Extracting Java Code from file...");
        CompilationUnit cu = new CompilationUnit();
        try {
            cu = StaticJavaParser.parse(file);

            // Remove all comments
            cu.getAllComments().forEach(Comment::remove);
            logger.log("Removing Java Comments...");

        } catch (Exception e) {
            logger.log(e.getMessage());
        }

        return cu.toString();
    }

    /**
     * extractJavaCodeFromResponse
     * @param response
     * @return
     */
    private String extractJavaCodeFromResponse(String response) {
        String startTag = "```java";
        String endTag = "```";

        int startIndex = response.indexOf(startTag);
        int endIndex = response.indexOf(endTag, startIndex + startTag.length());

        if (startIndex != -1 && endIndex != -1) {
            return response.substring(startIndex + startTag.length(), endIndex).trim();
        }
        return "";
    }

    /**
     * saveToFile
     * @param code
     * @param filePath
     */
    private void saveToFile(String code, String filePath) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(code);
            logger.log("Mutated Java code saved to: " + filePath);
        } catch (IOException e) {
            logger.error("Failed to save mutated code to file: " + e.getMessage());
        }
    }

    /**
     * Checks for java licence headers
     * @param file : file under check
     * @return header if found in list of constant headers
     */
    @Override
    public String checkForHeader(File file) {
        try {
            String content = Files.readString(file.toPath());

            for (String header : Headers.getAllHeaders()) {
                if (content.contains(header)) {
                    return header + "\n";
                }
            }
        } catch (Exception e) {
            logger.log("Error reading file: " + e.getMessage());
        }

        return "";
    }

    /**
     * Extracts all mutation operators used to mutate the java file.
     * @param mutatedJavaCode mutated code with comments of operators used.
     * @return list of comments (operators)
     */
    @Override
    public String getAppliedMutators(String mutatedJavaCode){
        StringBuilder operations = new StringBuilder();
        try {
            CompilationUnit cu = StaticJavaParser.parse(mutatedJavaCode);
            List<Comment> comments = cu.getAllComments();
            for(Comment comment : comments){
                operations.append(comment.getContent().trim()).append("\n");
            }
        } catch (Exception e) {
            logger.log(e.getMessage());
        }
        return operations.toString();
    }

    /**
     * Saves list of mutators used for each java mutated file in an Excel format
     * @param excelFilePath : Path for Excel report file.
     */
    private void reportMutators(String excelFilePath) {
        logger.log("Writing Mutation comments to Excel file: " + excelFilePath);

        try (FileWriter writer = new FileWriter(excelFilePath)) {
            writer.append("Java File Name,Mutation Comments\n");

            for (Map.Entry<String, String> entry : mutationOperators.entrySet()) {
                String sanitizedComment = entry.getValue().replace("\"", "\"\"");
                writer.append(entry.getKey()).append(",\"").append(sanitizedComment).append("\"\n");
            }
            logger.log("CSV file created successfully!");
        } catch (IOException e) {
            logger.log(e.getMessage());
        }

    }

    /**
     * prepareOutputDirectory
     * @param rootDir
     * @return
     * @throws IOException
     */
    private boolean prepareOutputDirectory(Path rootDir) throws IOException {
        outputDir = new File(rootDir.toFile(), "output");
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
            logger.log("Output directory cleared...");
        }
        return outputDir.mkdirs();
    }

    /**
     * mutate each file
     * @param file
     * @throws IOException
     */
    private void processFile(File file) throws IOException {
        String javaHeader = checkForHeader(file);
        String javaCode = readJavaCodeWithoutComments(file);

        if (javaCode.isEmpty()) {
            logger.error("No valid Java code found in " + file.getName());
            return;
        } else {
            logger.log("Java code extracted from " + file.getName());
        }

        // Generate and print mutated code
        String mutatedCode = openAIService.askOpenAI(javaCode);

        if (!mutatedCode.isEmpty()) {
            String extractedJavaCode = extractJavaCodeFromResponse(mutatedCode);
            if (!extractedJavaCode.isEmpty()) {
                Path relativePath = rootDir.relativize(file.toPath());
                Path outputFilePath = outputDir.toPath().resolve(relativePath);

                // Create parent dirs if it doesn't exist
                Files.createDirectories(outputFilePath.getParent());
                saveToFile(javaHeader + extractedJavaCode, outputFilePath.toString());

                mutationOperators.put(relativePath.toString(),getAppliedMutators(extractedJavaCode));
            } else {
                logger.error("Java code block not found in response. LLM Mutation for " + file.getName() + " failed...");
            }
        } else {
            logger.error("No Mutation response returned for " + file.getName());
        }
    }
}
