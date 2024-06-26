package br.jessicaoliveira.rest.tests.refac;

import br.jessicaoliveira.rest.core.BaseTest;
import br.jessicaoliveira.rest.test.Movimentacao;
import br.jessicaoliveira.rest.utils.BarrigaUtils;
import br.jessicaoliveira.rest.utils.DataUtils;
import io.restassured.http.ContentType;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

public class MovimentacaoTest extends BaseTest {

    @Test
    public void deveInserirMovimentacaoSucesso() {
        Movimentacao mov = getMovimentacaoValida();

                 given()
                .contentType(ContentType.JSON)
                .body(mov)
                .when()
                .post("/transacoes")
                .then()
                .statusCode(201);
    }

    @Test
    public void deveValidarCamposObrigatoriosMovimentacao() {
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
    public void naoDeveInserirMovimentacaoComDataFutura() {
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
    public void naoDeveRemoverContaComMovimentacao() {
        Integer CONTA_ID = BarrigaUtils.getIdContaPeloNome("Conta com movimentacao");

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
    public void deveRemoverMovimentacao() {
        Integer MOV_ID = BarrigaUtils.getIdMovPelaDescricao("Movimentacao para exclusao");
        given()
                .pathParam("id", MOV_ID)
                .contentType(ContentType.JSON)
                .when()
                .delete("/transacoes/{id}")
                .then()
                .statusCode(204);
    }




    private static Movimentacao getMovimentacaoValida() {
        Movimentacao mov = new Movimentacao();
        mov.setConta_id(BarrigaUtils.getIdContaPeloNome("Conta para movimentacoes"));
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
