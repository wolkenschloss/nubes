package wolkenschloss.task;

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
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Erzeugt das Festplatten-Abbild für die virtuelle Maschine
 * des Prüfstandes.
 */
// Wenn es mal wieder Probleme mit Permission Denied gibt, wenn die
// VM startet soll: Einfach mal appamor für diese VM ausschalten:
@CacheableTask
abstract public class CreateRootImage extends DefaultTask {

    public static final String DEFAULT_IMAGE_SIZE = "20G";
    public static final String DEFAULT_RUN_FILE_NAME = "root.md5";

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

    public void initialize(TestbedExtension extension, TaskProvider<DownloadDistribution> downloadDistribution) {
        getSize().convention(DEFAULT_IMAGE_SIZE);
        getBaseImage().convention(downloadDistribution.get().getBaseImage());
        getRootImage().convention(extension.getPoolDirectory().file(extension.getPool().getRootImageName()));
        getRootImageMd5File().convention(extension.getRunDirectory().file(DEFAULT_RUN_FILE_NAME));
    }

    @TaskAction
    public void exec() throws NoSuchAlgorithmException, IOException {

        boolean created = getRootImage().get().getAsFile().getParentFile().mkdirs();
        if (created) {
            getLogger().info("Directory created");
        }

        exec(spec -> spec.commandLine("qemu-img")
                .args("create", "-f", "qcow2", "-F", "qcow2", "-b",
                        getBaseImage().get(),
                        getRootImage().get()));

        exec(spec -> spec.commandLine("qemu-img")
                    .args("resize", getRootImage().get(), getSize().get()));

        var hash = getBaseImage().get().getAsFile().getAbsolutePath() + getSize().get();
        var md = MessageDigest.getInstance("MD5");

        md.update(hash.getBytes());
        Files.write(getRootImageMd5File().getAsFile().get().toPath(), md.digest());
    }

    private void exec(Action<? super ExecSpec> spec) throws IOException {
        try (var stdout = new ByteArrayOutputStream()) {
            var result = getExecOperations().exec(s -> {
                spec.execute(s);
                s.setStandardOutput(stdout);
            });
            getLogger().info(stdout.toString());
            result.assertNormalExitValue();
        }
    }
}

