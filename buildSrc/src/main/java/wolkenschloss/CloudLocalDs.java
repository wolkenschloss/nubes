package wolkenschloss;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

abstract public class CloudLocalDs extends Exec {

    public CloudLocalDs() {
        commandLine("cloud-localds");
    }

    @InputFile
    abstract public RegularFileProperty getNetworkConfig();

    @InputFile
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
