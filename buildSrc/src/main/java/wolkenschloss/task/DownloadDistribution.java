package wolkenschloss.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;

@CacheableTask
abstract public class DownloadDistribution extends DefaultTask {

    public DownloadDistribution() {
    }

    @Inject
    protected abstract FileSystemOperations getFileSystemOperations();

    @Inject
    protected abstract ExecOperations getExecOperations();

    @Input
    abstract public Property<String> getBaseImageLocation();

    @Input
    abstract public Property<String> getDistributionName();

    @Internal
    abstract public RegularFileProperty getBaseImage();

    @Internal
    abstract public DirectoryProperty getDistributionDir();

    private URL getBaseImageUrl() throws MalformedURLException {
        return new URL(getBaseImageLocation().get());
    }

    private URL getSha256SumsUrl() throws MalformedURLException, URISyntaxException {
        URI location = getBaseImageUrl().toURI();
        URI parent = location.getPath().endsWith("/") ? location.resolve("..") : location.resolve(".");
        return parent.resolve("SHA256SUMS").toURL();
    }

    private URL getGpgFileUrl() throws MalformedURLException, URISyntaxException {
        URI location = getBaseImageUrl().toURI();
        URI parent = location.getPath().endsWith("/") ? location.resolve("..") : location.resolve(".");
        return parent.resolve("SHA256SUMS.gpg").toURL();
    }

    @TaskAction
    public void download() throws URISyntaxException, IOException {

        downloadFile(getBaseImageUrl());
        downloadFile(getSha256SumsUrl());
        downloadFile(getGpgFileUrl());

        verifySignature();
        verifyChecksum();

        changePermissions();
    }

    private void changePermissions() {

        var file = new File(getBaseImage().getAsFile().get().toPath().toString());
        var dir = file.getParentFile();

        Arrays.stream(dir.listFiles()).forEach(f -> {
            var success = f.setWritable(false, false)
                    && f.setReadable(true, true)
                    && f.setExecutable(false, false);
            if (!success) {
                var message = String.format("Can not change file permissions: %s", file.getPath());
                getLogger().warn(message);
            }
        });
    }

    private Provider<RegularFile> downloadPath(String filename) {
        return getDistributionDir().file(filename);
    }

    private void verifyChecksum() {
        getExecOperations().exec(spec -> spec.commandLine("sha256sum")
                .args("--ignore-missing",
                        "--check",
                        "SHA256SUMS")
                .workingDir(getDistributionDir().get()))
                .assertNormalExitValue();
    }

    private void verifySignature() {
        getExecOperations().exec(spec -> spec.commandLine("gpg")
                .args("--keyid-format", "long", "--verify",
                        downloadPath("SHA256SUMS.gpg").get(),
                        downloadPath("SHA256SUMS").get()))
                .assertNormalExitValue();
    }

    private void downloadFile(URL src) throws IOException {

        getLogger().lifecycle("Downloading file {}", src);

        var filename = Path.of(src.getFile()).getFileName();
        var dst = getDistributionDir().file(filename.toString()).get().getAsFile();

        //noinspection ResultOfMethodCallIgnored
        dst.getParentFile().mkdirs();

        if (dst.exists()) {
            return;
        }

        try (InputStream input = src.openStream()) {
            try (var output = new FileOutputStream(dst)) {
                input.transferTo(output);
            }
        }
    }
}