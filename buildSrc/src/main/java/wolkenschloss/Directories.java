package wolkenschloss;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class Directories {

    public static final String APPLICATION_NAME = "wolkenschloss";
    public static final String TESTBED_NAME = "testbed";
    public static final String CERTIFICATE_AUTHORITY_NAME = "ca";

    public static Path getApplicationHome() {
        return getXdgDataHome().resolve(APPLICATION_NAME);
    }

    public static Path getTestbedHome() {
        return getApplicationHome().resolve(TESTBED_NAME);
    }

    public static Path getCertificateAuthorityHome() {
        return getApplicationHome().resolve(CERTIFICATE_AUTHORITY_NAME);
    }

    public static Path getXdgDataHome() {
        return Optional.ofNullable(System.getenv("XDG_DATA_HOME"))
                .map(Paths::get)
                .orElse(Paths.get(System.getProperty("user.home"), ".local", "share"));
    }
}
