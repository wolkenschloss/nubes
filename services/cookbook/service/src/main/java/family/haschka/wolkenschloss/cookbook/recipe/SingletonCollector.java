package family.haschka.wolkenschloss.cookbook.recipe;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class SingletonCollector<T> implements Collector<T, SingletonCollector<T>, T> {

    public static <T> SingletonCollector<T> toItem() {
        return new SingletonCollector<T>();
    }

    T item = null;

    @Override
    public Supplier<SingletonCollector<T>> supplier() {
        return () -> this;
    }

    @Override
    public BiConsumer<SingletonCollector<T>, T> accumulator() {
        return (a, b) -> {
            if (a.item == null) {
                a.item = b;
            } else {
                throw new TooManyItemsException();
            }
        };
    }

    @Override
    public BinaryOperator<SingletonCollector<T>> combiner() {
        return (a, b) -> {
            if (a.item == null && b.item != null) {
                return b;
            } else {
                throw new TooManyItemsException();
            }
        };
    }

    @Override
    public Function<SingletonCollector<T>, T> finisher() {
        return container -> {
            if (container.item == null) {
                throw new TooFewItemsException();
            }

            return container.item;
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.UNORDERED);
    }

    public static class TooManyItemsException extends RuntimeException {
    }

    public static class TooFewItemsException extends  RuntimeException {
        public TooFewItemsException() {
            super("Too few items");
        }
    }
}
