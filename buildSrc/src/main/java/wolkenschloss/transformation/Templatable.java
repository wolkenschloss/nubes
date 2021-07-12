package wolkenschloss.transformation;

import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;

public interface Templatable {
    Outputable template(Provider<RegularFile> template);
}
