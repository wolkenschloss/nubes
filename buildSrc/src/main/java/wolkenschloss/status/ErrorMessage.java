package wolkenschloss.status;

interface ErrorMessage<T> {
    StatusChecker error(String message);
}
