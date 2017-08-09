import static fluentconditionals.FluentConditionals.given;

public class IfElseParametrizedGenerics {

    public static void main(String[] args) {
        String message = given(SomeClass::new)
                .when(TestHelper::somethingIsTrue)
                .thenReturn(SomeClass::getMessageForLowNumber)
                .orElse(SomeClass::getMessageForHighNumber);
        System.out.println(message);//I'm so low

        AnotherClass object = given(SomeClass::new)
                .when(TestHelper::somethingIsTrue)
                .thenReturn(TestHelper::extractMessageForHighNumber)
                .orElse(TestHelper::extractMessageForLowNumber);
        System.out.println(object.message);//I'm so high
    }
}
