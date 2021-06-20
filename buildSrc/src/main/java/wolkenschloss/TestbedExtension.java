package wolkenschloss;

import org.gradle.api.Action;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public class TestbedExtension implements BaseTestbedExtension {


    private final DirectoryProperty configDirectory;
    private final DirectoryProperty cloudInitDirectory;
    private final DirectoryProperty poolDirectory;
    private final RegularFileProperty sshKeyFile;

    private final TestbedDomain domain;
    private final TestbedPool pool;
    private final TestbedView view;
    private final Property<String> rootImageName;
    private final Property<String> cidataImageName;


    @Inject
    public TestbedExtension(ObjectFactory objects) {

        this.configDirectory = objects.directoryProperty();
        this.cloudInitDirectory = objects.directoryProperty();
        this.poolDirectory = objects.directoryProperty();

        this.sshKeyFile = objects.fileProperty();
        this.domain = objects.newInstance(TestbedDomain.class);
        this.pool = objects.newInstance(TestbedPool.class);
        this.view = objects.newInstance(TestbedView.class);
        this.rootImageName = objects.property(String.class);
        this.cidataImageName = objects.property(String.class);
    }

    @Override
    public RegularFileProperty getSshKeyFile() {
        return this.sshKeyFile;
    }

    @Override
    public TestbedView getView() {
        return this.view;
    }

    @Override
    public void view(Action<? super TestbedView> action) {
        action.execute(getView());
    }

    @Override
    public TestbedDomain getDomain() {
        return this.domain;
    }

    @Override
    public void domain(Action<? super TestbedDomain> action) {action.execute(getDomain());}

    @Override
    public TestbedPool getPool() {
        return this.pool;
    }

    @Override
    public void pool(Action<? super TestbedPool> action) {action.execute(getPool());}

    public DirectoryProperty getConfigDirectory() {
        return configDirectory;
    }

    public DirectoryProperty getCloudInitDirectory() {
        return cloudInitDirectory;
    }

    public DirectoryProperty getPoolDirectory() {
        return poolDirectory;
    }

    public Property<String> getRootImageName() {
        return rootImageName;
    }

    public Property<String> getCidataImageName() {
        return cidataImageName;
    }
}
