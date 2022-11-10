import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;


public class Lesson2Ex7Test {

    @Test
    public void ex7Test() {

        String url = "https://playground.learnqa.ru/api/long_redirect";
        int statusCode = -1;
        int count = 0;
        while (statusCode !=200) {
            Response response = RestAssured
                    .given()
                    .redirects()
                    .follow(false)
                    .when()
                    .get(url)
                    .andReturn();

            statusCode = response.getStatusCode();
            url = statusCode != 200
                    ? response.getHeader("Location")
                    : url;
            count++;

            if (statusCode != 200) {
                System.out.printf("Status Code = %d; Redirect #%d: Location is %s \n", statusCode, count, url);
            }
        }

        System.out.printf("Status Code = %d; Final URL is %s \n", statusCode, url);
    }
}

