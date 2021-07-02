package wolkenschloss.task;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.*;

@CacheableTask
abstract public class CreateDataSource extends Exec {

    public CreateDataSource() {
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
