package family.haschka.wolkenschloss.cookbook.job;

public final class EventBusAddress {
    private static final String PREFIX = "family.haschka.wolkenschloss.cookbook.job";
    public static final String COMPLETED = PREFIX + ".completed";
    public static final String CREATED = PREFIX + ".created";
    public static final String IMPORTED = PREFIX + ".imported";
    public static final String FAILED = PREFIX + ".failed";
}
