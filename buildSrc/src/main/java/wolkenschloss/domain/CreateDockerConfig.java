package wolkenschloss.domain;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.FileWriter;
import java.io.IOException;

public abstract class CreateDockerConfig extends DefaultTask {

    @OutputFile
    abstract public RegularFileProperty getDockerConfigFile();

    @Internal
    abstract public Property<DomainOperations> getDomainOperations();

    @TaskAction
    public void write() {
        DomainOperations domainOperations = getDomainOperations().get();
        RegistryService registryService = domainOperations.getRegistry();

        String registry = registryService.getAddress();


        var configFile = getDockerConfigFile().get().getAsFile();

        if (getDockerConfigFile().get().getAsFile().getParentFile().mkdirs()) {
            this.getProject().getLogger().info("Target directory created.");
        }

        if (configFile.exists()) {
            if (configFile.delete()) {
                this.getProject().getLogger().info("Old Docker configuration file deleted");
            }
        }

        try {
            if (configFile.createNewFile()) {
                this.getProject().getLogger().info("Docker configuration file created");
            }
        } catch (IOException e) {
            throw new GradleException("Can not create docker configuration file", e);
        }

        try (FileWriter writer = new FileWriter(configFile)) {
            writer.append("{\n")
                    .append("  \"insecure-registries\" : [\"").append(registry).append("\"]\n")
                    .append("}\n");

            writer.flush();

        } catch (Exception exception) {
            throw new GradleException("Can not write docker configuration file", exception);
        }
    }
}
