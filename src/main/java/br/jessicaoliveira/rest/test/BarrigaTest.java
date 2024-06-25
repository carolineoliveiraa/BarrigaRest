package br.jessicaoliveira.rest.test;

import br.jessicaoliveira.rest.core.BaseTest;
import br.jessicaoliveira.rest.utils.DataUtils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.FilterableRequestSpecification;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BarrigaTest extends BaseTest {

    private static String TOKEN;
    private static Integer CONTA_ID;
    private static Integer MOV_ID;
    private static String CONTA_NAME = "Conta" + System.nanoTime();

    @BeforeClass
    public static void login() {
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
                .extract().path("token");

        RestAssured.requestSpecification.header("Authorization", "JWT " + TOKEN);
    }



    @Test
    public void t02_deveIncluirContaComSucesso() {
        CONTA_ID = given()
                .contentType(ContentType.JSON)
                .body("{ \"nome\": \"" + CONTA_NAME + "\" }")
                .when()
                .post("/contas")
                .then()
                .statusCode(201)
                .extract().path("id");
    }

    @Test
    public void t03_deveAlterarContaComSucesso() {
        given()
                .contentType(ContentType.JSON)
                .body("{ \"nome\": \"" + CONTA_NAME + " alterada 08\" }")
                .pathParam("id", CONTA_ID)
                .when()
                .put("/contas/{id}")
                .then()
                .statusCode(200)
                .body("nome", is("" + CONTA_NAME + " alterada 08"));
    }

    @Test
    public void t04_naoDeveInserirContaComMesmoNome() {
        given()
                .contentType(ContentType.JSON)
                .body("{ \"nome\": \"" + CONTA_NAME + " alterada 08\" }")
                .when()
                .post("/contas")
                .then()
                .log().all()
                .statusCode(400)
                .body("error", is("Já existe uma conta com esse nome!"));
    }

    @Test
    public void t05_DeveInserirMovimentacaoSucesso() {
        Movimentacao mov = getMovimentacaoValida();

        MOV_ID = given()
                .contentType(ContentType.JSON)
                .body(mov)
                .when()
                .post("/transacoes")
                .then()
                .log().all()
                .statusCode(201)
                .extract().path("id");
    }

    @Test
    public void t06_deveValidarCamposObrigatoriosMovimentacao() {
        given()
                .body("{}")
                .when()
                .post("/transacoes")
                .then()
                .log().all()
                .statusCode(400)
                .body("$", hasSize(8))
                .body("msg", hasItems(
                        "Data da Movimentação é obrigatório",
                        "Data do pagamento é obrigatório",
                        "Descrição é obrigatório",
                        "Interessado é obrigatório",
                        "Valor é obrigatório",
                        "Valor deve ser um número",
                        "Conta é obrigatório",
                        "Situação é obrigatório"
                ));
    }

    @Test
    public void t07_naoDeveInserirMovimentacaoComDataFutura() {
        Movimentacao mov = getMovimentacaoValida();
        mov.setData_transacao(DataUtils.getDataDiferencaDias(2));
        given()
                .contentType(ContentType.JSON)
                .body(mov)
                .when()
                .post("/transacoes")
                .then()
                .log().all()
                .statusCode(400)
                .body("$", hasSize(1))
                .body("msg", hasItems("Data da Movimentação deve ser menor ou igual à data atual"));
    }

    @Test
    public void t08_naoDeveRemoverContaComMovimentacao() {
        given()
                .pathParam("id", CONTA_ID)
                .contentType(ContentType.JSON)
                .when()
                .delete("/contas/{id}")
                .then()
                .statusCode(500)
                .body("constraint", is("transacoes_conta_id_foreign"));
    }

    @Test
    public void t09_deveCalcularSaldoContas() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/saldo")
                .then()
                .statusCode(200)
                .body("find{it.conta_id == " + CONTA_ID + "}.saldo", is("100.00"));
    }

    @Test
    public void t10_deveRemoverMovimentacao() {
        given()
                .pathParam("id", MOV_ID)
                .contentType(ContentType.JSON)
                .when()
                .delete("/transacoes/{id}")
                .then()
                .statusCode(204);
    }

    @Test
    public void t11_naoDeveAcessarAPISemToken() {
        FilterableRequestSpecification req = (FilterableRequestSpecification) RestAssured.requestSpecification;
        req.removeHeader("Authorization");
        given()
                .when()
                .get("/contas")
                .then()
                .statusCode(401);
    }

    private static Movimentacao getMovimentacaoValida() {
        Movimentacao mov = new Movimentacao();
        mov.setConta_id(CONTA_ID);
        mov.setDescricao("Descrição da movimentação");
        mov.setEnvolvido("Envolvido na mov");
        mov.setTipo("REC");
        mov.setData_transacao(DataUtils.getDataDiferencaDias(-1));
        mov.setData_pagamento(DataUtils.getDataDiferencaDias(5));
        mov.setValor(100f);
        mov.setStatus(true);

        return mov;
    }
}
