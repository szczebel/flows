import static fluentconditionals.FluentConditionals.*;

public class IfElseParametrizedThenReturn {

    public static void main(String[] args) {
        int result1 = given("Greetings")
                .when(TestHelper::somethingIsTrue)
                .thenReturn(String::length)
                .orElse(String::hashCode);
        System.out.println(result1);//9

        int result2 = given(TestHelper::getAString)//"a string"
                .when(TestHelper::somethingIsTrue)
                .thenReturn(TestHelper::getHighNumber)
                .orElse(String::hashCode);
        System.out.println(result2);//1000

        int result3 = given("Greetings")
                .when(!TestHelper.somethingIsTrue())
                .thenReturn(String::length)
                .orElse(666);
        System.out.println(result3);//666

        int result4 = given("Greetings")
                .when(!TestHelper.somethingIsTrue())
                .thenReturn(String::length)
                .orElse(TestHelper::getLowNumber);
        System.out.println(result4);//1

        int result5 = given("Greetings")
                .when(!TestHelper.somethingIsTrue())
                .thenReturn(String::length)
                .orElseThrow(RuntimeException::new);
        //exception thrown
    }
}
