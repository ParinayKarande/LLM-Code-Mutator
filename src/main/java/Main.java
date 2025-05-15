import Interfaces.LLMApiService;
import Interfaces.LoggerService;

import java.io.*;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        LoggerService logger = new Logger();
        InitProcessor initProcessor = new InitProcessor(args, logger);

        try{
            logger.log("Starting LLM Mutator ...");

            List<File> files = initProcessor.getJavaFiles();
            LLMApiService apiService = initProcessor.createAPIService();

            //process file(s)
            if (files.isEmpty()) {
                logger.error("No file(s) found in the specified directory. LLM Mutation Terminated...");
            } else if (apiService == null) {
                logger.error("Unsupported LLM model. LLM Mutation Terminated...");
            } else {
                FileProcessor fileProcessor = new FileProcessor(logger, apiService);
                fileProcessor.processJavaFiles(files,initProcessor.getOutputPath());
                logger.log("LLM Mutation Completed");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
