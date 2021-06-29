package wolkenschloss;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.internal.impldep.com.google.common.collect.Lists;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecResult;
import wolkenschloss.task.CheckedConsumer;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class SecureShell {
    private final Testbed testbed;
    private final ExecOperations operations;
    private final RegularFileProperty knownHostFile;

    public SecureShell(
            Testbed testbed,
            @SuppressWarnings("CdiInjectionPointsInspection") ExecOperations operations,
            @SuppressWarnings("CdiInjectionPointsInspection") RegularFileProperty knownHostFile) {
        this.testbed = testbed;
        this.operations = operations;
        this.knownHostFile = knownHostFile;
    }

    public CheckedConsumer<CheckedConsumer<ExecResult>> ssh(Object... args) throws Throwable {
//        e.args("-o", String.format("UserKnownHostsFile=%s", getKnownHostsFile().get().getAsFile().getAbsolutePath()),
//                ip, "uname", "-nrom");

        return (CheckedConsumer<ExecResult> consumer) -> {
            testbed.withDomain(domain -> {
                var ip = domain.getTestbedHostAddress();

                var arguments = new Vector<Object>();
                arguments.addAll(Arrays.asList("-o",
                        String.format("UserKnownHostsFile=%s", this.knownHostFile.get().getAsFile().getPath()),
                        ip));
                arguments.addAll(Arrays.asList(args));

                try (var stdout = new ByteArrayOutputStream()) {
                    var result = operations.exec(spec -> {
                        spec.commandLine("ssh")
                                .args(arguments)
                                .setStandardOutput(stdout);
                    }).assertNormalExitValue();
                    consumer.accept(result);
                }
            });
        };
    }
}
