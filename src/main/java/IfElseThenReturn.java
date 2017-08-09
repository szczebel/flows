import static fluentconditionals.FluentConditionals.*;

//Task 3
public class IfElseThenReturn {

    public static void main(String[] args) {
        int result1 = when(TestHelper::somethingIsTrue)
                .thenReturn(TestHelper::getHighNumber)
                .orElse(TestHelper::getLowNumber);
        System.out.println(result1);//1000

        int result2 = when(!TestHelper.somethingIsTrue())
                .thenReturn(TestHelper::getHighNumber)
                .orElse(0);
        System.out.println(result2);//0
    }
}
