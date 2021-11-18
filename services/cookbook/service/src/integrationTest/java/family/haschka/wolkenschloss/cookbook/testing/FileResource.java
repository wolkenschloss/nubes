package family.haschka.wolkenschloss.cookbook.testing;

import org.jboss.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileResource {
    private static final Logger logger = Logger.getLogger(FileResource.class);

    private final String path;

    public FileResource(String path) {
        this.path = path;
    }

    public String read() {
        logger.infov("reading {0}", this.path);

        var path = Paths.get(ClassLoader.getSystemResource(this.path).getPath());
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
