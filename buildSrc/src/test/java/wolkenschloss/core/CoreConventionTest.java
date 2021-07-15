package wolkenschloss.core;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class CoreConventionTest {

    @Test
    public void coreConventionExists() {
        Project project = org.gradle.testfixtures.ProjectBuilder.builder().build();
        project.getPluginManager().apply("wolkenschloss.core-conventions");

        project.getPluginManager().hasPlugin("wolkenschloss.core-conventions");
        Assertions.assertTrue(project.getPluginManager().hasPlugin("wolkenschloss.core-conventions"));
    }
}
