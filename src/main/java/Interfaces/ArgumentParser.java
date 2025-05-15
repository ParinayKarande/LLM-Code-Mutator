package Interfaces;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public interface ArgumentParser {
    List<File> getJavaFiles();

    String getModel();

    LLMApiService createAPIService();
}
