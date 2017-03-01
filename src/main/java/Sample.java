import static flows.Flows.*;

public class Sample {

    public static void main(String[] args) {

        when(true)
                .then(Sample::printHigh)
                .orElseThrowE(new RuntimeException());

        when(Sample::isSomeCondition)
                .then(Sample::printLow)
                .orElse(Sample::printHigh);

        when(Sample::isSomeCondition)
                .then(Sample::printLow)
                .orElse(DO_NOTHING);

        when(Sample::isSomeCondition)
                .then(Sample::printLow)
                .orElseThrow(RuntimeException::new);

        String result = when(isSomeCondition())
                .thenReturn("Yay")
                .orElse("Nah");

        int result1 = when(Sample::isSomeCondition)
                .thenReturn(Sample::getHighNumber)
                .orElse(Sample::getLowNumber);

        int result2 = when(Sample::isSomeCondition)
                .thenReturn(Sample::getHighNumber)
                .orElse(0);

        int result3 = when(Sample::isSomeCondition)
                .thenReturn(Sample::getHighNumber)
                .orElseThrowE(new RuntimeException());

        given("This")
                .when(true)
                .then(System.out::println)
                .orElseThrow(RuntimeException::new);

        given(() -> "Greetings")
                .when(Sample::isSomeCondition)
                .then(System.out::println)
                .orElse(DO_NOTHING());

        given("Greetings")
                .when(Sample::isSomeCondition)
                .then(System.out::println)
                .orElseThrow(RuntimeException::new);

        int result4 = given("Greetings")
                .when(Sample::isSomeCondition)
                .thenReturn(String::hashCode)
                .orElse(String::length);

        int result5 = given("Greetings")
                .when(Sample::isSomeCondition)
                .thenReturn(String::hashCode)
                .orElse(Sample::getHighNumber);

        int result6 = given("Greetings")
                .when(Sample::isSomeCondition)
                .thenReturn(String::hashCode)
                .orElse(2);

        int result7 = given("Greetings")
                .when(Sample::isSomeCondition)
                .thenReturn(String::hashCode)
                .orElseThrow(RuntimeException::new);

        int result8 = given("Greetings")
                .when(Sample::isSomeCondition)
                .thenReturn(Sample::getHighNumber)
                .orElse(String::hashCode);

        int result9 = given("Greetings")
                .when(Sample::isSomeCondition)
                .thenReturn(String::hashCode)
                .orElseThrow(RuntimeException::new, "Exception message");

        when(Sample::isSomeCondition)
                .then(DO_NOTHING)
                .orElseThrow(RuntimeException::new, "Exception message");

        when(Sample::isSomeCondition)
                .thenThrow(RuntimeException::new, "This was expected");

    }

    private static int getHighNumber() {
        return 1;
    }

    private static int getLowNumber() {
        return -1;
    }

    private static boolean isSomeCondition() {
        return true;
    }

    private static void printHigh() {
        System.out.println("high");
    }

    private static void printLow() {
        System.out.println("low");
    }
}
