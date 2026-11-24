import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.*;
import java.util.Map;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class PatchUserTests {
    private String email;
    private String password;
    private String name;
    private String accessToken;
    private String emailPatch;
    private String namePatch;
    private String passwordPatch;
    @Before
    public void setUp()
    {
        RestAssured.baseURI = "https://stellarburgers.education-services.ru/";
        email = "user_patch_test@yandex.ru";
        password = "user_patch_test";
        name = "login_patch_user";
        Map<String, String> body = Map.of("email", email, "password", password, "name", name);
        accessToken = regUser(body)
                .statusCode(200)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue())
                .extract().path("accessToken");
    }
    @After
    public void tearDown()
    {
        if(accessToken!=null)
        {
            deleteUser(accessToken);
        }
    }
    @Step("Удаление тестового пользователя")
    private void deleteUser(String token) {
        given()
                .header("Authorization", token)
                .when()
                .delete("/api/auth/user");
    }
    @Step("Регистрация тестового пользователя")
    private ValidatableResponse regUser(Map<String, String> body)
    {
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/auth/register")
                .then();
    }

    @Step("Изменение данных пользователя с авторизацией")
    public ValidatableResponse patchUser(Map<String, String> body)
    {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", accessToken)
                .body(body)
                .when()
                .patch("/api/auth/user")
                .then();
    }
    @Step("Изменение данных пользователя без авторизации")
    public ValidatableResponse patchUserWithoutAuth(Map<String, String> body)
    {
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .patch("/api/auth/user")
                .then();
    }
    @Test
    @DisplayName("Изменение всех данных пользователя с авторизацией")
    @Description("При изменении данных пользователя с авторизацией должен вернуться код: 200; success: true; user.email: заданный email (user_patch_2@yandex.ru); user.name: заданный name (login_patch_user)")
    public void patchUserWithAuth()
    {
        email = "user_patch_test@yandex.ru";
        password = "user_patch_test";
        name = "login_patch_user";
        emailPatch = "user_patch_2@yandex.ru";
        namePatch = "name_patch_2";
        passwordPatch = "user_patch_test_2";
        Map<String, String> bodyPatch = Map.of("email", emailPatch, "password", passwordPatch,  "name", namePatch);
        patchUser(bodyPatch)
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(emailPatch))
                .body("user.name", equalTo(namePatch));
    }

    @Test
    @DisplayName("Изменение только поля email пользователя с авторизацией")
    @Description("При изменении поля Email пользователя с авторизацией должен вернуться код: 200; success: true; user.email: заданный email (user_patch_2@yandex.ru); user.name не должен равняться Null")
    public void patchEmailUserWithAuth()
    {
        email = "user_patch_test@yandex.ru";
        password = "user_patch_test";
        name = "login_patch_user";
        emailPatch = "user_patch_2@yandex.ru";
        Map<String, String> bodyPatch = Map.of("email", emailPatch);
        patchUser(bodyPatch)
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(emailPatch))
                .body("user.name", notNullValue());

    }

    @Test
    @DisplayName("Изменение только поля name пользователя с авторизацией")
    @Description("При изменении поля Name пользователя с авторизацией должен вернуться код: 200; success: true; user.email: не равняется Null; user.name: заданный при патче name (name_patch_2)")
    public void patchNameUserWithAuth()
    {
        email = "user_patch_test@yandex.ru";
        password = "user_patch_test";
        name = "login_patch_user";
        namePatch = "name_patch_2";
        Map<String, String> bodyPatch = Map.of("name", namePatch);
        patchUser(bodyPatch)
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", notNullValue())
                .body("user.name", equalTo(namePatch));
    }

    @Test
    @DisplayName("Изменение только поля password пользователя с авторизацией")
    @Description("При изменении поля password пользователя с авторизацией должен вернуться код: 200; success: true; user.email: не равняется Null; user.name: не равняется Null")
    public void patchPasswordUserWithAuth()
    {
        email = "user_patch_test@yandex.ru";
        password = "user_patch_test";
        name = "login_patch_user";
        passwordPatch = "user_patch_test_2";
        Map<String, String> bodyPatch = Map.of("password", passwordPatch);
        patchUser(bodyPatch)
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", notNullValue())
                .body("user.name", notNullValue());
    }

    @Test
    @DisplayName("Изменение всех данных пользователя без авторизации")
    @Description("При изменении данных пользователя без авторизации должен вернуться код: 401; success: false; message: You should be authorised")
    public void patchUserWithoutAuth()
    {
        emailPatch = "user_patch_2@yandex.ru";
        namePatch = "name_patch_2";
        passwordPatch = "user_patch_test_2";
        Map<String, String> bodyPatch = Map.of("email", emailPatch, "password", passwordPatch, "name", namePatch);
        patchUserWithoutAuth(bodyPatch)
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Test
    @DisplayName("Изменение только поля email пользователя без авторизации")
    @Description("При изменении поля Email пользователя без авторизации должен вернуться код: 401; success: false; message: You should be authorised")
    public void patchEmailUserWithoutAuth()
    {
        emailPatch = "user_patch_2@yandex.ru";
        Map<String, String> bodyPatch = Map.of("email", emailPatch);
        patchUserWithoutAuth(bodyPatch)
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Test
    @DisplayName("Изменение только поля name пользователя без авторизации")
    @Description("При изменении поля Name пользователя без авторизации должен вернуться код: 401; success: false; message: You should be authorised")
    public void patchNameUserWithoutAuth()
    {
        namePatch = "name_patch_2";
        Map<String, String> bodyPatch = Map.of("name", namePatch);
        patchUserWithoutAuth(bodyPatch)
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Test
    @DisplayName("Изменение только поля password пользователя без авторизации")
    @Description("При изменении поля password пользователя без авторизации должен вернуться код: 401; success: false; message: You should be authorised")
    public void patchPasswordUserWithoutAuth()
    {
        passwordPatch = "user_patch_test_2";
        Map<String, String> bodyPatch = Map.of("password", passwordPatch);
        patchUserWithoutAuth(bodyPatch)
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

}
