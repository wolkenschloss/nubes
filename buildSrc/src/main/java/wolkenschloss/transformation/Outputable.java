package wolkenschloss.transformation;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;

import java.util.function.Function;

public interface Outputable {
    Registrable outputDescription(Function<DirectoryProperty, Provider<RegularFile>> fn);
    Registrable outputCloudConfig(Function<DirectoryProperty, Provider<RegularFile>> fn);
}
