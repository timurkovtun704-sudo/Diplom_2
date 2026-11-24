import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CreateUserTests {
    private String email;
    private String password;
    private String name;
    private String accessToken;
    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.education-services.ru/";
        String unique = UUID.randomUUID().toString().substring(0, 8);
        email = "user_" + unique + "@yandex.ru";
        password = "pass_" + unique;
        name = "Name_" + unique;
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            deleteUser(accessToken);
        }
    }

    @Step("Отправка запроса на регистрацию пользователя")
    private ValidatableResponse registerUser(Map<String, String> body) {
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/auth/register")
                .then();
    }

    @Step("Удаление тестового пользователя")
    private void deleteUser(String token) {
        given()
                .header("Authorization", token)
                .when()
                .delete("/api/auth/user");
    }

    @Test
    @DisplayName("Создание уникального пользователя")
    @Description("Создаётся новый уникальный пользователь со всеми заполненными полями, в ответ на запрос ожидается код статуса: 200; сообщение: success:true. Также проверяется, что accessToken не равен null.")
    public void createUniqueUserSuccess() {
        Map<String, String> body = Map.of("email", email, "password", password, "name", name);
        accessToken = registerUser(body)
                .statusCode(200)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue())
                .extract().path("accessToken");
    }

    @Test
    @DisplayName("Создание пользователя, который уже зарегистрирован")
    @Description("Отправляем запрос на создание уже созданного пользователя. В ответ на запрос ожидается: код статуса: 403; success: false; message: User already exists")
    public void createDuplicateUserReturnsError() {
        Map<String, String> body = Map.of("email", email, "password", password, "name", name);
        accessToken = registerUser(body).extract().path("accessToken");
        registerUser(body)
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    @DisplayName("Создание пользователя без заполнения одного из обязательных полей (email)")
    @Description("Попытка создания пользователя без одного из обязательный полей (email) должна возвращать: код: 403; success: false; message: Email, password and name are required fields")
    public void createUserWithoutEmailReturnsError() {
        Map<String, String> body = new HashMap<>();
        body.put("password", password);
        body.put("name", name);
        registerUser(body)
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без заполнения одного из обязательных полей (name)")
    @Description("Попытка создания пользователя без одного из обязательный полей (name) должна возвращать: код: 403; success: false; message: Email, password and name are required fields")
    public void createUserWithoutNameReturnsError() {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        registerUser(body)
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без заполнения одного из обязательных полей (password)")
    @Description("Попытка создания пользователя без одного из обязательный полей (password) должна возвращать: код: 403; success: false; message: Email, password and name are required fields")
    public void createUserWithoutPasswordReturnsError() {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("name", name);
        registerUser(body)
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }
}

