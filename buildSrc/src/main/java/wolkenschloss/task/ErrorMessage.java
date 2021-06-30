package wolkenschloss.task;

interface ErrorMessage<T> {
    StatusChecker<T> error(String message);
}
