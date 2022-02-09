package wolkenschloss;

import com.github.dockerjava.api.model.Mount;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;


public abstract class Configuration {

    @Inject
    public abstract ObjectFactory getObjectFactory();

    @Inject
    public abstract ProviderFactory getProviderFactory();

    @Nested
    public abstract DomainObjectSet<InputFileMount> getInputs();

    @Nested
    public abstract DomainObjectSet<OutputMount> getOutputs();

    @Internal
    public Provider<List<Mount>> getMounts() {
        return getProviderFactory().provider(() -> {
            var list = getInputs().stream()
                    .map(m -> m.toMount().get())
                    .collect(Collectors.toList());
                    list.addAll(getOutputs().stream()
                            .map(m -> m.toMount().get())
                            .collect(Collectors.toList()));
            return list;
        });
    }

    public void input(Action<? super InputFileMount> action) {

        InputFileMount mount = getObjectFactory().newInstance(InputFileMount.class);
        action.execute(mount);
        getInputs().add(mount);
    }

    public void output(Action<? super OutputMount> action) {
        OutputMount mount = getObjectFactory().newInstance(OutputMount.class);
        action.execute(mount);
        getOutputs().add(mount);
    }
}