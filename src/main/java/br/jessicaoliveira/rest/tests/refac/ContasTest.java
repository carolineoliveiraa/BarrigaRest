package br.jessicaoliveira.rest.tests.refac;

import br.jessicaoliveira.rest.core.BaseTest;
import br.jessicaoliveira.rest.utils.BarrigaUtils;
import io.restassured.http.ContentType;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

public class ContasTest extends BaseTest {

    @Test
    public void deveIncluirContaComSucesso() {
        given()
                .contentType(ContentType.JSON)
                .body("{ \"nome\": \"Conta inserida\" }")
                .when()
                .post("/contas")
                .then()
                .statusCode(201);
    }

    @Test
    public void deveAlterarContaComSucesso() {
        Integer CONTA_ID = BarrigaUtils.getIdContaPeloNome("Conta para alterar");

        given()
                .contentType(ContentType.JSON)
                .body("{ \"nome\": \"Conta alterada\" }")
                .pathParam("id", CONTA_ID)
                .when()
                .put("/contas/{id}")
                .then()
                .statusCode(200)
                .body("nome", is("Conta alterada"));
    }

    @Test
    public void naoDeveInserirContaComMesmoNome() {
        given()
                .contentType(ContentType.JSON)
                .body("{ \"nome\": \"Conta mesmo nome\" }")
                .when()
                .post("/contas")
                .then()
                .log().all()
                .statusCode(400)
                .body("error", is("Já existe uma conta com esse nome!"));
    }

}
