package br.ce.wcaquino.rest.tests.refac;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import br.ce.wcaquino.rest.core.BaseTest;
import br.ce.wcaquino.rest.tests.Movimentacao;
import br.ce.wcaquino.rest.tests.refac.suite.BarrigaUtils;
import br.ce.wcaquino.rest.utils.DataUtils;

public class MovimentacaoTest extends BaseTest {
	
	@Test 
	public void deveInserirMovimentacaoComSucesso() {
		Movimentacao mov = getMovimentacaoValida();
		
		given()
			.body(mov) // Como o Content Type é JSON, este objeto é convertido para JSON
		.when()
			.post("/transacoes")
		.then()
			.statusCode(201)
		;
	}
	
	@Test 
	public void deveValidarCamposObrigatoriosNaMovimentacao() { 
		given()
			.body("{}")
		.when()
			.post("/transacoes")
		.then()
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
					))
		;
	}
	
	@Test 
	public void naoDeveInserirMovimentacaoComDataFutura() {
		Movimentacao mov = getMovimentacaoValida();
		mov.setData_transacao(DataUtils.getDataDiferencaDias(2));
		
		given()
			.body(mov) // Como o Content Type é JSON, este objeto é convertido para JSON
		.when()
			.post("/transacoes")
		.then()
			.statusCode(400)
			.body("$", hasSize(1))
			.body("msg", hasItem("Data da Movimentação deve ser menor ou igual à data atual"))
			//Usamos "hasItem" porque a mensagem vem dentro de um array
		;
	}
	
	@Test 
	public void naoDeveRemoverContaComMovimentacao() {
		Integer CONTA_ID = BarrigaUtils.getIdContaPeloNome("Conta com movimentacao");
		
		given()
			.pathParam("id", CONTA_ID)
		.when()
			.delete("/contas/{id}")
		.then()
			.statusCode(500)
			.body("constraint", is("transacoes_conta_id_foreign"))
		;
	}
	
	@Test 
	public void deveRemoverMovimentacao() {
		Integer MOV_ID = BarrigaUtils.getIdMovPelaDescricao("Movimentacao para exclusao");
		
		given()
			.pathParam("id", MOV_ID)
		.when()
			.delete("/transacoes/{id}")
		.then()
			.statusCode(204)
		;
	}
	
	
	
	private Movimentacao getMovimentacaoValida() {
		Movimentacao mov = new Movimentacao();
		
		mov.setConta_id(BarrigaUtils.getIdContaPeloNome("Conta para movimentacoes"));
		//mov.setUsuario_id(usuario_id);
		mov.setDescricao("Descricao da movimentacao 2");
		mov.setEnvolvido("Envolvido na movimentacao 2");
		mov.setTipo("DESP");
		mov.setData_transacao(DataUtils.getDataDiferencaDias(-1));
		mov.setData_pagamento(DataUtils.getDataDiferencaDias(5));
		mov.setValor(100f);
		mov.setStatus(true);
		
		return mov;
	}
}
