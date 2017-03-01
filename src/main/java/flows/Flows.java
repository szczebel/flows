package flows;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("SameParameterValue")
public class Flows {

    public static final Runnable DO_NOTHING = () -> {};
    public static <T> Consumer<T> DO_NOTHING() {
        return t -> {};
    }

    public static Flow when(Supplier<Boolean> condition) {
        return new Flow.Impl(condition);
    }

    public static Flow when(boolean condition) {
        return when(() -> condition);
    }

    public static <T> Parametrized<T> given(T parameter) {
        return given(() -> parameter);
    }

    public static <T> Parametrized<T> given(Supplier<T> parameter) {
        return new Parametrized.Impl<>(parameter);
    }

    public interface Flow {
        VoidConclusion then(Runnable action);

        <T> SupplierConclusion<T> thenGet(Supplier<T> supplier);

        class Impl implements Flow {

            private final Supplier<Boolean> condition;

            Impl(Supplier<Boolean> condition) {
                this.condition = condition;
            }

            @Override
            public VoidConclusion then(Runnable action) {
                return new VoidConclusion.Impl(condition, action);
            }

            @Override
            public <T> SupplierConclusion<T> thenGet(Supplier<T> supplier) {
                return new SupplierConclusion.Impl<>(condition, supplier);
            }
        }

    }

    public interface Parametrized<T> {
        ParameterFlow<T> when(Supplier<Boolean> condition);

        default ParameterFlow<T> when(boolean condition) {
            return when(() -> condition);
        }

        class Impl<T> implements Parametrized<T> {

            private final Supplier<T> parameter;

            Impl(Supplier<T> parameter) {
                this.parameter = parameter;
            }

            @Override
            public ParameterFlow<T> when(Supplier<Boolean> condition) {
                return new ParameterFlow.Impl<>(condition, parameter);
            }
        }
    }

    public interface ParameterFlow<T> {
        ConsumerConclusion<T> then(Consumer<T> consumer);
        <R> FunctionConclusion<T, R> thenReturn(Function<T, R> function);
        default <R> FunctionConclusion<T, R> thenReturn(Supplier<R> supplier) {
            return thenReturn(t -> supplier.get());
        }

        class Impl<T> implements ParameterFlow<T> {

            private final Supplier<Boolean> condition;
            private final Supplier<T> parameter;

            Impl(Supplier<Boolean> condition, Supplier<T> parameter) {
                this.condition = condition;
                this.parameter = parameter;
            }

            @Override
            public ConsumerConclusion<T> then(Consumer<T> consumer) {
                return new ConsumerConclusion.Impl<>(condition, consumer, parameter);
            }

            @Override
            public <R> FunctionConclusion<T, R> thenReturn(Function<T, R> function) {
                return new FunctionConclusion.Impl<>(condition, function, parameter);
            }
        }
    }

    public interface FunctionConclusion<T, R> extends ReturningConclusion<R> {
        R orElse(Function<T, R> elseFunction);

        default R orElse(Supplier<R> elseSupplier) {
            return orElse(t -> elseSupplier.get());
        }

        default R orElse(R value) {
            return orElse(t -> value);
        }

        class Impl<T,R> extends ReturningConclusion.Impl<R> implements FunctionConclusion<T, R> {

            private final Function<T, R> function;
            private final Supplier<T> parameter;

            Impl(Supplier<Boolean> condition, Function<T, R> function, Supplier<T> parameter) {
                super(condition);
                this.function = function;
                this.parameter = parameter;
            }

            @Override
            public R orElse(Function<T, R> elseFunction) {
                if(condition.get()) return proceed();
                else return elseFunction.apply(parameter.get());
            }

            @Override
            R proceed() {
                return function.apply(parameter.get());
            }
        }
    }

    public interface ConsumerConclusion<T> extends Conclusion {
        void orElse(Consumer<T> elseConsumer);

        class Impl<T> extends Conclusion.Impl implements ConsumerConclusion<T> {

            private final Consumer<T> consumer;
            private final Supplier<T> parameter;

            Impl(Supplier<Boolean> condition, Consumer<T> consumer, Supplier<T> parameter) {
                super(condition);
                this.consumer = consumer;
                this.parameter = parameter;
            }

            @Override
            public void orElse(Consumer<T> elseConsumer) {
                if (condition.get()) proceed();
                else elseConsumer.accept(parameter.get());
            }

            @Override
            protected void proceed() {
                consumer.accept(parameter.get());
            }
        }
    }

    public interface SupplierConclusion<T> extends ReturningConclusion<T> {

        T orElseGet(Supplier<T> elseSupplier);

        default T orElse(T elseValue) {
            return orElseGet(() -> elseValue);
        }

        class Impl<T> extends ReturningConclusion.Impl<T> implements SupplierConclusion<T> {

            private final Supplier<T> supplier;

            Impl(Supplier<Boolean> condition, Supplier<T> supplier) {
                super(condition);
                this.supplier = supplier;
            }

            @Override
            public T orElseGet(Supplier<T> elseSupplier) {
                if (condition.get()) return proceed();
                else return elseSupplier.get();
            }

            T proceed() {
                return supplier.get();
            }
        }

    }

    public interface VoidConclusion extends Conclusion {
        void orElse(Runnable action);

        class Impl extends Conclusion.Impl implements VoidConclusion {

            private final Runnable action;

            Impl(Supplier<Boolean> condition, Runnable action) {
                super(condition);
                this.action = action;
            }

            @Override
            public void orElse(Runnable elseAction) {
                if (condition.get()) proceed();
                else elseAction.run();
            }

            @Override
            protected void proceed() {
                action.run();
            }
        }
    }

    public interface Conclusion {
        <Ex extends Throwable> void orElseThrow(Supplier<Ex> throwable) throws Ex;

        default <Ex extends Throwable> void orElseThrowE(Ex throwable) throws Ex {
            orElseThrow(() -> throwable);
        }

        abstract class Impl implements Conclusion {

            final Supplier<Boolean> condition;

            Impl(Supplier<Boolean> condition) {
                this.condition = condition;
            }

            @Override
            public <Ex extends Throwable> void orElseThrow(Supplier<Ex> throwable) throws Ex {
                if (condition.get()) proceed();
                else throw throwable.get();
            }

            protected abstract void proceed();

        }
    }

    public interface ReturningConclusion<R> {
        <Ex extends Throwable> R orElseThrow(Supplier<Ex> throwable) throws Ex;

        default <Ex extends Throwable> R orElseThrowE(Ex throwable) throws Ex {
            return orElseThrow(() -> throwable);
        }

        abstract class Impl<R> implements ReturningConclusion<R> {

            final Supplier<Boolean> condition;

            Impl(Supplier<Boolean> condition) {
                this.condition = condition;
            }

            @Override
            public <Ex extends Throwable> R orElseThrow(Supplier<Ex> throwable) throws Ex {
                if (condition.get()) return proceed();
                else throw throwable.get();
            }

            abstract R proceed();

        }
    }
}
