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
public class LoginUserTests {
    private String email;
    private String password;
    private String accessToken;
    @Before
    public void setUp()
    {
        RestAssured.baseURI = "https://stellarburgers.education-services.ru/";
        email = "user_login_test@yandex.ru";
        password = "user_login_test";
        String name = "login_test_user";
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
    @Step("Логин пользователя")
    private ValidatableResponse loginUser(Map<String, String> body)
    {
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/auth/login")
                .then();
    }

    @Test
    @DisplayName("Логин под существующим пользователем")
    @Description("Запрос на логин под существующим пользователем с заполнением всех полей. Ожидается: код: 200; success: true; accessToken: не Null")
    public void loginWithExistUser()
    {
        email = "user_login_test@yandex.ru";
        password = "user_login_test";
        Map<String, String> body = Map.of("email", email, "password", password);
        accessToken = loginUser(body)
                .statusCode(200)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue())
                .extract().path("accessToken");
    }

    @Test
    @DisplayName("Логин с неверным полем email")
    @Description("Запрос на логин с неверным полем email должен вернуть: код: 401; success: false; message: email or password are incorrect")
    public void incorrectEmailLogin()
    {
        email = "user_login_test_uncorrect@yandex.ru";
        password = "user_login_test";
        Map<String, String> body = Map.of("email", email, "password", password);
        loginUser(body)
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }
    @Test
    @DisplayName("Логин с неверным полем password")
    @Description("Запрос на логин с неверным полем password должен вернуть: код: 401; success: false; message: email or password are incorrect")
    public void incorrectPasswordLogin()
    {
        email = "user_login_test@yandex.ru";
        password = "user_login_test_uncorrect";
        Map<String, String> body = Map.of("email", email, "password", password);
        loginUser(body)
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }
    @Test
    @DisplayName("Логин с неверными полями email и password")
    @Description("Запрос на логин с неверным полем password и неверным полем email должен вернуть: код: 401; success: false; message: email or password are incorrect")
    public void incorrectPasswordAndEmailLogin()
    {
        email = "user_login_test_uncorrect@yandex.ru";
        password = "user_login_test_uncorrect";
        Map<String, String> body = Map.of("email", email, "password", password);
        loginUser(body)
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

}
