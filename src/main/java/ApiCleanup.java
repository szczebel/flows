import static fluentconditionals.FluentConditionals.*;

//Task 10
public class ApiCleanup {

    public static void main(String[] args) {
        //these should not execute anything
        // - unfinished statements (missing orElse/orElseThrow)

        when(ApiCleanup::veryComplexCondition)
                .then(TestHelper::printBar);
        //"Evaluating condition" should NOT be printed
        //"Bar" should NOT be printed

        given(TestHelper::getAString)
                .when(ApiCleanup::veryComplexCondition)
                .then(TestHelper::printFirstChar);
        //"Evaluating condition" should NOT be printed
        //"a" should NOT be printed

        //these should not even compile
        when(true).when(false);
        when(true).orElseThrow(RuntimeException::new);
        when(true).then(TestHelper::printFoo).then(TestHelper::printBar);
        given("a string").given("another");
        given("a string").then(TestHelper::printBar);
        int result1 = when(TestHelper::somethingIsTrue).thenReturn(TestHelper::getHighNumber);
        int result2 = when(true).then(TestHelper::printFoo).orElse(1);
        int result3 = when(true).then(TestHelper::printFoo).orElse(TestHelper::printBar);
    }

    static boolean veryComplexCondition() {
        System.out.println("Evaluating condition");
        return true;
    }
}
