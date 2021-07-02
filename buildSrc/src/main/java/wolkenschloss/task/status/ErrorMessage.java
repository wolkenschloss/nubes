package wolkenschloss.task.status;

public interface ErrorMessage<T> {
    public StatusChecker<T> error(String message);
}
