package wolkenschloss.task;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.*;
import wolkenschloss.TestbedExtension;

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

    public void initialize(TestbedExtension extension, TaskProvider<Transform> transformNetworkConfig, TaskProvider<Transform> transformUserData) {
        getNetworkConfig().convention(transformNetworkConfig.get().getOutputFile());
        getUserData().convention(transformUserData.get().getOutputFile());
        getCidata().convention(extension.getPoolDirectory().file(extension.getPool().getCidataImageName()));
    }

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
