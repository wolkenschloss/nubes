package wolkenschloss;

import org.gradle.api.Action;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Nested;

public interface BaseTestbedExtension {
    RegularFileProperty getSshKeyFile();

    @Nested
    TestbedView getView();

    void view(Action<? super TestbedView> action);

    @Nested
    TestbedDomain getDomain();

    void domain(Action<? super TestbedDomain> action);

    @Nested
    TestbedPool getPool();

    void pool(Action<? super TestbedPool> action);

    Property<String> getRootImageName();

    Property<String> getCidataImageName();

    @Nested
    BaseImage getBaseImage();

    void base(Action<? super BaseImage> action);
}
