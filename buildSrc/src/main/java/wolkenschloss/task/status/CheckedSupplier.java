package wolkenschloss.task.status;

@FunctionalInterface
public interface CheckedSupplier<T> {
    T apply() throws Throwable;
}
