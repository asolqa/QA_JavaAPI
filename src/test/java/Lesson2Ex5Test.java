import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Test;

public class Lesson2Ex5Test {

    @Test
    public void ex5Test(){
        JsonPath response = RestAssured
                .get("https://playground.learnqa.ru/api/get_json_homework")
                .jsonPath();

        String secondMessage = response.get("messages[1].message");
        if (secondMessage == null){
            System.out.println("There is no such message in response!");
        } else
            System.out.println(secondMessage);
    }
}
