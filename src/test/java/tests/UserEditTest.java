package tests;

import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Epic("Update")
@Feature("Editing user cases")
@Links(
        {
                @Link(name = "Task definition", url = "https://software-testing.ru/lms/mod/assign/view.php?id=296126"),
                @Link(name = "API Reference Page", url = "https://playground.learnqa.ru/api/map")
        }
)
public class UserEditTest extends BaseTestCase {
    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();
    @Test
    @Description("This test is from lesson")
    @DisplayName("Learning: Test edit of just created user")
    public void testEditJustCreatedTest() {
        //GENERATE USER
        Map<String, String> userData = DataGenerator.getRegistrationData();

        JsonPath responseCreateAuth = RestAssured
                .given()
                .body(userData)
                .post("https://playground.learnqa.ru/api/user")
                .jsonPath();
        String userId = responseCreateAuth.getString("id");

        //LOGIN
        Map<String, String> authData = new HashMap<>();
        authData.put("email", userData.get("email"));
        authData.put("password", userData.get("password"));

        Response responseGetAuth = RestAssured
                .given()
                .body(authData)
                .post("https://playground.learnqa.ru/api/user/login")
                .andReturn();

        //EDIT
        String newName = "Changed Name";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        RestAssured
                .given()
                .header("x-csrf-token", this.getHeader(responseGetAuth, "x-csrf-token"))
                .cookie("auth_sid", this.getCookie(responseGetAuth, "auth_sid"))
                .body(editData)
                .put("https://playground.learnqa.ru/api/user/" + userId)
                .andReturn();

        //GET
        Response responseUserData = RestAssured
                .given()
                .header("x-csrf-token", this.getHeader(responseGetAuth, "x-csrf-token"))
                .cookie("auth_sid", this.getCookie(responseGetAuth, "auth_sid"))
                .body(editData)
                .get("https://playground.learnqa.ru/api/user/" + userId)
                .andReturn();

        Assertions.assertJsonByName(responseUserData,"firstName", newName);
    }

    @Test
    @Description("This test checks edit by not auth user")
    @DisplayName("Test un authorize edit")
    @Severity(SeverityLevel.CRITICAL)
    public void testNotAuthEditTest(){
        String newName = "Changed Name";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        Response editResponse = apiCoreRequests.makeNotAuthPutRequest("https://playground.learnqa.ru/api/user/2", editData);

        Assertions.assertResponseCodeEquals(editResponse, 400);
        Assertions.assertResponseTextEquals(editResponse,"Auth token not supplied");
    }

    @Test
    @Description("This test checks edit by auth user")
    @DisplayName("Test editing other user data")
    @Severity(SeverityLevel.CRITICAL)
    public void testOtherUserEditTest() {
        //CREATE RANDOM TEST USER 1
        Map<String, String> testUserData1 = DataGenerator.getRegistrationData();
        int testUserId1 = apiCoreRequests.createUser(testUserData1);
        System.out.println("User1 created: " + testUserId1);

        //LOGIN UNDER CREATED TEST USER 1
        Map<String, String> testUserAuthData1 = apiCoreRequests.loginUser(testUserData1);
        String header1 = testUserAuthData1.get("x-csrf-token");
        String cookie1 = testUserAuthData1.get("auth_sid");

        //CREATE TEST USER 2
        Map<String, String> testUserData2 = DataGenerator.getRegistrationData();
        int testUserId2 = apiCoreRequests.createUser(testUserData2);
        System.out.println("User2 created: " + testUserId2);

        //LOGIN UNDER CREATED TEST USER 2
        Map<String, String> testUserAuthData2 = apiCoreRequests.loginUser(testUserData2);
        String header2 = testUserAuthData2.get("x-csrf-token");
        String cookie2 = testUserAuthData2.get("auth_sid");

        //EDIT TEST USER 2 UNDER TEST USER 1
        String newName = "Changed New Name";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        Response editResponse = apiCoreRequests.makeAuthPutRequest("https://playground.learnqa.ru/api/user/" + testUserId2,
                editData, cookie1, header1);

        //GET USER 1 DATA
        Response getResponseUser1 = apiCoreRequests.makeGetRequest("https://playground.learnqa.ru/api/user/" + testUserId1,
                header1, cookie1);

        //GET USER 2 DATA
        Response getResponseUser2 = apiCoreRequests.makeGetRequest("https://playground.learnqa.ru/api/user/" + testUserId2,
                header2, cookie2);

        assertAll(
                () -> Assertions.assertResponseCodeEquals(editResponse, 400),
                () -> assertNotEquals(getResponseUser1.jsonPath().getString("firstName"),newName),
                () -> assertNotEquals(getResponseUser2.jsonPath().getString("firstName"),newName)
        );
    }

    @Test
    @Description("This test checks editing data by auth user")
    @DisplayName("Test email negative edit")
    public void testSameUserEmailEditTest(){
        //CREATE TEST USER
        Map<String, String> testUserData = DataGenerator.getRegistrationDataOnTestUser();
        int testUserId = apiCoreRequests.createUserIfDoesNotExist(testUserData);

        //LOGIN UNDER CREATED TEST USER
        Map<String, String> testUserAuthData = apiCoreRequests.loginUser(testUserData);

        String header = testUserAuthData.get("x-csrf-token");
        String cookie = testUserAuthData.get("auth_sid");

        //EDIT
        String newEmail = "asolqaexample.com";
        Map<String, String> editData = new HashMap<>();
        editData.put("email", newEmail);

        Response editResponse = apiCoreRequests.makeAuthPutRequest("https://playground.learnqa.ru/api/user/" + testUserId,
                editData, cookie, header);

        Assertions.assertResponseCodeEquals(editResponse, 400);
        Assertions.assertResponseTextEquals(editResponse,"Invalid email format");

    //DELETE TEST USER
        apiCoreRequests.deleteUserIfExists(testUserData, cookie, header);
    }

    @Test
    @Description("This test checks editing data by auth user")
    @DisplayName("Test firstName negative edit")
    public void testSameUserNameEditTest(){
        //CREATE TEST USER
        Map<String, String> testUserData = DataGenerator.getRegistrationDataOnTestUser();
        int testUserId = apiCoreRequests.createUserIfDoesNotExist(testUserData);

        //LOGIN UNDER CREATED TEST USER
        Map<String, String> testUserAuthData = apiCoreRequests.loginUser(testUserData);

        String header = testUserAuthData.get("x-csrf-token");
        String cookie = testUserAuthData.get("auth_sid");

        //EDIT
        String newName = "a";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        Response editResponse = apiCoreRequests.makeAuthPutRequest("https://playground.learnqa.ru/api/user/" + testUserId,
                editData, cookie, header);

        Assertions.assertResponseCodeEquals(editResponse, 400);
        Assertions.assertJsonHasField(editResponse, "error");
        assertEquals(editResponse.jsonPath().getString("error"),"Too short value for field firstName");

        //DELETE TEST USER
        apiCoreRequests.deleteUserIfExists(testUserData, cookie, header);
    }
}
