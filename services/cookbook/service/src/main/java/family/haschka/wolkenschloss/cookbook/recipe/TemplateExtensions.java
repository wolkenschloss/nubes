package family.haschka.wolkenschloss.cookbook.recipe;

import io.quarkus.qute.TemplateExtension;

@TemplateExtension
public class TemplateExtensions {
    public static String wrap(String text)  {
        return new WrappedString(text).wrap(80);
    }
}
