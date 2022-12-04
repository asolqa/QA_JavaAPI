package lib;

import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class ApiCoreRequests {
    @Step("Make a GET-request with token and auth cookie")
    public Response makeGetRequest(String url, String token, String cookie){
        return given()
                .filter(new AllureRestAssured())
                .header(new Header("x-csrf-token", token))
                .cookie("auth_sid", cookie)
                .get(url)
                .andReturn();
    }

    @Step("Make a GET-request with auth cookie only")
    public Response makeGetRequestWithCookie(String url, String cookie) {
        return given()
                .filter(new AllureRestAssured())
                .cookie("auth_sid", cookie)
                .get(url)
                .andReturn();
    }
    @Step("Make a GET-request with token only")
    public Response makeGetRequestWithToken(String url, String token){
        return given()
                .filter(new AllureRestAssured())
                .header(new Header("x-csrf-token", token))
                .get(url)
                .andReturn();
    }
    @Step("Make a POST-request")
    public Response makePostRequest(String url, Map<String, String> authData){
        return given()
                .filter(new AllureRestAssured())
                .body(authData)
                .post(url)
                .andReturn();
    }

    @Step("Make a PUT-request with authorization")
    public Response makeAuthPutRequest(String url, Map<String, String> editedUserData, String cookie, String token) {
        return given()
                .filter(new AllureRestAssured())
                .header(new Header("x-csrf-token", token))
                .cookie("auth_sid", cookie)
                .body(editedUserData)
                .put(url)
                .andReturn();
    }

    @Step("Make a PUT-request without authorization")
    public Response makeNotAuthPutRequest(String url, Map<String, String> editedUserData){
        return given()
                .filter(new AllureRestAssured())
                .body(editedUserData)
                .put(url)
                .andReturn();
    }

    @Step("Make a DELETE-request")
    public Response makeDeleteRequest(String url, String cookie, String token){
        return given()
                .filter(new AllureRestAssured())
                .cookie("auth_sid", cookie)
                .header("x-csrf-token", token)
                .delete(url)
                .andReturn();
    }

    @Step("Login user")
    public Map<String, String> loginUser(Map<String, String> authData) {
        Response responseGetAuth = makePostRequest("https://playground.learnqa.ru/api/user/login", authData);

        Map<String, String> result = new HashMap<>();
        result.put("x-csrf-token", responseGetAuth.getHeader("x-csrf-token"));
        result.put("auth_sid", responseGetAuth.getCookie("auth_sid"));

        return result;
    }

    @SuppressWarnings("UnusedReturnValue")
    @Step("Create user and return 'id'")
    public int createUser(Map<String, String> registrationData){
        Response responseCreateAuth =
                given()
                .filter(new AllureRestAssured())
                .body(registrationData)
                .post("https://playground.learnqa.ru/api/user")
                .andReturn();

        Assertions.assertResponseCodeEquals(responseCreateAuth, 200);
        return responseCreateAuth.jsonPath().getInt("id");
    }

    @Step("Create very new user and return 'id'")
    public int createUserIfDoesNotExist(Map<String, String> registrationData){
        int testUserId;
        if(!userExist(registrationData)) {
            testUserId = createUser(registrationData);
        } else {
            testUserId = getUserId(registrationData);
        }
        return testUserId;
    }

    @Step("Delete user by id")
    public void deleteUser(Map<String, String> userData, String cookie, String token){
        int userId = getUserId(userData);

        Response deleteResponse = makeDeleteRequest("https://playground.learnqa.ru/api/user/" + userId, cookie, token);
        Assertions.assertResponseCodeEquals(deleteResponse, 200);
    }

    @Step("Delete existing user")
    public void deleteUserIfExists(Map<String, String> userData, String cookie, String token){
        if(userExist(userData)) {
            deleteUser(userData, cookie, token);
        }
    }

    @Step("Check if user exists")
    public boolean userExist(Map<String, String> userData) {
        Response responseGetAuth = makePostRequest("https://playground.learnqa.ru/api/user/login", userData);
        return responseGetAuth.getStatusCode() == 200;
    }

    @Step("Log user in and get id")
    public int getUserId(Map<String, String> userData) {
        Response responseGetAuth = makePostRequest("https://playground.learnqa.ru/api/user/login", userData);
        Assertions.assertResponseCodeEquals(responseGetAuth, 200);
        return responseGetAuth.jsonPath().getInt("user_id");
    }
}
