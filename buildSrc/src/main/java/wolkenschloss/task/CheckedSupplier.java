package wolkenschloss.task;

@FunctionalInterface
public interface CheckedSupplier<T> {
    T apply() throws Throwable;
}
