package Interfaces;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * MutationProcessor interface
 */
public interface MutationProcessor {

    void processJavaFiles(List<File> files, Path outputPath);

    String readJavaCodeWithoutComments(File file);

    String checkForHeader(File file);

    String getAppliedMutators(String mutatedJavaCode);
}