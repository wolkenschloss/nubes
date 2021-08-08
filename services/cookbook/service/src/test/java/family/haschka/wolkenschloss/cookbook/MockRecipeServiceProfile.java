package family.haschka.wolkenschloss.cookbook;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Collections;
import java.util.Set;

public class MockRecipeServiceProfile implements QuarkusTestProfile {
    @Override
    public Set<Class<?>> getEnabledAlternatives() {
        return Collections.singleton(MockRecipeService.class);
    }

    @Override
    public String getConfigProfile() {
        return "testnojob";
    }
}