import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.*;
import java.util.List;
import java.util.Map;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CreateOrderTests {
    private String accessToken;
    private List<String> validIngredients;
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
        List<String> allIngredients = given()
                .when()
                .get("/api/ingredients")
                .then()
                .statusCode(200)
                .extract().path("data._id");
        validIngredients = List.of(allIngredients.get(0), allIngredients.get(1));
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

    @Step("Создание заказа с авторизацией")
    private ValidatableResponse createOrderWithAuth(List<String> ingredients)
    {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", accessToken)
                .body(Map.of("ingredients", ingredients))
                .when()
                .post("/api/orders")
                .then();
    }

    @Step("Создание заказа без авторизации")
    private ValidatableResponse createOrderWithoutAuth(List<String> ingredients)
    {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of("ingredients", ingredients))
                .when()
                .post("/api/orders")
                .then();
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и с ингредиентами")
    @Description("Отправляется запрос на создание заказа под авторизованным пользователем и с ингредиентами. Ожидается код статуса: 200; success: true; order.number не равен Null")
    public void createOrderWithAuthAndWithIngredients()
    {
        createOrderWithAuth(validIngredients)
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без авторизации и с ингредиентами")
    @Description("Отправляется запрос на создание заказа без авторизации и с ингредиентами. Ожидается код статуса: 200; success: true; order.number не равен Null")
    public void createOrderWithoutAuthAndWithIngredients()
    {
        createOrderWithoutAuth(validIngredients)
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа с авторизацией, но без ингредиентов")
    @Description("Отправляется запрос на создание заказа с авторизацией, но без ингредиентов, ожидается ответ с кодом статуса 400; success: false; message: Ingredient ids must be provided")
    public void createOrderWithAuthAndWithoutIngredients(){
        createOrderWithAuth(List.of())
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа без авторизации и без ингредиентов")
    @Description("Отправляется запрос на создание заказа без авторизации и без ингредиентов, ожидается ответ с кодом статуса 400; success: false; message: Ingredient ids must be provided")
    public void createOrderWithoutAuthAndWithoutIngredients(){
        createOrderWithoutAuth(List.of())
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и с неверным хэшем ингредиента")
    @Description("При отправке запроса на создание заказа с авторизацией и с неверным хэшем ингредиента в ответ ожидается код статуса 500")
    public void createOrderWithAuthAndWithIncorrectHashOfIngredient()
    {
        createOrderWithAuth(List.of("incorrectHash"))
                .statusCode(500);
    }

    @Test
    @DisplayName("Создание заказа без авторизации и с неверным хэшем ингредиента")
    @Description("При отправке запроса на создание заказа без авторизации и с неверным хэшем ингредиента в ответ ожидается код статуса 500")
    public void createOrderWithoutAuthAndWithIncorrectHashOfIngredient()
    {
        createOrderWithoutAuth(List.of("incorrectHash"))
                .statusCode(500);
    }
}
