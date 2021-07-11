package wolkenschloss.status;

import java.util.function.Function;

interface OkMessage<T> {
    ErrorMessage<T> ok(Function<T, String> message);
}
