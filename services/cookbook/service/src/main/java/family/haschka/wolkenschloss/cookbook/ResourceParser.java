package family.haschka.wolkenschloss.cookbook;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public interface ResourceParser {
    List<String> readData(URI source) throws IOException;
}
