package br.jessicaoliveira.rest.refac.suite;

import br.jessicaoliveira.rest.core.BaseTest;
import br.jessicaoliveira.rest.tests.refac.AuthTest;
import br.jessicaoliveira.rest.tests.refac.ContasTest;
import br.jessicaoliveira.rest.tests.refac.MovimentacaoTest;
import br.jessicaoliveira.rest.tests.refac.SaldoTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

@RunWith(org.junit.runners.Suite.class)
@org.junit.runners.Suite.SuiteClasses({
        ContasTest.class,
        MovimentacaoTest.class,
        SaldoTest.class,
        AuthTest.class
})
public class Suite extends BaseTest {
    @BeforeClass
    public static void login() {
        Map<String, String> login = new HashMap<>();
        login.put("email", "wagner@aquino");
        login.put("senha", "123456");

        String TOKEN = given()
                .contentType(ContentType.JSON)
                .body(login)
                .when()
                .post("/signin")
                .then()
                .statusCode(200)
                .extract().path("token");

        RestAssured.requestSpecification.header("Authorization", "JWT " + TOKEN);

        RestAssured.get("/reset").then().statusCode(200);
    }
}
