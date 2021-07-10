package wolkenschloss.pool;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.GradleScriptException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

// TODO: Refactor. wolkenschloss.task darf nicht auf wolkenschloss zugreifen.
import wolkenschloss.Testbed;

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

    @TaskAction
    public void exec() {

        try (var testbed = new Testbed(getDomainName().get())) {
            if (getPoolRunFile().get().getAsFile().exists()) {
                testbed.destroyPool(getPoolRunFile());
                getLogger().info("Pool destroyed");
            }

            if (testbed.poolExistiert(getPoolName())) {
                var message = String.format(
                        "Der Pool %1$s existiert bereits, aber die Markierung-Datei %2$s ist nicht vorhanden.%n" +
                                "Löschen Sie ggf. den Storage Pool mit dem Befehl 'virsh pool-destroy %1$s && virsh pool-undefine %1$s'",
                        getPoolName().get(),
                        getPoolRunFile().get().getAsFile().getPath());

                throw new GradleException(message);
            }

            var uuid = testbed.createPool(getXmlDescription());
            Files.writeString(getPoolRunFile().getAsFile().get().toPath(), uuid.toString());

        } catch (Exception e) {
            throw new GradleScriptException("Can not define storage pool", e);
        }
    }
}
