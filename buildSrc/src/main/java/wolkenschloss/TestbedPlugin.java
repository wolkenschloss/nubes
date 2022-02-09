package wolkenschloss;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class TestbedPlugin implements Plugin<Project> {

    public static final String TESTBED_EXTENSION_NAME = "testbed";
    public static final String TESTBED_CLIENT_EXTENSION_NAME = "testbedclient";

    @Override
    public void apply(Project project) {

        TestbedExtension extension = project.getExtensions()
                .create(TESTBED_EXTENSION_NAME, TestbedExtension.class)
                .configure(project);

        TestbedClientExtension tce = project.getExtensions()
                .create(TESTBED_CLIENT_EXTENSION_NAME, TestbedClientExtension.class)
                .configure(project);

        var registrar = new Registrar(project, extension);

        registrar.register();
  }
}
