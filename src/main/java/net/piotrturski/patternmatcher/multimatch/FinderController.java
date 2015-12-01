package net.piotrturski.patternmatcher.multimatch;

import static com.netflix.hystrix.exception.HystrixRuntimeException.FailureType.COMMAND_EXCEPTION;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.hystrix.exception.HystrixRuntimeException;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.piotrturski.patternmatcher.multimatch.api.MultiPatternFinder;

@Slf4j
@RestController
@RequestMapping(produces=APPLICATION_JSON_UTF8_VALUE)
@AllArgsConstructor(onConstructor=@__(@Autowired))
public class FinderController {

	public static final String FIND_MATCHES_PATH = "/find-matches";
	
	@NonNull
	private final MultiPatternFinder finder;
	
	@RequestMapping(value=FIND_MATCHES_PATH, method=RequestMethod.POST)
	public Collection<String> compare(Reader textToSearch) throws IOException {
		return finder.findUsedPatterns(textToSearch);
	}
	
	@ExceptionHandler
	@ResponseStatus(BAD_REQUEST)
	public void parsingError(HttpMessageNotReadableException e) {}
	
	@ExceptionHandler
	public String hystrixError(HttpServletResponse response, HystrixRuntimeException e) {
		if (e.getFailureType() == COMMAND_EXCEPTION) {
			log.error("error inside hystrix command", e);
			response.setStatus(INTERNAL_SERVER_ERROR.value());
			return "Internal error";
		}
		response.setStatus(SERVICE_UNAVAILABLE.value());
		return "System is too busy. Try again later";
	}
	
}
