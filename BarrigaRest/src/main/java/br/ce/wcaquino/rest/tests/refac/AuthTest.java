package br.ce.wcaquino.rest.tests.refac;

import static io.restassured.RestAssured.given;

import org.junit.Test;

import br.ce.wcaquino.rest.core.BaseTest;
import io.restassured.RestAssured;
import io.restassured.specification.FilterableRequestSpecification;

public class AuthTest extends BaseTest {
	
	@Test
	public void naoDeveAcessarAPISemToken() {
		FilterableRequestSpecification req = (FilterableRequestSpecification) RestAssured.requestSpecification;
		req.removeHeader("Authorization");
		//Retirando o token JWT porque este � o �nico teste no qual ele n�o � necess�rio
		
		given()
		.when()
			.get("/contas")
		.then()
			.statusCode(401)
		;
	}
}
