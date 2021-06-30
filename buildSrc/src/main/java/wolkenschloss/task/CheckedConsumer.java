package wolkenschloss.task;

import org.libvirt.LibvirtException;

@FunctionalInterface
public interface CheckedConsumer<T> {

    /**
     * Gets a result.
     *
     * @return a result
     */
    void accept(T t) throws Throwable;
}
