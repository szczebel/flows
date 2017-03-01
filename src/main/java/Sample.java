import static flows.Flows.*;

public class Sample {

    public static void main(String[] args) {

        when(Sample::somethingIsTrue)
                .then(Sample::printBar)
                .orElse(Sample::printFoo);

        String result = when(somethingIsTrue())
                .thenReturn("Yay")
                .orElse("Nah");

        when(Sample::somethingIsTrue)
                .then(Sample::printBar)
                .orElse(DO_NOTHING);

        when(Sample::somethingIsTrue)
                .then(Sample::printBar)
                .orElseThrow(RuntimeException::new);

        when(true)
                .then(Sample::printFoo)
                .orElseThrowE(new RuntimeException());


        int result1 = when(Sample::somethingIsTrue)
                .thenReturn(Sample::getHighNumber)
                .orElse(Sample::getLowNumber);

        int result2 = when(Sample::somethingIsTrue)
                .thenReturn(Sample::getHighNumber)
                .orElse(0);

        int result3 = when(Sample::somethingIsTrue)
                .thenReturn(Sample::getHighNumber)
                .orElseThrowE(new RuntimeException());

        given("This")
                .when(true)
                .then(System.out::println)
                .orElseThrow(RuntimeException::new);

        given(() -> "Greetings")
                .when(Sample::somethingIsTrue)
                .then(System.out::println)
                .orElse(DO_NOTHING());

        given("Greetings")
                .when(Sample::somethingIsTrue)
                .then(System.out::println)
                .orElseThrow(RuntimeException::new);

        int result4 = given("Greetings")
                .when(Sample::somethingIsTrue)
                .thenReturn(String::hashCode)
                .orElse(String::length);

        int result5 = given("Greetings")
                .when(Sample::somethingIsTrue)
                .thenReturn(String::hashCode)
                .orElse(Sample::getHighNumber);

        int result6 = given("Greetings")
                .when(Sample::somethingIsTrue)
                .thenReturn(String::hashCode)
                .orElse(2);

        int result7 = given("Greetings")
                .when(Sample::somethingIsTrue)
                .thenReturn(String::hashCode)
                .orElseThrow(RuntimeException::new);

        int result8 = given("Greetings")
                .when(Sample::somethingIsTrue)
                .thenReturn(Sample::getHighNumber)
                .orElse(String::hashCode);

        int result9 = given("Greetings")
                .when(Sample::somethingIsTrue)
                .thenReturn(String::hashCode)
                .orElseThrow(RuntimeException::new, "Exception message");

        when(Sample::somethingIsTrue)
                .then(DO_NOTHING)
                .orElseThrow(RuntimeException::new, "Exception message");

        when(Sample::somethingIsTrue)
                .thenThrow(RuntimeException::new, "This was expected");

    }

    private static int getHighNumber() {
        return 1;
    }

    private static int getLowNumber() {
        return -1;
    }

    private static boolean somethingIsTrue() {
        return true;
    }

    private static void printFoo() {
        System.out.println("Foo");
    }

    private static void printBar() {
        System.out.println("Bar");
    }
}
