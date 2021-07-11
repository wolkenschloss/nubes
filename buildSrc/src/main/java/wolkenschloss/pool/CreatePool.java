package wolkenschloss.pool;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.GradleScriptException;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.*;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

@CacheableTask
abstract public class CreatePool extends DefaultTask {

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract public RegularFileProperty getXmlDescription();

    @OutputFile
    abstract public RegularFileProperty getPoolRunFile();

    @Internal
    abstract public Property<PoolOperations> getPoolOperations();

    @Inject
    abstract public FileSystemOperations getFso();

    @Inject
    abstract public ProviderFactory getProviderFactory();

    @TaskAction
    public void exec() {

        var poolOperations = getPoolOperations().get();

        try {
            File runFile = getPoolRunFile().get().getAsFile();
            var runFileContent = getProviderFactory().fileContents(getPoolRunFile());
            if (runFile.exists()) {
                var oldPoolUuid = runFileContent.getAsText().map(UUID::fromString).get();
                poolOperations.destroy(oldPoolUuid);
                getFso().delete(spec -> spec.delete(getPoolRunFile()));

                getLogger().info("Pool destroyed");
            }

            if (poolOperations.exists()) {
                var message = String.format(
                        "Der Pool %1$s existiert bereits, aber die Markierung-Datei %2$s ist nicht vorhanden.%n" +
                                "Löschen Sie ggf. den Storage Pool mit dem Befehl 'virsh pool-destroy %1$s && virsh pool-undefine %1$s'",
                        poolOperations.getParameters().getPoolName().get(),
                        runFile.getPath());

                throw new GradleException(message);
            }

            var uuid = poolOperations.create(getXmlDescription());
            Files.writeString(runFile.toPath(), uuid.toString());

        } catch (Exception e) {
            throw new GradleScriptException("Can not define storage pool", e);
        }
    }
}
