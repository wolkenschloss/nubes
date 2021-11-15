package familie.haschka.wolkenschloss.cookbook.testing;

import io.quarkus.test.common.QuarkusTestResource;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectMongoShell  {
}
