import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class Lesson3Ex10Test {
    @ParameterizedTest
    @ValueSource(strings = {"", "Short", "Fifteen symbols", "More than 15 symbols"})
    public void testName(String name) {
        assertTrue(name.length() > 15, "Length is less than 15 symbols!");
    }
}
