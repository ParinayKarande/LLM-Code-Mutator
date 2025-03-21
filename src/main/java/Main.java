import java.io.*;

public class Main {
    public static void main(String[] args) {
        try{
            Logger.log("Starting LLM Mutation ...");

            // Read Java code from the file
            //File file = new File("/Users/parinaykarande/Desktop/Capstone/Spring_25/OpenSrc/txt/CSV/CSVPrinter.java");
            File file = new File(args[0]);
            String javaCode = FileProcessor.readJavaCodeWithoutComments(file);

            if (javaCode.isEmpty()) {
                Logger.error("No valid Java code found in the file.");
                return;
            }
            else {
                Logger.log("Java code extracted from file...");
            }

            // Generate and print mutated code
            String mutatedCode = OpenAIService.askOpenAI(javaCode);
            System.out.println("GPT Response:\n" + mutatedCode);

            // Extract Java and Save to File.
            String extractedJavaCode = FileProcessor.extractJavaCodeFromResponse(mutatedCode);
            if (!extractedJavaCode.isEmpty()) {
                String savePath = "/Users/parinaykarande/Desktop/Capstone/Spring_25/OpenSrc/txt/output/CSV/CSVPrinter.java";
                FileProcessor.saveToFile(extractedJavaCode, savePath);
            }
            else {
                Logger.error("Java code block not found in response.");
            }

            Logger.log("LLM Mutation Completed.");


        } catch (Exception e) {
            Logger.error(e.getMessage());;
        }
    }
}
