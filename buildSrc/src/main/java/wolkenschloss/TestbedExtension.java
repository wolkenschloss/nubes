package wolkenschloss;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Nested;
import wolkenschloss.domain.DomainExtension;
import wolkenschloss.download.BaseImageExtension;
import wolkenschloss.pool.PoolExtension;
import wolkenschloss.domain.SecureShellService;
import wolkenschloss.domain.RegistryService;
import wolkenschloss.transformation.TransformationExtension;

import java.util.Map;

public abstract class TestbedExtension  {

    public static final String DEFAULT_KNOWN_HOSTS_FILE_NAME = "known_hosts";
    public static final String DEFAULT_HOSTS_FILE_NAME = "hosts";
    public static final String DEFAULT_KUBE_CONFIG_FILE_NAME = "kubeconfig";

    @SuppressWarnings("UnstableApiUsage")
    public TestbedExtension configure(Project project) {
        // Set build directories
        var layout = project.getLayout();
        var buildDirectory = layout.getBuildDirectory();
        var sharedServices = project.getGradle().getSharedServices();

        getRunDirectory().set(buildDirectory.dir("run"));

        getTransformation().initialize(layout);
        getUser().initialize();
        getHost().initialize();
        getPool().initialize(sharedServices, buildDirectory, getRunDirectory());
        getBaseImage().initialize();

        getDomain().initialize(
                sharedServices,
                getRunDirectory().file(DEFAULT_KNOWN_HOSTS_FILE_NAME),
                getRunDirectory().file(DEFAULT_HOSTS_FILE_NAME),
                getRunDirectory().file(DEFAULT_KUBE_CONFIG_FILE_NAME));

        return this;
    }

    @Nested
    abstract public HostExtension getHost();
    public void host(Action<? super HostExtension> action) {
        action.execute(getHost());
    }

    @Nested
    abstract public UserExtension getUser();
    public void user(Action<? super UserExtension> action) {
        action.execute(getUser());
    }

    @Nested
    abstract public DomainExtension getDomain();
    public void domain(Action<? super DomainExtension> action) {action.execute(getDomain());}

    @Nested
    abstract public PoolExtension getPool();
    public void pool(Action<? super PoolExtension> action) {action.execute(getPool());}

    @Nested
    abstract public BaseImageExtension getBaseImage();
    public void base(Action<? super BaseImageExtension> action) {
        action.execute(getBaseImage());
    }

    @Nested
    abstract public TransformationExtension getTransformation();
    public void transformation(Action<? super TransformationExtension> action) {
        action.execute(getTransformation());
    }

    public Provider<Map<String, Object>> asPropertyMap(ObjectFactory objects) {
        var property = objects.mapProperty(String.class, Object.class);

        property.put("user", getUser().getName());
        property.put("sshKey", getUser().getSshKey());
        property.put("hostname", getDomain().getName());
        property.put("fqdn", getDomain().getFqdn());
        property.put("locale", getDomain().getLocale());

        var callback = objects.mapProperty(String.class, Object.class);
        callback.put("ip", getHost().getHostAddress());
        callback.put("port", getHost().getCallbackPort());
        property.put("callback", callback);

        var disks = objects.mapProperty(String.class, Object.class);
        disks.put("root", getPool().getRootImageName());
        disks.put("cidata", getPool().getCidataImageName());
        property.put("disks", disks);

        var pool = objects.mapProperty(String.class, Object.class);
        pool.put("name", getPool().getName());
        pool.put("directory", getPool().getPoolDirectory());
        property.put("pool", pool);

        return property;
    }

    abstract public DirectoryProperty getRunDirectory();

    abstract public Property<SecureShellService> getSecureShellService();

    abstract public Property<RegistryService> getRegistryService();
}
