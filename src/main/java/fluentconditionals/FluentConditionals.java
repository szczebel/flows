package fluentconditionals;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("SameParameterValue")
public interface FluentConditionals {

    //reusable constants ---------------------------------------------------------------------------------------

    Runnable doNothing = () -> {};

    static <T> Consumer<T> doNothing() {
        return t -> {};
    }

    //entry points ----------------------------------------------------------------------------------------------

    static When when(Supplier<Boolean> condition) {
        return new When.Impl(condition);
    }

    static When when(boolean condition) {
        return when(() -> condition);
    }

    static <T> Given<T> given(T parameter) {
        return given(() -> parameter);
    }

    static <T> Given<T> given(Supplier<T> parameter) {
        return new Given.Impl<>(parameter);
    }

    //middle tier -----------------------------------------------------------------------------------------------

    interface When {
        WhenThenExecute then(Runnable action);

        <T> WhenThenReturn<T> thenReturn(Supplier<T> supplier);
        default <T> WhenThenReturn<T> thenReturn(T value) {
            return thenReturn(() -> value);
        }

        <Ex extends Throwable> void thenThrow(Function<String, Ex> exceptionFactory, String exceptionMessage) throws Ex;

        class Impl implements When {

            private final Supplier<Boolean> condition;

            Impl(Supplier<Boolean> condition) {
                this.condition = condition;
            }

            @Override
            public WhenThenExecute then(Runnable action) {
                return new WhenThenExecute.Impl(condition, action);
            }

            @Override
            public <T> WhenThenReturn<T> thenReturn(Supplier<T> supplier) {
                return new WhenThenReturn.Impl<>(condition, supplier);
            }

            @Override
            public <Ex extends Throwable> void thenThrow(Function<String, Ex> exceptionFactory, String exceptionMessage) throws Ex {
                if(condition.get()) throw exceptionFactory.apply(exceptionMessage);
            }
        }
    }

    interface Given<T> {
        GivenWhen<T> when(Supplier<Boolean> condition);

        default GivenWhen<T> when(boolean condition) {
            return when(() -> condition);
        }

        class Impl<T> implements Given<T> {

            private final Supplier<T> parameter;

            Impl(Supplier<T> parameter) {
                this.parameter = parameter;
            }

            @Override
            public GivenWhen<T> when(Supplier<Boolean> condition) {
                return new GivenWhen.Impl<>(condition, parameter);
            }
        }
    }

    interface GivenWhen<T> {
        GivenWhenThenExecute<T> then(Consumer<T> consumer);
        <R> GivenWhenThenReturn<T, R> thenReturn(Function<T, R> function);
        default <R> GivenWhenThenReturn<T, R> thenReturn(Supplier<R> supplier) {
            return thenReturn(t -> supplier.get());
        }

        class Impl<T> implements GivenWhen<T> {

            private final Supplier<Boolean> condition;
            private final Supplier<T> parameter;

            Impl(Supplier<Boolean> condition, Supplier<T> parameter) {
                this.condition = condition;
                this.parameter = parameter;
            }

            @Override
            public GivenWhenThenExecute<T> then(Consumer<T> consumer) {
                return new GivenWhenThenExecute.Impl<>(condition, consumer, parameter);
            }

            @Override
            public <R> GivenWhenThenReturn<T, R> thenReturn(Function<T, R> function) {
                return new GivenWhenThenReturn.Impl<>(condition, function, parameter);
            }
        }
    }

    //conclusions -----------------------------------------------------------------------------------------------

    //base
    interface Throwing {
        default <Ex extends Throwable> void orElseThrow(Function<String, Ex> throwable, String exceptionMessage) throws Ex {
            orElseThrow(() -> throwable.apply(exceptionMessage));
        }
        <Ex extends Throwable> void orElseThrow(Supplier<Ex> throwable) throws Ex;

        default <Ex extends Throwable> void orElseThrowE(Ex throwable) throws Ex {
            orElseThrow(() -> throwable);
        }

        abstract class Impl implements Throwing {

            private final Supplier<Boolean> condition;

            Impl(Supplier<Boolean> condition) {
                this.condition = condition;
            }

