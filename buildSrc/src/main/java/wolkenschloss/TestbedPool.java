package wolkenschloss;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

import java.io.Serializable;

abstract public class TestbedPool implements Serializable {
    abstract public Property<String> getName();
//    abstract public DirectoryProperty getDir();
}
