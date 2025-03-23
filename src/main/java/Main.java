import java.io.*;
import java.util.List;


public class Main {
    public static void main(String[] args) {
        try{
            Logger.log("Starting LLM Mutation ...");

            //process args
            List<File> files = InitProcessor.processArguments(args);

            //process file(s)
            if(!files.isEmpty()){
                FileProcessor.processFiles(files);
            }
            else {
                Logger.error("No file(s) found in the specified directory...");
            }

            Logger.log("LLM Mutation Completed");

        } catch (Exception e) {
            Logger.error(e.getMessage());;
        }
    }
}
