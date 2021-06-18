package wolkenschloss;

import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;

abstract public class TestbedView {

    abstract public Property<String> getSshKey();
    abstract public Property<String> getHostname();
    abstract public Property<String> getUser();

    public abstract Property<String> getHostAddress();
    public abstract Property<Integer> getCallbackPort();
}
