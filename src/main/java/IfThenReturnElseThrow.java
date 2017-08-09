import static fluentconditionals.FluentConditionals.*;

//Task 4
public class IfThenReturnElseThrow {

    public static void main(String[] args) {

        int result3 = when(TestHelper::somethingIsTrue)
                .thenReturn(TestHelper::getLowNumber)
                .orElseThrowE(new RuntimeException());
        System.out.println(result3);//1

        int result4 = when(!TestHelper.somethingIsTrue())
                .thenReturn(TestHelper::getLowNumber)
                .orElseThrow(RuntimeException::new);
        //exception thrown
    }
}
