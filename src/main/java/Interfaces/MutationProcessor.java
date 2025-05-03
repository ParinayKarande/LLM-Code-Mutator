package Interfaces;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public interface MutationProcessor {
    void processJavaFiles(List<File> files, Path outputPath);
}
