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

    static FlowEntry when(Supplier<Boolean> condition) {
        return new FlowEntry.Impl(condition);
    }

    static FlowEntry when(boolean condition) {
        return when(() -> condition);
    }

    static <T> Parametrized<T> given(T parameter) {
        return given(() -> parameter);
    }

    static <T> Parametrized<T> given(Supplier<T> parameter) {
        return new Parametrized.Impl<>(parameter);
    }

    //middle tier -----------------------------------------------------------------------------------------------

    interface FlowEntry {
        VoidConclusion then(Runnable action);

        <T> SupplierConclusion<T> thenReturn(Supplier<T> supplier);
        default <T> SupplierConclusion<T> thenReturn(T value) {
            return thenReturn(() -> value);
        }

        <Ex extends Throwable> void thenThrow(Function<String, Ex> exceptionFactory, String exceptionMessage) throws Ex;

        class Impl implements FlowEntry {

            private final Supplier<Boolean> condition;

            Impl(Supplier<Boolean> condition) {
                this.condition = condition;
            }

            @Override
            public VoidConclusion then(Runnable action) {
                return new VoidConclusion.Impl(condition, action);
            }

            @Override
            public <T> SupplierConclusion<T> thenReturn(Supplier<T> supplier) {
                return new SupplierConclusion.Impl<>(condition, supplier);
            }

            @Override
            public <Ex extends Throwable> void thenThrow(Function<String, Ex> exceptionFactory, String exceptionMessage) throws Ex {
                if(condition.get()) throw exceptionFactory.apply(exceptionMessage);
            }
        }
    }

    interface Parametrized<T> {
        ParametrizedFlowEntry<T> when(Supplier<Boolean> condition);

        default ParametrizedFlowEntry<T> when(boolean condition) {
            return when(() -> condition);
        }

        class Impl<T> implements Parametrized<T> {

            private final Supplier<T> parameter;

            Impl(Supplier<T> parameter) {
                this.parameter = parameter;
            }

            @Override
            public ParametrizedFlowEntry<T> when(Supplier<Boolean> condition) {
                return new ParametrizedFlowEntry.Impl<>(condition, parameter);
            }
        }
    }

    interface ParametrizedFlowEntry<T> {
        ConsumerConclusion<T> then(Consumer<T> consumer);
        <R> FunctionConclusion<T, R> thenReturn(Function<T, R> function);
        default <R> FunctionConclusion<T, R> thenReturn(Supplier<R> supplier) {
            return thenReturn(t -> supplier.get());
        }

        class Impl<T> implements ParametrizedFlowEntry<T> {

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

    //conclusions -----------------------------------------------------------------------------------------------

    interface Conclusion {
        default <Ex extends Throwable> void orElseThrow(Function<String, Ex> throwable, String exceptionMessage) throws Ex {
            orElseThrow(() -> throwable.apply(exceptionMessage));
        }
        <Ex extends Throwable> void orElseThrow(Supplier<Ex> throwable) throws Ex;

        default <Ex extends Throwable> void orElseThrowE(Ex throwable) throws Ex {
            orElseThrow(() -> throwable);
        }

        abstract class Impl implements Conclusion {

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

    interface ReturningConclusion<R> {
        default <Ex extends Throwable> R orElseThrow(Function<String, Ex> throwable, String exceptionMessage) throws Ex {
            return orElseThrow(() -> throwable.apply(exceptionMessage));
        }

        <Ex extends Throwable> R orElseThrow(Supplier<Ex> throwable) throws Ex;

        default <Ex extends Throwable> R orElseThrowE(Ex throwable) throws Ex {
            return orElseThrow(() -> throwable);
        }

        abstract class Impl<R> implements ReturningConclusion<R> {

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


    interface ConsumerConclusion<T> extends Conclusion {
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
                evaluateConditionAndConclude(() -> elseConsumer.accept(parameter.get()));
            }

            @Override
            protected void happyPath() {
                consumer.accept(parameter.get());
            }
        }
    }

    interface VoidConclusion extends Conclusion {
        void orElse(Runnable action);

        class Impl extends Conclusion.Impl implements VoidConclusion {

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


    interface SupplierConclusion<T> extends ReturningConclusion<T> {

        T orElse(Supplier<T> elseSupplier);

        default T orElse(T elseValue) {
            return orElse(() -> elseValue);
        }

        class Impl<T> extends ReturningConclusion.Impl<T> implements SupplierConclusion<T> {

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

    interface FunctionConclusion<T, R> extends ReturningConclusion<R> {
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
                return evaluateConditionAndConclude(()-> elseFunction.apply(parameter.get()));
            }

            @Override
            R happyPath() {
                return function.apply(parameter.get());
            }
        }
    }
}
