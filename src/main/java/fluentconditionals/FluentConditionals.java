package fluentconditionals;

import java.util.function.BooleanSupplier;
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

    static When when(BooleanSupplier condition) {
        return new When.Impl(condition);
    }

    static When when(boolean condition) {
        return when(() -> condition);
    }

    static <ParameterType> Given<ParameterType> given(ParameterType parameter) {
        return given(() -> parameter);
    }

    static <ParameterType> Given<ParameterType> given(Supplier<ParameterType> parameter) {
        return new Given.Impl<>(parameter);
    }

    //middle tier -----------------------------------------------------------------------------------------------

    interface When {
        WhenThenExecute then(Runnable action);

        <ReturnType> WhenThenReturn<ReturnType> thenReturn(Supplier<ReturnType> supplier);
        default <ReturnType> WhenThenReturn<ReturnType> thenReturn(ReturnType value) {
            return thenReturn(() -> value);
        }

        <ExceptionType extends Throwable> void thenThrow(Function<String, ExceptionType> exceptionFactory, String exceptionMessage) throws ExceptionType;

        class Impl implements When {

            private final BooleanSupplier condition;

            Impl(BooleanSupplier condition) {
                this.condition = condition;
            }

            @Override
            public WhenThenExecute then(Runnable action) {
                return new WhenThenExecute.Impl(condition, action);
            }

            @Override
            public <ReturnType> WhenThenReturn<ReturnType> thenReturn(Supplier<ReturnType> supplier) {
                return new WhenThenReturn.Impl<>(condition, supplier);
            }

            @Override
            public <ExceptionType extends Throwable> void thenThrow(Function<String, ExceptionType> exceptionFactory, String exceptionMessage) throws ExceptionType {
                if(condition.getAsBoolean()) throw exceptionFactory.apply(exceptionMessage);
            }
        }
    }

    interface Given<ParameterType> {
        GivenWhen<ParameterType> when(BooleanSupplier condition);

        default GivenWhen<ParameterType> when(boolean condition) {
            return when(() -> condition);
        }

        class Impl<ParameterType> implements Given<ParameterType> {

            private final Supplier<ParameterType> parameter;

            Impl(Supplier<ParameterType> parameter) {
                this.parameter = parameter;
            }

            @Override
            public GivenWhen<ParameterType> when(BooleanSupplier condition) {
                return new GivenWhen.Impl<>(condition, parameter);
            }
        }
    }

    interface GivenWhen<ParameterType> {
        GivenWhenThenExecute<ParameterType> then(Consumer<ParameterType> consumer);
        <ReturnType> GivenWhenThenReturn<ParameterType, ReturnType> thenReturn(Function<ParameterType, ReturnType> function);
        default <ReturnType> GivenWhenThenReturn<ParameterType, ReturnType> thenReturn(Supplier<ReturnType> supplier) {
            return thenReturn(t -> supplier.get());
        }

        class Impl<ParameterType> implements GivenWhen<ParameterType> {

            private final BooleanSupplier condition;
            private final Supplier<ParameterType> parameter;

            Impl(BooleanSupplier condition, Supplier<ParameterType> parameter) {
                this.condition = condition;
                this.parameter = parameter;
            }

            @Override
            public GivenWhenThenExecute<ParameterType> then(Consumer<ParameterType> consumer) {
                return new GivenWhenThenExecute.Impl<>(condition, consumer, parameter);
            }

            @Override
            public <ReturnType> GivenWhenThenReturn<ParameterType, ReturnType> thenReturn(Function<ParameterType, ReturnType> function) {
                return new GivenWhenThenReturn.Impl<>(condition, function, parameter);
            }
        }
    }

    //conclusions -----------------------------------------------------------------------------------------------

    //base
    interface Throwing {
        default <ExceptionType extends Throwable> void orElseThrow(Function<String, ExceptionType> throwable, String exceptionMessage) throws ExceptionType {
            orElseThrow(() -> throwable.apply(exceptionMessage));
        }
        <ExceptionType extends Throwable> void orElseThrow(Supplier<ExceptionType> throwable) throws ExceptionType;

        default <ExceptionType extends Throwable> void orElseThrowE(ExceptionType throwable) throws ExceptionType {
            orElseThrow(() -> throwable);
        }

        abstract class Impl implements Throwing {

            private final BooleanSupplier condition;

            Impl(BooleanSupplier condition) {
                this.condition = condition;
            }

            @Override
            public <ExceptionType extends Throwable> void orElseThrow(Supplier<ExceptionType> throwable) throws ExceptionType {
                if (condition.getAsBoolean()) happyPath();
                else throw throwable.get();
            }

            void evaluateConditionAndConclude(Runnable negativePath){
                if(condition.getAsBoolean()) happyPath();
                else negativePath.run();
            }

            protected abstract void happyPath();

        }
    }

    interface ReturningOrThrowing<ReturnType> {
        default <ExceptionType extends Throwable> ReturnType orElseThrow(Function<String, ExceptionType> throwable, String exceptionMessage) throws ExceptionType {
            return orElseThrow(() -> throwable.apply(exceptionMessage));
        }

        <ExceptionType extends Throwable> ReturnType orElseThrow(Supplier<ExceptionType> throwable) throws ExceptionType;

        default <ExceptionType extends Throwable> ReturnType orElseThrowE(ExceptionType throwable) throws ExceptionType {
            return orElseThrow(() -> throwable);
        }

        abstract class Impl<ReturnType> implements ReturningOrThrowing<ReturnType> {

            private final BooleanSupplier condition;

            Impl(BooleanSupplier condition) {
                this.condition = condition;
            }

            @Override
            public <ExceptionType extends Throwable> ReturnType orElseThrow(Supplier<ExceptionType> throwable) throws ExceptionType {
                if (condition.getAsBoolean()) return happyPath();
                else throw throwable.get();
            }

            ReturnType evaluateConditionAndConclude(Supplier<ReturnType> negativePath) {
                if(condition.getAsBoolean()) return happyPath();
                else return negativePath.get();
            }

            abstract ReturnType happyPath();
        }
    }

    //concrete
    interface WhenThenExecute extends Throwing {
        void orElse(Runnable action);

        class Impl extends Throwing.Impl implements WhenThenExecute {

            private final Runnable action;

            Impl(BooleanSupplier condition, Runnable action) {
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

    interface GivenWhenThenExecute<ParameterType> extends Throwing {
        void orElse(Consumer<ParameterType> elseConsumer);

        class Impl<ParameterType> extends Throwing.Impl implements GivenWhenThenExecute<ParameterType> {

            private final Consumer<ParameterType> consumer;
            private final Supplier<ParameterType> parameter;

            Impl(BooleanSupplier condition, Consumer<ParameterType> consumer, Supplier<ParameterType> parameter) {
                super(condition);
                this.consumer = consumer;
                this.parameter = parameter;
            }

            @Override
            public void orElse(Consumer<ParameterType> elseConsumer) {
                evaluateConditionAndConclude(() -> elseConsumer.accept(parameter.get()));
            }

            @Override
            protected void happyPath() {
                consumer.accept(parameter.get());
            }
        }
    }

    interface WhenThenReturn<ReturnType> extends ReturningOrThrowing<ReturnType> {

        ReturnType orElse(Supplier<ReturnType> elseSupplier);

        default ReturnType orElse(ReturnType elseValue) {
            return orElse(() -> elseValue);
        }

        class Impl<ReturnType> extends ReturningOrThrowing.Impl<ReturnType> implements WhenThenReturn<ReturnType> {

            private final Supplier<ReturnType> supplier;

            Impl(BooleanSupplier condition, Supplier<ReturnType> supplier) {
                super(condition);
                this.supplier = supplier;
            }

            @Override
            public ReturnType orElse(Supplier<ReturnType> elseSupplier) {
                return evaluateConditionAndConclude(elseSupplier);
            }

            ReturnType happyPath() {
                return supplier.get();
            }
        }

    }

    interface GivenWhenThenReturn<ParameterType, ReturnType> extends ReturningOrThrowing<ReturnType> {
        ReturnType orElse(Function<ParameterType, ReturnType> elseFunction);

        default ReturnType orElse(Supplier<ReturnType> elseSupplier) {
            return orElse(t -> elseSupplier.get());
        }

        default ReturnType orElse(ReturnType value) {
            return orElse(t -> value);
        }

        class Impl<ParameterType, ReturnType> extends ReturningOrThrowing.Impl<ReturnType> implements GivenWhenThenReturn<ParameterType, ReturnType> {

            private final Function<ParameterType, ReturnType> function;
            private final Supplier<ParameterType> parameter;

            Impl(BooleanSupplier condition, Function<ParameterType, ReturnType> function, Supplier<ParameterType> parameter) {
                super(condition);
                this.function = function;
                this.parameter = parameter;
            }

            @Override
            public ReturnType orElse(Function<ParameterType, ReturnType> elseFunction) {
                return evaluateConditionAndConclude(()-> elseFunction.apply(parameter.get()));
            }

            @Override
            ReturnType happyPath() {
                return function.apply(parameter.get());
            }
        }
    }
}
