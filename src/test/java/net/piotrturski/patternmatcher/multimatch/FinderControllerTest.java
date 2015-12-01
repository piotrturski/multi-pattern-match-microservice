package net.piotrturski.patternmatcher.multimatch;

import static net.piotrturski.patternmatcher.crosscutting.MockUtils.finderFailingWith;
import static net.piotrturski.patternmatcher.crosscutting.RestMvcTestUtils.jsonContent;
import static net.piotrturski.patternmatcher.crosscutting.RestMvcTestUtils.postJson;
import static org.apache.commons.lang3.CharEncoding.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.runner.RunWith;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.googlecode.zohhak.api.Coercion;
import com.googlecode.zohhak.api.Configure;
import com.googlecode.zohhak.api.TestWith;
import com.googlecode.zohhak.api.runners.ZohhakRunner;
import com.netflix.hystrix.HystrixInvokable;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.netflix.hystrix.exception.HystrixRuntimeException.FailureType;

import net.piotrturski.patternmatcher.crosscutting.Coercer;
import net.piotrturski.patternmatcher.crosscutting.RestMvcTestUtils;
import net.piotrturski.patternmatcher.multimatch.FinderController;
import net.piotrturski.patternmatcher.multimatch.algorithm.AhoCorasick;
import net.piotrturski.patternmatcher.multimatch.api.MultiPatternFinder;

@RunWith(ZohhakRunner.class)
@Configure(separator=";")
public class FinderControllerTest {
	
	
	@TestWith(inheritCoercers=false, coercers=Coercer.class, value={
		" a b ; aaa abc ; ['a', 'b']",
		" 㰕 ; 㰕     ; ['\u3C15']",
		" a ; ''      ; [] ",
	})
	public void should_parse_input_and_serialize_result(List<String> patterns, String requestBody, String expectedResponseBody) throws Exception {
		
		usingMvcBackedBy(new AhoCorasick(patterns))
		
		.perform(postJson(requestBody))
		
		.andExpect(status().isOk())
		.andExpect(jsonContent(expectedResponseBody));
	}

	@TestWith({
		"TIMEOUT 			; 503 ; System is too busy. Try again later",
		"COMMAND_EXCEPTION  ; 500 ; Internal error"
	})
	public void should_return_error_msg_on_hystrix_error(FailureType failureType, int errorCode, String expectedResponse) throws Exception {
		
		HystrixRuntimeException hystrixException = new HystrixRuntimeException(failureType, HystrixInvokable.class, "", null, null);
		
		usingMvcBackedBy(finderFailingWith(hystrixException))
		
		.perform(postJson(toJson("")))
		
		.andExpect(status().is(errorCode))
		.andExpect(jsonContent("'"+expectedResponse+"'"));
	}
	
	/**
	 * Creates a MockMvc that is backed by the provided backend 
	 */
	private MockMvc usingMvcBackedBy(MultiPatternFinder backend) {
		FinderController view = new FinderController(backend);
		return MockMvcBuilders.standaloneSetup(view)
				.setMessageConverters(new MappingJackson2HttpMessageConverter())
				.alwaysExpect(forwardedUrl(null))
				.alwaysExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
				.alwaysExpect(content().encoding(UTF_8))
				.build();
	}
	
	@Coercion
	public String toJson(String input) {
		return RestMvcTestUtils.toJson(input);
	}
	
}
