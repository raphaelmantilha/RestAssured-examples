package br.ce.wcaquino.rest.tests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import br.ce.wcaquino.rest.core.BaseTest;
import br.ce.wcaquino.rest.utils.DataUtils;
import io.restassured.RestAssured;
import io.restassured.specification.FilterableRequestSpecification;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BarrigaTest extends BaseTest {
	
	private static String CONTA_NAME = "Conta " + System.nanoTime();
	private static Integer CONTA_ID;
	private static Integer MOV_ID;
	//Colocando como static o JUnit não reinicia esta variável entre as execuções dos métodos de teste
	
	
	@BeforeClass
	public static void login() {
		Map<String,String> login = new HashMap<>();
		login.put("email", "raphael.mantilha@gmail.com");
		login.put("senha", "12345");
		
		String TOKEN = given()
			.body(login)
		.when()
			.post("/signin")
		.then()
			.statusCode(200)
			.extract().path("token")
		;
		
		RestAssured.requestSpecification.header("Authorization","JWT "+TOKEN);
		//Assim o JWT é enviado no header de todas as requisições
	}
	
	@Test 
	public void t02_deveIncluirContaComSucesso() {
				
		System.out.println("{\"nome\":\""+CONTA_NAME+"\"}");
		CONTA_ID = given()
			.body("{\"nome\":\""+CONTA_NAME+"\"}")
		.when()
			.post("/contas")
		.then()
			.statusCode(201)
			.extract().path("id")
		;
	}
	
	@Test 
	public void t03_deveAlterarContaComSucesso() {
		
		given()
			.body("{\"nome\":\""+CONTA_NAME+" alterada\"}")
			.pathParam("id", CONTA_ID)
		.when()
			.put("/contas/{id}")
		.then()
			.statusCode(200)
			.body("nome",is(CONTA_NAME+" alterada"))
		;
	}
	
	@Test 
	public void t04_naoDeveInserirContaComMesmoNome() {
		
		given()
			.body("{\"nome\":\""+CONTA_NAME+" alterada\"}")
		.when()
			.post("/contas")
		.then()
			.statusCode(400)
			.body("error", is("Já existe uma conta com esse nome!"))
		;
	}
	
	@Test 
	public void t05_deveInserirMovimentacaoComSucesso() {
		Movimentacao mov = getMovimentacaoValida();
		
		MOV_ID = given()
			.body(mov) // Como o Content Type é JSON, este objeto é convertido para JSON
		.when()
			.post("/transacoes")
		.then()
			.statusCode(201)
			.extract().path("id")
		;
	}
	
	@Test 
	public void t06_deveValidarCamposObrigatoriosNaMovimentacao() { 
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
	public void t07_naoDeveInserirMovimentacaoComDataFutura() {
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
	public void t08_naoDeveRemoverContaComMovimentacao() {
	
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
	public void t09_deveCalcularSaldoContas() {
	
		given()
		.when()
			.get("/saldo")
		.then()
			.statusCode(200)
			.body("find{it.conta_id == "+CONTA_ID+"}.saldo", is("-100.00"))
		;
	}
	
	@Test 
	public void t10_deveRemoverMovimentacao() {
	
		given()
			.pathParam("id", MOV_ID)
		.when()
			.delete("/transacoes/{id}")
		.then()
			.statusCode(204)
		;
	}
	
	@Test
	public void t11_naoDeveAcessarAPISemToken() {
		FilterableRequestSpecification req = (FilterableRequestSpecification) RestAssured.requestSpecification;
		req.removeHeader("Authorization");
		//Retirando o token JWT porque este é o único teste no qual ele não é necessário
		
		given()
		.when()
			.get("/contas")
		.then()
			.statusCode(401)
		;
	}
	
	private Movimentacao getMovimentacaoValida() {
		Movimentacao mov = new Movimentacao();
		
		mov.setConta_id(CONTA_ID);
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
