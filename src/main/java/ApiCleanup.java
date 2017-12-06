import static fluentconditionals.FluentConditionals.*;

//Task 10
public class ApiCleanup {

    public static void main(String[] args) {
        //these should not execute anything
        // - unfinished statements (missing orElse/orElseThrow)

        when(TestHelper::somethingIsTrue)
                .then(TestHelper::printBar);

        given(TestHelper::getAString)
                .when(TestHelper::somethingIsTrue)
                .then(TestHelper::printFirstChar);

        //these should not even compile
        when(true).when(false);
        when(true).then(TestHelper::printFoo).then(TestHelper::printBar);
        given("a string").given("another");
        given("a string").then(TestHelper::printBar);
        int result1 = when(TestHelper::somethingIsTrue)
                .thenReturn(TestHelper::getHighNumber);

    }
}
