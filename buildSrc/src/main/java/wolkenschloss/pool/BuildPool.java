package wolkenschloss.pool;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.GradleScriptException;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

@CacheableTask
abstract public class BuildPool extends DefaultTask {

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract public RegularFileProperty getPoolDescriptionFile();

    @OutputFile
    abstract public RegularFileProperty getPoolRunFile();

    @Internal
    abstract public Property<PoolOperations> getPoolOperations();

    @Inject
    abstract public FileSystemOperations getFileSystemOperations();

    @Inject
    abstract public ProviderFactory getProviderFactory();

    @TaskAction
    public void exec() {

        var poolOperations = getPoolOperations().get();

        try {
            File runFile = getPoolRunFile().get().getAsFile();
            var runFileContent = getProviderFactory().fileContents(getPoolRunFile());
            if (runFile.exists()) {

                @SuppressWarnings("NullableProblems")
                var oldPoolUuid = runFileContent.getAsText().map(UUID::fromString).get();

                poolOperations.destroy(oldPoolUuid);
                getFileSystemOperations().delete(spec -> spec.delete(getPoolRunFile()));

                getLogger().info("Pool destroyed");
            }

            if (poolOperations.exists()) {
                @SuppressWarnings({"UnstableApiUsage", "SpellCheckingInspection"})
                var message = String.format(
                        "Der Pool %1$s existiert bereits, aber die Markierung-Datei %2$s ist nicht vorhanden.%n" +
                        "Löschen Sie ggf. den Storage Pool mit dem Befehl 'virsh pool-destroy %1$s && virsh "+
                        "pool-undefine %1$s'",
                        poolOperations.getParameters().getPoolName().get(),
                        runFile.getPath());

                throw new GradleException(message);
            }

            var uuid = poolOperations.create(getPoolDescriptionFile());
            Files.writeString(runFile.toPath(), uuid.toString());

        } catch (Exception e) {
            throw new GradleScriptException("Can not define storage pool", e);
        }
    }
}
