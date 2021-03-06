package family.haschka.wolkenschloss.cookbook.testing;

import org.jetbrains.annotations.NotNull;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.testcontainers.containers.Network;

record ContainerNetwork(String id) implements Network {

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void close() {
    }

    @NotNull
    @Override
    public Statement apply(@NotNull Statement base, @NotNull Description description) {
        throw new NotImplementedException();
    }
}
