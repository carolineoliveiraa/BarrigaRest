package br.jessicaoliveira.rest.test;

import br.jessicaoliveira.rest.core.BaseTest;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

public class BarrigaTest extends BaseTest {

    private String TOKEN;

    @Before
    public void login(){
        Map<String, String> login = new HashMap<>();
        login.put("email", "wagner@aquino");
        login.put("senha", "123456");

        TOKEN = given()
                .contentType(ContentType.JSON)
                .body(login)
                .when()
                .post("/signin")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    @Test
    public void naoDeveAcessarAPISemToken(){
        given()
                .when()
                .get("/contas")
                .then()
                .statusCode(401);
    }

    @Test
    public void deveIncluirContaComSucesso(){
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "JWT " + TOKEN)
                .body("{ \"nome\": \"conta qualquer 1\" }")
                .when()
                .post("/contas")
                .then()
                .statusCode(201);
    }

    @Test
    public void deveAlterarContaComSucesso(){
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "JWT " + TOKEN)
                .body("{ \"nome\": \"conta alterada 08\" }")
                .when()
                .put("/contas/2162994")
                .then()
                .statusCode(200)
                .body("nome", is("conta alterada 08"));
    }

    @Test
    public void naoDeveInserirContaComMesmoNome(){
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "JWT " + TOKEN)
                .body("{ \"nome\": \"conta alterada 08\" }")
                .when()
                .post("/contas")
                .then()
                .log().all()
                .statusCode(400)
                .body("error", is("Já existe uma conta com esse nome!"));
    }
}
