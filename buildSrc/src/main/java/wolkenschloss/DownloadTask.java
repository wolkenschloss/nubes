package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.GradleScriptException;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.*;
import java.net.URL;

@CacheableTask
abstract public class DownloadTask extends DefaultTask {

    public DownloadTask() {
        getBaseImage().set(getProject().getLayout().getBuildDirectory().file("focal-server-cloudimg-amd64-disk-kvm.img"));
    }

    @Inject
    protected abstract FileSystemOperations getFileSystemOperations();

    @Inject
    protected abstract ExecOperations getExecOperations();

    @Input
    abstract public Property<String> getBaseImageUrl();

    @Input
    abstract public Property<String> getSha256Sum();

    @OutputFile
    abstract public RegularFileProperty getBaseImage();

    @Inject
    protected abstract ProgressLoggerFactory getProgressLoggerFactory();

    @TaskAction
    public void download()  {

        var progressLogger = getProgressLoggerFactory().newOperation(DownloadTask.class);
        progressLogger.start("Download base Image", getName());

        try {
            URL location = new URL(getBaseImageUrl().get());
            InputStream input = location.openStream();
            OutputStream output = new FileOutputStream(getBaseImage().get().getAsFile());

            byte[] buffer = new byte[1444];
            int byteRead;
            int byteSum = 0;
            while((byteRead = input.read(buffer)) != -1) {
                byteSum += byteRead;
                output.write(buffer, 0, byteRead);
                progressLogger.progress(String.format("%d MB", byteSum / (1024 * 1024)));
            }

            input.close();
            output.close();

            progressLogger.completed();

            var stdout = new ByteArrayOutputStream();
            var result = getExecOperations().exec(exec -> {
                exec.commandLine("sha256sum");
                exec.args("-b", getBaseImage().get());
                exec.setStandardOutput(stdout);
            });

            result.getExitValue();
            getLogger().info(stdout.toString());
            var sha256 = stdout.toString().split(" ")[0];

            if(sha256.compareTo(getSha256Sum().get()) != 0) {
                getLogger().error("Checksum error for file {}", getBaseImage().get());
                getLogger().error("Expected checksum: {}", getSha256Sum().get());
                getLogger().error("Actual checksum:   {}", sha256);
                throw new GradleException("Unexpected checksum");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new GradleScriptException("Kann das Basis Image nicht herunterladen", e);
        }
    }
}
