package net.piotrturski.patternmatcher.multimatch;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.piotrturski.patternmatcher.multimatch.algorithm.AhoCorasick;
import net.piotrturski.patternmatcher.multimatch.api.MultiPatternFinder;

/**
 *  implementation of the AhoCorasick's algorithm for finding optimal sequence alignment.
 *  thread safe
 */
@Component
@AllArgsConstructor(access=AccessLevel.PACKAGE)
public class FinderService implements MultiPatternFinder {

	public static final String DICTIONARY_FILENAME = "phrases";
	
	@NonNull
	private final MultiPatternFinder trie;
	
	@Autowired
	public FinderService(@Value("classpath:/"+DICTIONARY_FILENAME) Resource dictionary) {
		this(new AhoCorasick(loadPatterns(dictionary)));
	}
	
	@HystrixCommand
	public Set<String> findUsedPatterns(Readable inputText) {
		return trie.findUsedPatterns(inputText);
	}
	
	@SneakyThrows(IOException.class)
	private static List<String> loadPatterns(Resource dictionary) {
		try(InputStream input = dictionary.getInputStream()) { 
			return IOUtils.readLines(input, "UTF-8");
		}
	}
	
}
