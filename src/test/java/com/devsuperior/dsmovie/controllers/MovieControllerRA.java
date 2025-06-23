package com.devsuperior.dsmovie.controllers;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.devsuperior.dsmovie.tests.TokenUtil;

import io.restassured.http.ContentType;

public class MovieControllerRA {

	private String clientUsername, adminUsername, userPassword;
	private String clientToken, adminToken, invalidToken;
	private Long existingId, nonExistingId;
	private Map<String, Object> postMovieInstance;

	@BeforeEach
	void setUp() throws Exception {
		baseURI = "http://localhost:8080";

		existingId = 1L;
		nonExistingId = 100L;

		// Validando Tokens de login dos usuários 
        clientUsername = "alex@gmail.com";
        adminUsername = "maria@gmail.com";
        userPassword = "123456";

        clientToken = TokenUtil.obtainAccessToken(clientUsername, userPassword);
        adminToken = TokenUtil.obtainAccessToken(adminUsername, userPassword);
        invalidToken = adminToken + "789";

		postMovieInstance = new HashMap<>();
        postMovieInstance.put("title", "New Movie");
        postMovieInstance.put("score", "5.0");
        postMovieInstance.put("count", "0");
        postMovieInstance.put("image", "https://www.themoviedb.org/t/p/w533_and_h300_bestv2/jBJWaqoSCiARWtfV0GlqHrcdidd.jpg");
	}

	/* findAll */
	@Test
	public void findAllShouldReturnOkWhenMovieNoArgumentsGiven() {

		given()
            .get("/movies")
        .then()
            .statusCode(200)
            .body("content.title", hasItems("The Witcher", "O Lobo de Wall Street", "Vingadores: Ultimato"));
	}
	
	@Test
	public void findAllShouldReturnPagedMoviesWhenMovieTitleParamIsNotEmpty() {		

		String movieTitle = "wars";

		given()
            .get("/movies?title={movieTitle}", movieTitle)
        .then()
            .statusCode(200)
			.body("content.id", hasItems(10, 11, 12))
            .body("content.title", hasItems("Star Wars: A Guerra dos Clones"));
	}
	
	@Test
	public void findByIdShouldReturnMovieWhenIdExists() {
		
		given()
            .get("/movies/{id}", existingId)
        .then()
            .statusCode(200)
			.body("id", is(1))
            .body("title", equalTo("The Witcher"))
			.body("score", is(4.5F))
			.body("count", is(2));
	}
	
	@Test
	public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() {

		given()
            .get("/movies/{id}", nonExistingId)
        .then()
            .statusCode(404)
            .body("status", is(404))
			.body("error", equalTo("Recurso não encontrado"));
	}
	
	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndBlankTitle() throws JSONException {	
		
		postMovieInstance.put("title", "a"); // invalid name

		JSONObject newMovie = new JSONObject(postMovieInstance);

		given()
			.header("Content-type", "application/json")
            .header("Authorization", "Bearer " + adminToken)
            .body(newMovie)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
		.when()
            .post("/movies")
        .then()
            .statusCode(422)
            .body("status", is(422))
			.body("error", equalTo("Dados inválidos"))
			.body("errors.message[0]", equalTo("Title must be between 5 and 80 characters"));
	}
	
	@Test
	public void insertShouldReturnForbiddenWhenClientLogged() throws Exception {

		JSONObject newMovie = new JSONObject(postMovieInstance);

		given()
			.header("Content-type", "application/json")
            .header("Authorization", "Bearer " + clientToken)
            .body(newMovie)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
		.when()
            .post("/movies")
        .then()
            .statusCode(403);
	}
	
	@Test
	public void insertShouldReturnUnauthorizedWhenInvalidToken() throws Exception {

		JSONObject newMovie = new JSONObject(postMovieInstance);

		given()
			.header("Content-type", "application/json")
            .header("Authorization", "Bearer " + invalidToken)
            .body(newMovie)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
		.when()
            .post("/movies")
        .then()
            .statusCode(401);
	}
}
