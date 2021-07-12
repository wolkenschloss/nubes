package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import wolkenschloss.transformation.Transform;

@SuppressWarnings("UnstableApiUsage")
public class TestbedPlugin implements Plugin<Project> {


    public static final String STATUS_TASK_NAME = "status";
    public static final String START_TASK_NAME = "start";
    public static final String DESTROY_TASK_NAME = "destroy";

    public static final String TESTBED_EXTENSION_NAME = "testbed";


    @Override
    public void apply(Project project) {

        TestbedExtension extension = project.getExtensions()
                .create(TESTBED_EXTENSION_NAME, TestbedExtension.class)
                .configure(project);

        var registrar = new Registrar(project, extension);

        var buildDataSourceImage = registrar.getBuildDataSourceImageTaskProvider();
        var buildRootImage = registrar.getBuildRootImageTaskProvider();
        var buildPool = registrar.getBuildPoolTaskProvider(buildDataSourceImage, buildRootImage);
        var buildDomain = registrar.getBuildDomainTaskProvider(buildPool);
        var readKubeConfig = registrar.getCopyKubeConfigTaskProvider(buildDomain);

        project.getTasks().withType(Transform.class).configureEach(
                task -> task.getScope().convention(extension.asPropertyMap(project.getObjects())));

        project.getTasks().register(
                START_TASK_NAME,
                DefaultTask.class,
                task -> {
                    task.setGroup(Registrar.BUILD_GROUP_NAME);
                    task.dependsOn(readKubeConfig);
                });

        project.getTasks().register(
                DESTROY_TASK_NAME,
                Destroy.class,
                task -> {
                    task.setGroup(Registrar.BUILD_GROUP_NAME);
                    task.getPoolOperations().set(extension.getPoolOperations());
                    task.getDomain().convention(extension.getDomain().getName());
                    task.getPoolRunFile().convention(buildPool.get().getPoolRunFile());
                    task.getBuildDir().convention(project.getLayout().getBuildDirectory());
                    task.getDomainOperations().set(extension.getDomainOperations());
                });
    }

}
