package wolkenschloss.status;

@FunctionalInterface
interface CheckedSupplier<T> {
    T apply() throws Throwable;
}
