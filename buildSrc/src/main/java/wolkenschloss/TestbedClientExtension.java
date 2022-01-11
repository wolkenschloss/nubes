package wolkenschloss;

import org.gradle.api.Project;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.provider.Property;

public abstract class TestbedClientExtension {

    public abstract Property<LogLevel> getContainerLogLevel();

    public TestbedClientExtension configure(Project project) {

        getContainerLogLevel().convention(LogLevel.INFO);

        return this;
    }
}
