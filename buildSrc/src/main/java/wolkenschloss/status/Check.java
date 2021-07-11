package wolkenschloss.status;

import java.util.function.Predicate;

interface Check<T> {
    OkMessage<T> check(Predicate<T> p);
}
