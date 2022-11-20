import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class Lesson3Ex11Test {

@Test
public void testCookies(){
    Response response = RestAssured
            .get("https://playground.learnqa.ru/api/homework_cookie")
            .andReturn();

    Map<String, String> cookies = response.getCookies();
    assertEquals(200, response.getStatusCode(), "Unexpected status code");
    assertFalse(cookies.isEmpty(), "Cookies are empty");
    assertTrue(cookies.containsKey("HomeWork"), "Response doesn't have 'HomeWork' cookie");
    assertTrue(cookies.containsValue("hw_value"), "Response doesn't have 'hw_value'");

   }
}
