package com.devsuperior.dsmovie.controllers;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.devsuperior.dsmovie.tests.TokenUtil;

import io.restassured.http.ContentType;


public class ScoreControllerRA {

	private String adminUsername, userPassword;
	private String adminToken;
	private Map<String, Object> putScoreInstance;

	@BeforeEach
	void setUp() throws Exception {
		
		baseURI = "http://localhost:8080";

		// Validando Tokens de login dos usuários 
        adminUsername = "maria@gmail.com";
        userPassword = "123456";

        adminToken = TokenUtil.obtainAccessToken(adminUsername, userPassword);

		putScoreInstance = new HashMap<>();
        putScoreInstance.put("movieId", 2);
        putScoreInstance.put("score", 5.0F);
	}
	
	@Test
	public void saveScoreShouldReturnNotFoundWhenMovieIdDoesNotExist() throws Exception {
		
		putScoreInstance.put("movieId", 100);

		JSONObject newScore = new JSONObject(putScoreInstance);

		given()
			.header("Content-type", "application/json")
            .header("Authorization", "Bearer " + adminToken)
            .body(newScore)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
		.when()
            .put("/scores")
        .then()
            .statusCode(404)
            .body("status", is(404))
			.body("error", equalTo("Recurso não encontrado"));
	}
	
	@Test
	public void saveScoreShouldReturnUnprocessableEntityWhenMissingMovieId() throws Exception {

		putScoreInstance.remove("movieId");

		JSONObject newScore = new JSONObject(putScoreInstance);

		given()
			.header("Content-type", "application/json")
            .header("Authorization", "Bearer " + adminToken)
            .body(newScore)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
		.when()
            .put("/scores")
        .then()
            .statusCode(422)
            .body("status", is(422))
			.body("error", equalTo("Dados inválidos"))
			.body("errors.message[0]", equalTo("Required field"));
	}
	
	@Test
	public void saveScoreShouldReturnUnprocessableEntityWhenScoreIsLessThanZero() throws Exception {
		
		putScoreInstance.put("score", -5.0F);

		JSONObject newScore = new JSONObject(putScoreInstance);

		given()
			.header("Content-type", "application/json")
            .header("Authorization", "Bearer " + adminToken)
            .body(newScore)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
		.when()
            .put("/scores")
        .then()
            .statusCode(422)
            .body("status", is(422))
			.body("error", equalTo("Dados inválidos"))
			.body("errors.message[0]", equalTo("Score should be greater than or equal to zero"));
	}
}
