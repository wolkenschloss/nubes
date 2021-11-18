package family.haschka.wolkenschloss.cookbook.testing;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.mockserver.client.MockServerClient;

public class MockServerClientParameterResolver implements Extension, ParameterResolver {
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        var type = parameterContext.getParameter().getType();

        if (type.equals(MockServerClient.class)) {
            return true;
        }

        if(type.equals(RecipeWebsite.class)) {
            return true;
        }

        return false;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {

        var type = parameterContext.getParameter().getType();
        var host = System.getProperty(MockServerResource.CLIENT_HOST_CONFIG);
        var port = Integer.parseInt(System.getProperty(MockServerResource.CLIENT_PORT_CONFIG));

        var client = new MockServerClient(host, port);

        if (type.equals(MockServerClient.class)) {
            return client;
        }

        if (type.equals(RecipeWebsite.class)) {
            return new RecipeWebsite(client);
        }

        return null;
    }
}
