package wolkenschloss.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.GradleScriptException;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import org.gradle.process.ExecOperations;
import wolkenschloss.Distribution;

import javax.inject.Inject;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;

@CacheableTask
abstract public class DownloadTask extends DefaultTask {

    public DownloadTask() {
    }

    @Inject
    protected abstract FileSystemOperations getFileSystemOperations();

    @Inject
    protected abstract ExecOperations getExecOperations();

    @Input
    abstract public Property<String> getBaseImageLocation();

    @Input
    abstract public Property<String> getDistributionName();

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

    @Internal
    abstract public RegularFileProperty getBaseImage();

    @Inject
    protected abstract ProgressLoggerFactory getProgressLoggerFactory();

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
            if (!success)  {
                var message = String.format("Can not change file permissions: %s", file.getPath());
                throw new GradleException(message);
            }
        });
    }

    private Path downloadPath(String filename) {
        return new Distribution(getProject().getObjects(), getDistributionName()).getDistributionDir().resolve(filename);
    }

    private void verifyChecksum() {
        getExecOperations().exec(spec -> spec.commandLine("sha256sum")
                .args("--ignore-missing",
                        "--check",
                        downloadPath("SHA256SUMS"))
                .workingDir(new Distribution(getProject().getObjects(), getDistributionName()).getDistributionDir()))
                .assertNormalExitValue();
    }

    private void verifySignature() {
        getExecOperations().exec(spec -> spec.commandLine("gpg")
                .args("--keyid-format", "long", "--verify",
                        downloadPath("SHA256SUMS.gpg"),
                        downloadPath("SHA256SUMS")))
                .assertNormalExitValue();
    }

    private void downloadFile(URL src) throws IOException {
        var progressLogger = getProgressLoggerFactory().newOperation(DownloadTask.class);

        progressLogger.start("Download base Image", src.getFile());

        try (InputStream input = src.openStream()) {

            OutputStream output;
            var filename = Path.of(src.getFile()).getFileName();

            var distribution = new Distribution(getProject().getObjects(), getDistributionName());
            var dst = distribution.getDistributionDir().resolve(filename.toString()).toFile();

            dst.getParentFile().mkdirs();

            if (!dst.exists()) {
                try {
                    output = new FileOutputStream(dst);
                } catch (FileNotFoundException e) {
                    throw new GradleScriptException("Datei nicht geschrieben werden", e);
                }

                byte[] buffer = new byte[1444];
                int byteRead;
                int byteSum = 0;
                while ((byteRead = input.read(buffer)) != -1) {
                    byteSum += byteRead;
                    output.write(buffer, 0, byteRead);
                    progressLogger.progress(String.format("%d MB", byteSum / (1024 * 1024)));
                }

                output.close();
            }
        }

        progressLogger.completed();
    }
}
