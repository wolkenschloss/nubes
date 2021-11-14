package familie.haschka.wolkenschloss.cookbook.testing;

import org.junit.jupiter.api.extension.*;
import org.mockserver.client.MockServerClient;

public class MockServerClientParameterResolver implements Extension, ParameterResolver {
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == MockServerClient.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {

        var host = System.getProperty(MockServerResource.TESTCLIENT_HOST_CONFIG);
        var port = Integer.parseInt(System.getProperty(MockServerResource.TESTCLIENT_PORT_CONFIG));

        return new MockServerClient(host, port);
    }
}
