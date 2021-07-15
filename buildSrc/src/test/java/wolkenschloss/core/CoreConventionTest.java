package wolkenschloss.core;

import org.gradle.api.Project;
import org.gradle.api.tasks.testing.junitplatform.JUnitPlatformOptions;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class CoreConventionTest {

    @Test
    public void coreConventionExists() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("wolkenschloss.core-conventions");

        Assertions.assertTrue(project.getPluginManager().hasPlugin("wolkenschloss.core-conventions"));
    }

    @Test
    public void shouldProvideJavaLibraryPlugin() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("wolkenschloss.core-conventions");

        Assertions.assertTrue(project.getPluginManager().hasPlugin("java-library"));
    }

    @Test
    public void shouldUseJunitPlatformForAllTestTasks() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("wolkenschloss.core-conventions");

        project.getTasks().withType(org.gradle.api.tasks.testing.Test.class, task -> {
            System.out.println(task.getTestFramework());
            Assertions.assertTrue(task.getTestFramework() instanceof JUnitPlatformOptions);
        });
    }
}
