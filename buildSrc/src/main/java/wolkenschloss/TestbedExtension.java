package wolkenschloss;

import org.gradle.api.Action;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.Nested;

abstract public class TestbedExtension {

    abstract public RegularFileProperty getSshKeyFile();


    @Nested
    abstract public TestbedView getView();

    public void view(Action<? super TestbedView> action) {
        action.execute(getView());
    }

    @Nested
    abstract public TestbedDomain getDomain();
    public void domain(Action<? super TestbedDomain> action) {action.execute(getDomain());}

    @Nested
    abstract public TestbedPool getPool();
    public void pool(Action<? super TestbedPool> action) {action.execute(getPool());}
}
