package net.piotrturski.patternmatcher.crosscutting;

import static net.piotrturski.patternmatcher.multimatch.FinderController.FIND_MATCHES_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import uk.co.datumedge.hamcrest.json.SameJSONAs;

public class RestMvcTestUtils {

	public static MockHttpServletRequestBuilder postJson(String requestBody) {
		return MockMvcRequestBuilders.post(FIND_MATCHES_PATH)
				.contentType(APPLICATION_JSON_UTF8)
				.content(requestBody);
	}
	
	/**
	 * strict json comparison
	 */
	public static ResultMatcher jsonContent(String json) {
		return content().string(sameJsonAs(json));
	}

	public static SameJSONAs<? super String> sameJsonAs(String json) {
		return sameJSONAs(toJson(json));
	}
	
	public static ResultMatcher unorderedJsonContent(String json) {
		return content().string(sameJsonAs(json).allowingAnyArrayOrdering());
	}
	
	public static String toJson(String input) {
		return input.replace('\'', '"');
	}
}
