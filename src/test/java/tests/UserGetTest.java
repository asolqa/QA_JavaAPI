package tests;

import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

@Epic("Get")
@Feature("Get user cases")
@Links(
        {
                @Link(name = "Task definition", url = "https://software-testing.ru/lms/mod/assign/view.php?id=296125"),
                @Link(name = "API Reference Page", url = "https://playground.learnqa.ru/api/map")
        }
)
public class UserGetTest extends BaseTestCase {
    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();
    @Test
    @Description("Negative check to read data")
    @DisplayName("Get data by not auth user")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetUserDataNotAuth(){
        Response responseUserData = RestAssured
                .get("https://playground.learnqa.ru/api/user/2")
                .andReturn();

        Assertions.assertJsonHasField(responseUserData, "username");
        Assertions.assertJsonHasNotField(responseUserData, "firstName");
        Assertions.assertJsonHasNotField(responseUserData, "lastName");
        Assertions.assertJsonHasNotField(responseUserData, "email");
    }
    @Test
    @Description("Positive check to read data")
    @DisplayName("Get auth user data")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetUserDetailsAuthAsSameUser(){
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        Response responseGetAuth = RestAssured
                .given()
                .body(authData)
                .post("https://playground.learnqa.ru/api/user/login")
                .andReturn();
        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        Response responseUserData = RestAssured
                .given()
                .header("x-csrf-token", header)
                .cookie("auth_sid", cookie)
                .get("https://playground.learnqa.ru/api/user/2")
                .andReturn();

        String[] expectedFields = {"username", "firstName", "lastName", "email"};
        Assertions.assertJsonHasFields(responseUserData, expectedFields);
    }

    @Test
    @Description("Negative check to read auth user")
    @DisplayName("Get other user data")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetUserDetailsAuthAsOtherUser(){
        //CREATE TEST USER
        Map<String, String> testUserData = DataGenerator.getRegistrationDataOnTestUser();
        int testUserId = apiCoreRequests.createUserIfDoesNotExist(testUserData);

        //LOGIN UNDER CREATED TEST USER AND GET DATA
        Map<String, String> testUserAuthData = apiCoreRequests.loginUser(testUserData);

        String header = testUserAuthData.get("x-csrf-token");
        String cookie = testUserAuthData.get("auth_sid");

        Response testUserDataResponse = apiCoreRequests.makeGetRequest("https://playground.learnqa.ru/api/user/" + testUserId,
                header, cookie);

        String[] expectedFields = {"username", "firstName", "lastName", "email"};
        Assertions.assertJsonHasFields(testUserDataResponse, expectedFields);

        //GET OTHER USER DATA
        Response vinKotovDataResponse = apiCoreRequests.makeGetRequest("https://playground.learnqa.ru/api/user/2", header, cookie);

        Assertions.assertJsonHasField(vinKotovDataResponse, "username");
        Assertions.assertJsonHasNotField(vinKotovDataResponse, "firstName");
        Assertions.assertJsonHasNotField(vinKotovDataResponse, "lastName");
        Assertions.assertJsonHasNotField(vinKotovDataResponse, "email");

        //DELETE TEST USER
        apiCoreRequests.deleteUserIfExists(testUserData, cookie, header);
    }
}
