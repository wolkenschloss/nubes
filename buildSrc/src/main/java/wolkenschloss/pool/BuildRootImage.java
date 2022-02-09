package wolkenschloss.pool;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecSpec;
import wolkenschloss.TestbedExtension;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/**
 * Erzeugt das Festplatten-Abbild für die virtuelle Maschine
 * des Prüfstandes.
 */
// Wenn es mal wieder Probleme mit Permission Denied gibt, wenn die
// VM startet soll: Einfach mal appamor für diese VM ausschalten:
@CacheableTask
abstract public class BuildRootImage extends DefaultTask {

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract public RegularFileProperty getBaseImage();

    @Input
    abstract public Property<String> getSize();

    @Internal
    abstract public RegularFileProperty getRootImage();

    @OutputFile
    abstract public RegularFileProperty getRootImageMd5File();

    @Inject
    abstract public ExecOperations getExecOperations();

    @TaskAction
    public void exec() throws NoSuchAlgorithmException, IOException {

        boolean created = getRootImage().get().getAsFile().getParentFile().mkdirs();
        var extension = Optional.ofNullable(getProject().getExtensions().findByType(TestbedExtension.class));
        var failOnError = extension.map(ext -> ext.getFailOnError().get())
                .orElse(true);

        getLogger().info("Testbed extension found: {}", extension.isPresent());
        getLogger().info("failOnError set to {}", failOnError);

        if (created) {
            getLogger().info("Directory created");
        }

        exec(spec -> spec
                .commandLine("qemu-img")
                .args("create", "-f", "qcow2", "-F", "qcow2", "-b",
                        getBaseImage().get(),
                        getRootImage().get())
                .setIgnoreExitValue(!failOnError));

        exec(spec -> spec.commandLine("qemu-img")
                .args("resize", getRootImage().get(), getSize().get())
                .setIgnoreExitValue(!failOnError));

        getLogger().info("Root image created");

        var hash = getBaseImage().get().getAsFile().getAbsolutePath() + getSize().get();
        var md = MessageDigest.getInstance("MD5");

        md.update(hash.getBytes());
        Files.write(getRootImageMd5File().getAsFile().get().toPath(), md.digest());
        getLogger().info(String.format("Root image MD5 hash is %032x", new BigInteger(1, md.digest())));
    }

    private void exec(Action<? super ExecSpec> spec) throws IOException {
        try (var stdout = new ByteArrayOutputStream()) {
            var result = getExecOperations().exec(s -> {
                getLogger().info("Executing {}", String.join(" ", s.getCommandLine()));
                spec.execute(s);
                s.setStandardOutput(stdout);
            });
            getLogger().info(stdout.toString());

            getLogger().info("Execute result {}", result.getExitValue());
//            result.assertNormalExitValue();
        }
    }
}

