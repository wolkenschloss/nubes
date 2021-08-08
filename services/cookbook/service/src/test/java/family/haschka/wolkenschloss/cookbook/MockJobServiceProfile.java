package family.haschka.wolkenschloss.cookbook;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Collections;
import java.util.Set;


public class MockJobServiceProfile implements QuarkusTestProfile {
    @Override
    public Set<Class<?>> getEnabledAlternatives() {
        return Collections.singleton(MockJobService.class);
    }

    @Override
    public String getConfigProfile() {
        return "testnojob";
    }
}
