package wolkenschloss;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

@SuppressWarnings("UnstableApiUsage")
public class TestbedPlugin implements Plugin<Project> {

    public static final String TESTBED_EXTENSION_NAME = "testbed";

    @Override
    public void apply(Project project) {

        TestbedExtension extension = project.getExtensions()
                .create(TESTBED_EXTENSION_NAME, TestbedExtension.class)
                .configure(project);

        var registrar = new Registrar(project, extension);

        registrar.register();
    }

}
