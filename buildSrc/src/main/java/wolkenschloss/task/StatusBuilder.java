package wolkenschloss.task;

import java.util.function.Function;
import java.util.function.Predicate;

class StatusBuilder<T> implements Check<T>, OkMessage<T>, ErrorMessage<T>, StatusChecker<T> {

    private final StatusTask statusTask;
    private final CheckedSupplier<T> fn;
    private Predicate<T> predicate;
    private String errorMessage;
    private Function<T, String> okMessageProducer;

    public StatusBuilder(StatusTask statusTask, CheckedSupplier<T> fn) {
        this.statusTask = statusTask;
        this.fn = fn;
    }

    public OkMessage<T> check(Predicate<T> p) {
        this.predicate = p;
        return this;
    }

    public ErrorMessage<T> ok(Function<T, String> message) {
        this.okMessageProducer = message;
        return this;
    }

    public ErrorMessage<T> ok(String message) {
        this.okMessageProducer = (Void) -> message;
        return this;
    }

    public StatusChecker<T> error(String message) {
        this.errorMessage = message;
        return this;
    }

    public void run(String label) {
        T value = null;
        try {
            value = fn.apply();
        } catch (Throwable throwable) {
            statusTask.getLogger().error(String.format("✗ %-15s: %s", label, throwable.getMessage()));
            return;
        }

        if (predicate.test(value)) {
            statusTask.getLogger().quiet(String.format("✓ %-15s: %s", label, okMessageProducer.apply(value)));
        } else {
            statusTask.getLogger().error(String.format("✗ %-15s: %s", label, errorMessage));
        }
    }
}
