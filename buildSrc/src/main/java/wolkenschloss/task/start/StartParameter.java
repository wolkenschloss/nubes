package wolkenschloss.task.start;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;

public interface StartParameter {

    Property<String> getDomain();

    Property<Integer> getPort();

    DirectoryProperty getRunDirectory();
}
