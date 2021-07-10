package wolkenschloss.status;

import java.util.function.Function;

public interface OkMessage<T> {
    public ErrorMessage<T> ok(Function<T, String> message);
}
