package family.haschka.wolkenschloss.cookbook;

import javax.json.bind.JsonbException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DeserializationExceptionMapper  implements ExceptionMapper<JsonbException> {
    @Override
    public Response toResponse(JsonbException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(exception.getMessage())
                .build();
    }
}
