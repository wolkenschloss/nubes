package wolkenschloss.pool;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

@CacheableTask
abstract public class CreateDataSourceImage extends Exec {

    public CreateDataSourceImage() {
        commandLine("cloud-localds");
    }

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract public RegularFileProperty getNetworkConfig();

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract public RegularFileProperty getUserData();

    @OutputFile
    abstract public RegularFileProperty getCidata();

    @TaskAction
    @Override
    public void exec() {
        args("--network-config",
                getNetworkConfig().get(),
                getCidata().get(),
                getUserData().get());

        super.exec();
    }
}
