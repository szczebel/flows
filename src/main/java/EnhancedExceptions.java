import static fluentconditionals.FluentConditionals.*;

//Task 9
public class EnhancedExceptions {

    public static void main(String[] args) {
        given("Greetings")
                .when(TestHelper::somethingIsTrue)
                .thenReturn(String::hashCode)
                .orElseThrow(RuntimeException::new, "Exception message");

        when(TestHelper::somethingIsTrue)
                .then(doNothing)
                .orElseThrow(RuntimeException::new, "Exception message");

        when(TestHelper::somethingIsTrue)
                .thenThrow(RuntimeException::new, "This was expected");
        //Exception in thread "main" java.lang.RuntimeException: This was expected
    }
}
