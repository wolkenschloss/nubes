package familie.haschka.wolkenschloss.cookbook.testing;

import io.quarkus.test.common.QuarkusTestResource;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@QuarkusTestResource(MockServerResource.class)
@ExtendWith(MockServerClientParameterResolver.class)
public @interface MockServer {
}
