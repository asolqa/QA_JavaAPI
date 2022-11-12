import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class Lesson2Ex8Test {

    @Test
    public void ex8_errorPath(){
        JsonPath response = RestAssured
                .given()
                .queryParam("token", "123")
                .get("https://playground.learnqa.ru/ajax/api/longtime_job")
                .jsonPath();

        String error = response.get("error");
        Assertions.assertEquals("No job linked to this token", error);
        System.out.println(error);
    }

    @Test
    public void ex8_successPath() throws InterruptedException {
        JsonPath responseOnCreation = RestAssured
                .get("https://playground.learnqa.ru/ajax/api/longtime_job")
                .jsonPath();

        String token = responseOnCreation.get("token");
        int timeout = responseOnCreation.getInt("seconds");
        int partTime = timeout / 2;
        System.out.printf("Job number %s will be completed in %d seconds\n", token, timeout);

        TimeUnit.SECONDS.sleep(partTime);

        JsonPath responseInProcess = RestAssured
                .given()
                .queryParam("token", token)
                .get("https://playground.learnqa.ru/ajax/api/longtime_job")
                .jsonPath();

        String status = responseInProcess.get("status");
        Assertions.assertEquals("Job is NOT ready", status);

        String result = responseInProcess.get("result");
        Assertions.assertNull(result);

        System.out.printf("After %d seconds %s\n", partTime, status);

        TimeUnit.SECONDS.sleep(partTime + 1);

        JsonPath responseCompleted = RestAssured
                .given()
                .queryParam("token", token)
                .get("https://playground.learnqa.ru/ajax/api/longtime_job")
                .jsonPath();

        status = responseCompleted.get("status");
        Assertions.assertEquals("Job is ready", status);

        result = responseCompleted.get("result");
        Assertions.assertNotNull(result);

        System.out.printf("After %d seconds %s with result %s\n", partTime*2+1, status, result);
    }
}
