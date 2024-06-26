package br.jessicaoliveira.rest.tests.refac;

import br.jessicaoliveira.rest.core.BaseTest;
import br.jessicaoliveira.rest.utils.BarrigaUtils;
import io.restassured.http.ContentType;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

public class SaldoTest extends BaseTest {

    @Test
    public void deveCalcularSaldoContas() {
        Integer CONTA_ID = BarrigaUtils.getIdContaPeloNome("Conta para saldo");
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/saldo")
                .then()
                .statusCode(200)
                .body("find{it.conta_id == " + CONTA_ID + "}.saldo", is("534.00"));
    }

}
