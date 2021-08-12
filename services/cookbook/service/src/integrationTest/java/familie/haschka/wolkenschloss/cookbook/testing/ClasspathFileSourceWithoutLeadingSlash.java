package familie.haschka.wolkenschloss.cookbook.testing;

import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.common.FileSource;

// See https://github.com/tomakehurst/wiremock/issues/504#issuecomment-383869098
public class ClasspathFileSourceWithoutLeadingSlash extends ClasspathFileSource {

    ClasspathFileSourceWithoutLeadingSlash() {
        super("");
    }

    @Override
    public FileSource child(String subDirectoryName) {
        return new ClasspathFileSource(subDirectoryName);
    }
}
