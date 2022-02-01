import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import wolkenschloss.testing.Fixtures;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

public class JavaDockerPluginTest {
    @Test
    @DisplayName("Java Docker Plugin Test")
    public void doIt(@TempDir Path tempDir) {
        var fixture = new Fixtures("image").clone(tempDir.toFile());

        listFiles(fixture);

        var result = GradleRunner.create()
                .withProjectDir(fixture)
                .withArguments("base", "-i")
                .withPluginClasspath()
                .build();

        System.out.println(result.getOutput());
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.task(":base"));
        Assertions.assertNotNull(result.task(":base").getOutcome());
        Assertions.assertEquals(result.task(":base").getOutcome(), TaskOutcome.SUCCESS);

    }

    private void listFiles(File fixture) {
        if (fixture.isDirectory()) {
            Arrays.asList(fixture.listFiles()).forEach(file -> {
                listFiles(file);
            });
        } else {
            System.out.println(fixture.getAbsolutePath());
        }
    }
}
