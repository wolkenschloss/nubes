package wolkenschloss;

import org.gradle.api.GradleScriptException;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class UserExtension {
    public void initialize() {
        getSshKeyFile().convention(() -> Path.of(System.getenv("HOME"), ".ssh", "id_rsa.pub").toFile());
        getSshKey().convention(getSshKeyFile().map(UserExtension::readSshKey));
        getPrivateSshKeyFile().convention(() -> Path.of(System.getenv("HOME"), ".ssh", "id_rsa").toFile());
        getName().convention(System.getenv("USER"));
    }

    @Nonnull
    public static String readSshKey(RegularFile sshKeyFile) {

        var file = sshKeyFile.getAsFile();

        try {
            return Files.readString(file.toPath()).trim();
        } catch (IOException e) {
            throw new GradleScriptException("Can not read public ssh key", e);
        }
    }

    abstract public RegularFileProperty getSshKeyFile();

    abstract public Property<String> getSshKey();

    abstract public RegularFileProperty getPrivateSshKeyFile();

    abstract public Property<String> getName();
}
