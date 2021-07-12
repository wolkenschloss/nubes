package wolkenschloss.pool;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.services.BuildServiceRegistry;

import java.io.Serializable;

public abstract class PoolExtension implements Serializable {

    public static final String DEFAULT_RUN_FILE_NAME = "root.md5";
    public static final String DEFAULT_POOL_RUN_FILE = "pool.run";

    public void initialize(BuildServiceRegistry sharedServices, DirectoryProperty buildDirectory, DirectoryProperty runDirectory) {
        getRootImageName().convention("root.qcow2");
        getCidataImageName().convention("cidata.img");
        getName().convention("testbed");

        getPoolOperations().set(sharedServices.registerIfAbsent(
                "poolops",
                PoolOperations.class,
                spec -> spec.getParameters().getPoolName().set(getName())));

        getPoolDirectory().set(buildDirectory.dir("pool"));
        getRootImageMd5File().set(runDirectory.file(DEFAULT_RUN_FILE_NAME));
        getPoolRunFile().set(runDirectory.file(DEFAULT_POOL_RUN_FILE));
    }

    public abstract Property<String> getName();

    public abstract Property<String> getRootImageName();

    public abstract Property<String> getCidataImageName();

    public abstract Property<PoolOperations> getPoolOperations();

    abstract public DirectoryProperty getPoolDirectory();

    abstract public RegularFileProperty getRootImageMd5File();

    abstract public RegularFileProperty getPoolRunFile();
}
