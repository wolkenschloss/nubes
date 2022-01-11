package wolkenschloss;

import com.github.dockerjava.api.model.Mount;
import org.gradle.api.provider.Provider;

public interface Mountable {
    public Provider<Mount> toMount();
}
