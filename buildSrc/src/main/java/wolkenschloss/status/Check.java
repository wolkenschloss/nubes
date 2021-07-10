package wolkenschloss.status;

import java.util.function.Predicate;

public interface Check<T> {
    public OkMessage<T> check(Predicate<T> p);
}