            @Override
            public <Ex extends Throwable> void orElseThrow(Supplier<Ex> throwable) throws Ex {
                if (condition.get()) happyPath();
                else throw throwable.get();
            }

            void evaluateConditionAndConclude(Runnable negativePath){
                if(condition.get()) happyPath();
                else negativePath.run();
            }

            protected abstract void happyPath();

        }
    }

    interface ReturningOrThrowing<R> {
        default <Ex extends Throwable> R orElseThrow(Function<String, Ex> throwable, String exceptionMessage) throws Ex {
            return orElseThrow(() -> throwable.apply(exceptionMessage));
        }

        <Ex extends Throwable> R orElseThrow(Supplier<Ex> throwable) throws Ex;

        default <Ex extends Throwable> R orElseThrowE(Ex throwable) throws Ex {
            return orElseThrow(() -> throwable);
        }

        abstract class Impl<R> implements ReturningOrThrowing<R> {

            private final Supplier<Boolean> condition;

            Impl(Supplier<Boolean> condition) {
                this.condition = condition;
            }

            @Override
            public <Ex extends Throwable> R orElseThrow(Supplier<Ex> throwable) throws Ex {
                if (condition.get()) return happyPath();
                else throw throwable.get();
            }

            R evaluateConditionAndConclude(Supplier<R> negativePath) {
                if(condition.get()) return happyPath();
                else return negativePath.get();
            }

            abstract R happyPath();
        }
    }

    //concrete
    interface WhenThenExecute extends Throwing {
        void orElse(Runnable action);

        class Impl extends Throwing.Impl implements WhenThenExecute {

            private final Runnable action;

            Impl(Supplier<Boolean> condition, Runnable action) {
                super(condition);
                this.action = action;
            }

            @Override
            public void orElse(Runnable elseAction) {
                evaluateConditionAndConclude(elseAction);
            }

            @Override
            protected void happyPath() {
                action.run();
            }
        }
    }

    interface GivenWhenThenExecute<T> extends Throwing {
        void orElse(Consumer<T> elseConsumer);

        class Impl<T> extends Throwing.Impl implements GivenWhenThenExecute<T> {

            private final Consumer<T> consumer;
            private final Supplier<T> parameter;

            Impl(Supplier<Boolean> condition, Consumer<T> consumer, Supplier<T> parameter) {
                super(condition);
                this.consumer = consumer;
                this.parameter = parameter;
            }

            @Override
            public void orElse(Consumer<T> elseConsumer) {
                evaluateConditionAndConclude(() -> elseConsumer.accept(parameter.get()));
            }

            @Override
            protected void happyPath() {
                consumer.accept(parameter.get());
            }
        }
    }

    interface WhenThenReturn<T> extends ReturningOrThrowing<T> {

        T orElse(Supplier<T> elseSupplier);

        default T orElse(T elseValue) {
            return orElse(() -> elseValue);
        }

        class Impl<T> extends ReturningOrThrowing.Impl<T> implements WhenThenReturn<T> {

            private final Supplier<T> supplier;

            Impl(Supplier<Boolean> condition, Supplier<T> supplier) {
                super(condition);
                this.supplier = supplier;
            }

            @Override
            public T orElse(Supplier<T> elseSupplier) {
                return evaluateConditionAndConclude(elseSupplier);
            }

            T happyPath() {
                return supplier.get();
            }
        }

    }

    interface GivenWhenThenReturn<T, R> extends ReturningOrThrowing<R> {
        R orElse(Function<T, R> elseFunction);

        default R orElse(Supplier<R> elseSupplier) {
            return orElse(t -> elseSupplier.get());
        }

        default R orElse(R value) {
            return orElse(t -> value);
        }

        class Impl<T,R> extends ReturningOrThrowing.Impl<R> implements GivenWhenThenReturn<T, R> {

            private final Function<T, R> function;
            private final Supplier<T> parameter;

            Impl(Supplier<Boolean> condition, Function<T, R> function, Supplier<T> parameter) {
                super(condition);
                this.function = function;
                this.parameter = parameter;
            }

            @Override
            public R orElse(Function<T, R> elseFunction) {
                return evaluateConditionAndConclude(()-> elseFunction.apply(parameter.get()));
            }

            @Override
            R happyPath() {
                return function.apply(parameter.get());
            }
        }
    }
}
