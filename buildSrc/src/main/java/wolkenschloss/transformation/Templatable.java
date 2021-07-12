package wolkenschloss.transformation;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;

import java.util.function.Function;

public interface Templatable {
    Outputable template(Function<DirectoryProperty, Provider<RegularFile>> fn);
}
