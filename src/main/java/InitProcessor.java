import Interfaces.*;
import Interfaces.LoggerService;

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
public class InitProcessor implements ArgumentParser {
    private final String[] args;
    private final LoggerService logger;
    private Path outputPath;

    public InitProcessor(String[] args, LoggerService logger) {
        this.args = args;
        this.logger = logger;
    }

    @Override
    public List<File> getJavaFiles(){

        List<File> files = new LinkedList<>();
        try {
            // Default values
            String filePath = null;
            String dirPath = null;

            // Process command-line arguments
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-f":
                        if (i + 1 < args.length) {
                            filePath = args[++i];
                        }
                        break;
                    case "-dir":
                        if (i + 1 < args.length) {
                            dirPath = args[++i];
                        }
                        break;
                }
            }

            if (filePath != null) {
                // Return a single file
                File file = new File(filePath);
                if (file.exists() && file.isFile()) {
                    files.add(file);
                    setOutputPath(Paths.get(file.getParent()));
                } else {
                    logger.error("File does not exist or is not a valid file: " + filePath);
                }
            }
            else if (dirPath != null){
                File dir = new File(dirPath);
                if (dir.exists() && dir.isDirectory()) {
                    try (Stream<Path> paths = Files.walk(dir.toPath())) {
                        paths.filter(Files::isRegularFile)
                                .filter(path -> path.toString().endsWith(".java"))
                                .forEach(path -> files.add(path.toFile()));

                        setOutputPath(Paths.get(dir.getParent()));
                    } catch (IOException e) {
                        logger.error("Error walking through directory: " + e.getMessage());
                    }
                } else {
                    logger.error("Invalid directory path.");
                }
            }
            else {
                logger.error("Please specify a file or directory.");
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return files;
    }

    /**
     * reads which LLM Model to be used
     * @return model type
     */
    public String getModel() {
        String model = "gpt4omini";//default model
        for (int i = 0; i < args.length; i++) {
            if ("-model".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                model = args[i + 1].toLowerCase();
            }
        }
        return model;
    }

    public LLMApiService createAPIService() {
        LLMApiService apiService = null;
        String model = getModel().trim().toLowerCase();
        logger.log("Using LLM model: " + model);

        if (model.equals("gpt4omini")) {
            String apiKey = System.getenv("OPENAI_API_KEY");
            if (apiKey != null && !apiKey.trim().isEmpty()) {
                apiService = new OpenAIService("gpt-4o-mini"
                        , apiKey
                        , "https://api.openai.com/v1/chat/completions"
                        , logger);
            } else {
                logger.error("API Key not found in Environment Variables");
            }
        }
        return apiService;
    }

    public Path getOutputPath(){
        return this.outputPath;
    }

    public void setOutputPath(Path outputPath){
        this.outputPath = outputPath;
    }

}
