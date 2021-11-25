package family.haschka.wolkenschloss.cookbook;

import io.smallrye.config.ConfigMapping;

import java.util.Optional;

@ConfigMapping(prefix = "vcs")
public interface VersionControlSystem {
    Optional<String> commit();
    Optional<String> ref();
}
