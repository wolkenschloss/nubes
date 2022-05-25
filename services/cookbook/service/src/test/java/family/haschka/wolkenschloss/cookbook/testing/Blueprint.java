package family.haschka.wolkenschloss.cookbook.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {ElementType.TYPE, ElementType.METHOD})
@Retention(value= RetentionPolicy.SOURCE)
public @interface Blueprint {
    String[] tags();
}
