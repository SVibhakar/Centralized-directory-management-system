//Name : Sejal Vibhakar
//ID : 1001765264
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DirectoryHelper {

    // https://stackoverflow.com/a/24006711
    // Picked up code to walk / traverse directory
    public static List<String> getSyncData(String dirName) {

        Path basePath = Paths.get(dirName);
        try {
            List<String> paths = Files.walk(basePath).filter(p -> Files.isDirectory(p)).map(p -> p.toString())
                    .collect(Collectors.toList());
            return paths;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // https://www.baeldung.com/java-delete-directory
    public static void deleteRecursively(Path path) {
        if (!path.toFile().exists())
            return;
        try {
            Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createSyncDir(String baseDir, List<String> paths) {
        try {
            Path basePath = Paths.get("local", baseDir);
            deleteRecursively(basePath);
            Files.createDirectories(basePath);
            for (String string : paths) {
                Files.createDirectories(basePath.resolve(string));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
