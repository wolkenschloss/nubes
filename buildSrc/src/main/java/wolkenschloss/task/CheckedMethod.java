package wolkenschloss.task;

@FunctionalInterface
public interface CheckedMethod<T> {
    T apply() throws Throwable;
}
