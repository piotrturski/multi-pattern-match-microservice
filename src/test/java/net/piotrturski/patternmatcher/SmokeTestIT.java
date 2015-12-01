package net.piotrturski.patternmatcher;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import org.hamcrest.Matcher;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import com.googlecode.zohhak.api.TestWith;
import com.googlecode.zohhak.api.runners.ZohhakRunner;

import net.piotrturski.patternmatcher.Application;
import net.piotrturski.patternmatcher.multimatch.FinderController;

@RunWith(ZohhakRunner.class)
@SpringApplicationConfiguration(classes=Application.class)
@WebIntegrationTest("server.port:0")
public class SmokeTestIT {

	@ClassRule public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
	@Rule public final SpringMethodRule springMethodRule = new SpringMethodRule();
	
	@Value("${local.server.port}")
    int port;
	
	@Test
	public void should_return_found_matches() {
		makeRequestAndAssertResponse(APPLICATION_JSON_UTF8_VALUE, 
				"I have a sore throat and headache.", containsInAnyOrder("sore throat", "headache" ));
	}
	
	@TestWith({ 
		"application/json;charset=UTF-8",
		"text/plain;charset=UTF-8"
	})
	public void should_find_unicode_characters_using_different_content_type(String contentType) {
		
		makeRequestAndAssertResponse(contentType, 
				"henoch-schönlein purpura", containsInAnyOrder("henoch-schönlein purpura", "purpura" ));
	}
	
	@TestWith(separator="!", value={
			"lymphoblastoma [obs]",
			"henoch-schönlein purpura",
			"calcinosis, raynaud's phenomenon, sclerodactyly, and telangiectasia (crst) syndrome",
			"h/o: injury",
			"open # reduction",
			"malnutrition of mild degree (gomez: 75% to less than 90% of standard weight)",
	})
	public void should_find_matches_containing_special_chars(String existingMatch) {
		
		makeRequestAndAssertResponse(APPLICATION_JSON_UTF8_VALUE, existingMatch, hasItems(existingMatch));
	}

	private void makeRequestAndAssertResponse(String contentType, String requestBody, Matcher<?> responseMatcher) {
		
		given()
			.contentType(contentType)
			.body(requestBody)
		.when()
			.post("http://localhost:"+port+FinderController.FIND_MATCHES_PATH)
		.then()
			.contentType(APPLICATION_JSON_UTF8_VALUE)
			.content("$", responseMatcher)
			.statusCode(200);
	}
	
}
