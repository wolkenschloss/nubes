package wolkenschloss;

import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;

interface PartialPath<T extends FileSystemLocation> {
    FileCollection source();

    Provider<T> build();
}
