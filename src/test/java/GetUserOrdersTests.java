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

public class GetUserOrdersTests {
    private String accessToken;

    @Before
    public void setUp()
    {
        RestAssured.baseURI = "https://stellarburgers.education-services.ru/";
        String email = "user_order_test@yandex.ru";
        String password = "user_order_test";
        String name = "login_order_user";
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

    @Step("Получение списка заказов конкретного пользователя с авторизацией")
    private ValidatableResponse getUserOrdersWithAuth()
    {
        return   given()
                .contentType(ContentType.JSON)
                .header("Authorization", accessToken)
                .when()
                .get("/api/orders")
                .then();
    }

    @Step("Получение списка заказов конкретного пользователя без авторизации")
    private ValidatableResponse getUserOrdersWithoutAuth()
    {
        return   given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/orders")
                .then();
    }
    @Test
    @DisplayName("Попытка получить список заказов конкретного пользователя с авторизацией")
    @Description("При попытке получить список заказов конкретного пользователя с авторизацией ожидается получить код ответа: 200; success: true; order: не равен Null")
    public void getOrdersWithAuth()
    {
        getUserOrdersWithAuth()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("orders", notNullValue());
    }

    @Test
    @DisplayName("Попытка получить список заказов конкретного пользователя без авторизации")
    @Description("При попытке получить список заказов конкретного пользователя без авторизацией ожидается получить код ответа: 401; success: false; message: You should be authorised")
    public void getOrdersWithoutAuth()
    {
        getUserOrdersWithoutAuth()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }
}
