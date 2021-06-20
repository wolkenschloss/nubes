package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;

abstract public class DefinePoolTask extends DefaultTask {

    @Inject
    abstract public ExecOperations getExecOperations();

    @Input
    abstract public RegularFileProperty getXmlDescription();

    @TaskAction
    public void exec() {
        var result = getExecOperations().exec(exec -> {
            exec.commandLine("virsh");
            exec.args("pool-define", getXmlDescription().get());
        });

        if (result.getExitValue() != 0) {
            throw new GradleException("Cannot execute virsh define-pool");
        }
    }
}
