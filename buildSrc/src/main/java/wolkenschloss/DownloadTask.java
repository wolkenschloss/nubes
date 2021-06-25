package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleScriptException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

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

    @OutputDirectory
    abstract public DirectoryProperty getDownloads();

    @Inject
    protected abstract ProgressLoggerFactory getProgressLoggerFactory();

    @TaskAction
    public void download() throws URISyntaxException, IOException {

            downloadFile(getBaseImageUrl());
            downloadFile(getSha256SumsUrl());
            downloadFile(getGpgFileUrl());

            verifySignature();
            verifyChecksum();

//        var file = new File(getBaseImage().getAsFile().get().toPath().toString());
//
//        var success = file.setWritable(false, false)
//                && file.setReadable(true, true)
//                && file.setExecutable(false, false);
//        if (!success)  {
//            throw new GradleException("Die Dateiberechtigungen konnten nicht geändert werden.");
//        }
    }

    private void verifyChecksum() throws IOException {
        var result = getExecOperations().exec(spec -> spec.commandLine("sha256sum")
                .args("--ignore-missing",
                        "--check",
                        getDownloads().get().file("SHA256SUMS").getAsFile().toPath())
                .workingDir(getDownloads().get()));
        result.assertNormalExitValue();
    }


    private void verifySignature() {
        getExecOperations().exec(spec -> {
            spec.commandLine("gpg")
                    .args("--keyid-format", "long", "--verify",
                            getDownloads().get().file("SHA256SUMS.gpg").getAsFile().toPath(),
                            getDownloads().get().file("SHA256SUMS").getAsFile().toPath());
        }).assertNormalExitValue();
    }

    private void downloadFile(URL src) throws IOException {
        var progressLogger = getProgressLoggerFactory().newOperation(DownloadTask.class);

        progressLogger.start("Download base Image", src.getFile());

        InputStream input = src.openStream();
        OutputStream output;

        var filename = Path.of(src.getFile()).getFileName();
        var dst = getDownloads().get().file(filename.toString());
        try {
            output = new FileOutputStream(dst.getAsFile());
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

        input.close();
        output.close();
        progressLogger.completed();
    }
}
