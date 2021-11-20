package family.haschka.wolkenschloss.cookbook;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "project")
public interface Project {
    String version();
    String name();
    String group();
    String sha();
    String ref();
}
