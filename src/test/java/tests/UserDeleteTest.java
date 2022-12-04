package tests;

import io.qameta.allure.*;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;

@Epic("Delete")
@Feature("Delete user cases")
@Links(
        {
                @Link(name = "Task definition", url = "https://software-testing.ru/lms/mod/assign/view.php?id=296127"),
                @Link(name = "API Reference Page", url = "https://playground.learnqa.ru/api/map")
        }
)
public class UserDeleteTest extends BaseTestCase {
    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    @Test
    @Description("This test checks delete some user is impossible")
    @DisplayName("Delete user with id 2")
    public void testDeleteUserWithId2(){
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        Map<String, String> kotovUserAuthData = apiCoreRequests.loginUser(authData);
        String header = kotovUserAuthData.get("x-csrf-token");
        String cookie = kotovUserAuthData.get("auth_sid");

        Response deleteResponse = apiCoreRequests.makeDeleteRequest("https://playground.learnqa.ru/api/user/2",
                cookie, header);

        Assertions.assertResponseCodeEquals(deleteResponse, 400);
        Assertions.assertResponseTextEquals(deleteResponse,"Please, do not delete test users with ID 1, 2, 3, 4 or 5.");
    }

    @Test
    @Description("Positive check to delete auth user")
    @DisplayName("Delete authorized user")
    @Severity(SeverityLevel.CRITICAL)
    public void testDeleteAuthUser(){
        //CREATE TEST USER
        Map<String, String> testUserData = DataGenerator.getRegistrationDataOnTestUser();
        int testUserId = apiCoreRequests.createUserIfDoesNotExist(testUserData);

        //LOGIN UNDER CREATED TEST USER
        Map<String, String> testUserAuthData = apiCoreRequests.loginUser(testUserData);

        String header = testUserAuthData.get("x-csrf-token");
        String cookie = testUserAuthData.get("auth_sid");

        //DELETE
        Response deleteResponse = apiCoreRequests.makeDeleteRequest("https://playground.learnqa.ru/api/user/" + testUserId,
                cookie, header);

        Assertions.assertResponseCodeEquals(deleteResponse, 200);

        //GET
        Response getResponse = apiCoreRequests.makeGetRequest("https://playground.learnqa.ru/api/user/" + testUserId,
                header, cookie);

        Assertions.assertResponseCodeEquals(getResponse, 404);
        Assertions.assertResponseTextEquals(getResponse, "User not found");
    }

    @Test
    @Description("Negative check to delete auth user")
    @DisplayName("Delete other user")
    @Severity(SeverityLevel.CRITICAL)
    public void testOtherUserDeleteTest(){
        //CREATE RANDOM TEST USER 1
        Map<String, String> testUserData1 = DataGenerator.getRegistrationData();
        int testUserId1 = apiCoreRequests.createUser(testUserData1);
        System.out.println("User1 created: " + testUserId1);
        //LOGIN UNDER CREATED TEST USER 1 AND GET DATA
        Map<String, String> testUserAuthData1 = apiCoreRequests.loginUser(testUserData1);

        String header1 = testUserAuthData1.get("x-csrf-token");
        String cookie1 = testUserAuthData1.get("auth_sid");

        //CREATE OTHER TEST USER 2
        Map<String, String> testUserData2 = DataGenerator.getRegistrationData();
        int testUserId2 = apiCoreRequests.createUser(testUserData2);
        System.out.println("User2 created: " + testUserId2);

        //DELETE TEST USER 2 UNDER TEST 1
        Response deleteResponse = apiCoreRequests.makeDeleteRequest("https://playground.learnqa.ru/api/user/" + testUserId2,
                cookie1, header1);

        //GET TEST USER 2 DATA
        Response getResponseUser2 = apiCoreRequests.makeGetRequest("https://playground.learnqa.ru/api/user/" + testUserId2,
                header1, cookie1);

        //GET TEST USER 1 DATA
        Response getResponseUser1 = apiCoreRequests.makeGetRequest("https://playground.learnqa.ru/api/user/" + testUserId1,
                header1, cookie1);

        assertAll(
                () -> Assertions.assertResponseCodeEquals(deleteResponse, 400),
                () -> Assertions.assertResponseCodeEquals(getResponseUser2, 200),
                () -> Assertions.assertResponseCodeEquals(getResponseUser1, 200)
        );
    }
}
