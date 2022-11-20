import io.restassured.RestAssured;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Lesson3Ex12Test {

    @Test
    public void testHeaders(){
        Response response = RestAssured
                .get("https://playground.learnqa.ru/api/homework_header")
                .andReturn();

        Headers headers = response.getHeaders();
        assertEquals(200, response.getStatusCode(), "Unexpected status code");
        assertTrue(headers.hasHeaderWithName("x-secret-homework-header"), "Response doesn't have 'x-secret-homework-header'");
        assertEquals("Some secret value", headers.get("x-secret-homework-header").getValue(), "Response doesn't have 'Some secret value'");

    }
}
