package wolkenschloss;

import com.github.dockerjava.api.model.Mount;
import com.github.dockerjava.api.model.MountType;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;

import javax.inject.Inject;

public abstract class InputFileMount implements Mountable {

    @Input
    public abstract Property<String> getTarget();

    @InputFile
    public abstract RegularFileProperty getSource();

    @Inject
    public abstract ProviderFactory getProviderFactory();

    public Provider<Mount> toMount() {
        return getProviderFactory().provider(() -> new Mount()
                .withSource(getSource().get().getAsFile().getPath())
                .withTarget(getTarget().get())
                .withType(MountType.BIND)
                .withReadOnly(true));
    }
}