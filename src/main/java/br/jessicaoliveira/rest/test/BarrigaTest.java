package br.jessicaoliveira.rest.test;

import br.jessicaoliveira.rest.core.BaseTest;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
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

    @Test
    public void DeveInserirMovimentacaoSucesso(){
        Movimentacao mov = getMovimentacaoValida();

        given()
                .header("Authorization", "JWT " + TOKEN)
                .contentType(ContentType.JSON)
                .body(mov)
                .when()
                .post("/transacoes")
                .then()
                .log().all()
                .statusCode(201);
    }

    @Test
    public void deveValidarCamposObrigatoriosMovimentacao(){
        given()
                .header("Authorization", "JWT " + TOKEN)
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
    public void naoDeveInserirMovimentacaoComDataFutura(){
        Movimentacao mov = getMovimentacaoValida();
        mov.setData_transacao("23/06/2024");
        given()
                .header("Authorization", "JWT " + TOKEN)
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
    public void naoDeveRemoverContaComMovimentacao(){
        given()
                .header("Authorization", "JWT " + TOKEN)
                .contentType(ContentType.JSON)
                .when()
                .delete("/contas/2162994")
                .then()
                .statusCode(500)
                .body("constraint", is("transacoes_conta_id_foreign"));
    }

    @Test
    public void deveCalcularSaldoContas(){
        given()
                .header("Authorization", "JWT " + TOKEN)
                .contentType(ContentType.JSON)
                .when()
                .get("/saldo")
                .then()
                .statusCode(200)
                .body("find{it.conta_id == 2162994}.saldo", is("100.00"));
    }

    //2029589

    @Test
    public void deveRemoverMovimentacao(){
        given()
                .header("Authorization", "JWT " + TOKEN)
                .contentType(ContentType.JSON)
                .when()
                .delete("/transacoes/2029589")
                .then()
                .statusCode(204);
    }

    private Movimentacao getMovimentacaoValida(){
        Movimentacao mov = new Movimentacao();
        mov.setConta_id(2162994);
        mov.setDescricao("Descrição da movimentação");
        mov.setEnvolvido("Envolvido na mov");
        mov.setTipo("REC");
        mov.setData_transacao("01/01/2024");
        mov.setData_pagamento("10/05/2010");
        mov.setValor(100f);
        mov.setStatus(true);

        return mov;
    }

    
}
