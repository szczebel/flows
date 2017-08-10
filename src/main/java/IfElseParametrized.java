import static fluentconditionals.FluentConditionals.*;

//Task 6
public class IfElseParametrized {

    public static void main(String[] args) {
        given("This")
                .when(true)
                .then(TestHelper::printFirstChar)
                .orElse(TestHelper::printLastChar);
        //'T' printed to console

        given(TestHelper::getAString)//"a string"
                .when(TestHelper::somethingIsTrue)
                .then(TestHelper::printFirstChar)
                .orElse(TestHelper::printLastChar);
        //'a' printed to console

        given(TestHelper::getAString)//"a string"
                .when(!TestHelper.somethingIsTrue())
                .then(TestHelper::printFirstChar)
                .orElse(doNothing());
        //nothing printed

        given(TestHelper::getAString)//"a string"
                .when(!TestHelper.somethingIsTrue())
                .then(TestHelper::printFirstChar)
                .orElseThrow(RuntimeException::new);
        //exception thrown

        given(TestHelper::getAString)//"a string"
                .when(!TestHelper.somethingIsTrue())
                .then(TestHelper::printFirstChar)
                .orElseThrowE(new RuntimeException());
        //exception thrown
    }
}
