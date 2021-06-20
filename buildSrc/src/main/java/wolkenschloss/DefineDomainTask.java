package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;

abstract public class DefineDomainTask extends DefaultTask {

    @Inject
    abstract public ExecOperations getExecOperations();

    @Input
    abstract public RegularFileProperty getXmlDescription();

    @TaskAction
    public void exec () {
        getLogger().info("Executing 'virsh define {}'", getXmlDescription().get());

        getExecOperations().exec(exec -> {
            exec.commandLine("virsh");
            exec.args("define", getXmlDescription().get());
        });
    }
}
