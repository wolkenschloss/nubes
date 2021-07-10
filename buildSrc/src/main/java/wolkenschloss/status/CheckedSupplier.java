package wolkenschloss.status;

@FunctionalInterface
public interface CheckedSupplier<T> {
    T apply() throws Throwable;
}
