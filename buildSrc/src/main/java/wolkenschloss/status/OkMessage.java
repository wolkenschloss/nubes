package wolkenschloss.status;

import java.util.function.Function;

interface OkMessage<T> {
    public ErrorMessage<T> ok(Function<T, String> message);
}
