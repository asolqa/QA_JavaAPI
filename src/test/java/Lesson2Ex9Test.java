import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lesson2Ex9Test {

    @Test
    public void ex9_errorPath(){
        Response response = RestAssured
                .given()
                .param("login", "someone")
                .param("password", "123")
                .post("https://playground.learnqa.ru/ajax/api/get_secret_password_homework")
                .andReturn();

        String error = response.jsonPath().getString("error");
        Assertions.assertEquals("Wrong data", error);

        int statusCode = response.getStatusCode();
        Assertions.assertEquals(500, statusCode);

        System.out.printf("Status Code = %d \n", statusCode);
        System.out.println(error);
        System.out.println("User doesn't exist!");
    }

    @Test
    public void ex9_successPath() throws IOException{
        List<String> passwords = Files.readAllLines(
                Paths.get("src/test/resources/splashdata_2019.txt").toAbsolutePath());

        for (String password : passwords){
            String authCookie = RestAssured
                    .given()
                    .param("login", "super_admin")
                    .param("password", password)
                    .post("https://playground.learnqa.ru/ajax/api/get_secret_password_homework")
                    .getCookies()
                    .get("auth_cookie");

            String result = RestAssured
                    .given()
                    .cookie("auth_cookie", authCookie)
                    .post("https://playground.learnqa.ru/ajax/api/check_auth_cookie")
                    .getBody()
                    .htmlPath()
                    .getString("body");

            if ("You are authorized".equals(result)){
                System.out.printf("Password = %s is correct \n", password);
                System.out.println(result);
            }
        }
    }
}
