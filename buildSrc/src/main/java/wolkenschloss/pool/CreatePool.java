package wolkenschloss.pool;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.GradleScriptException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import java.io.File;
import java.nio.file.Files;

@CacheableTask
abstract public class CreatePool extends DefaultTask {

    @Input
    public abstract Property<String> getPoolName();

    @Input
    public abstract Property<String> getDomainName();

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract public RegularFileProperty getXmlDescription();

    @OutputFile
    abstract public RegularFileProperty getPoolRunFile();

    @Internal
    abstract public Property<PoolOperations> getPoolOperations();

    @TaskAction
    public void exec() {

        var poolOperations = getPoolOperations().get();

        try {
            File runFile = getPoolRunFile().get().getAsFile();
            if (runFile.exists()) {
                poolOperations.destroyPool(getPoolRunFile());
                getLogger().info("Pool destroyed");
            }

            if (poolOperations.poolExistiert(getPoolName())) {
                var message = String.format(
                        "Der Pool %1$s existiert bereits, aber die Markierung-Datei %2$s ist nicht vorhanden.%n" +
                                "Löschen Sie ggf. den Storage Pool mit dem Befehl 'virsh pool-destroy %1$s && virsh pool-undefine %1$s'",
                        getPoolName().get(),
                        runFile.getPath());

                throw new GradleException(message);
            }

            var uuid = poolOperations.createPool(getXmlDescription());
            Files.writeString(runFile.toPath(), uuid.toString());

        } catch (Exception e) {
            throw new GradleScriptException("Can not define storage pool", e);
        }
    }
}
