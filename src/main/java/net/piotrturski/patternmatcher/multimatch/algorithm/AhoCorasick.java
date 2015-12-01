package net.piotrturski.patternmatcher.multimatch.algorithm;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.roklenarcic.util.strings.AhoCorasickMap;
import com.roklenarcic.util.strings.ReadableMatchListener;

import lombok.NonNull;
import lombok.SneakyThrows;
import net.piotrturski.patternmatcher.multimatch.api.MultiPatternFinder;

public class AhoCorasick implements MultiPatternFinder {

	private final static boolean CASE_SENSITIVE = true;
	private final AhoCorasickMap<String> ahoCorasick;
	private final int dictionarySize;
	
	/**
	 * Creates a thread safe object that can be later use to find matches
	 * concurrently by multiple threads 
	 * 
	 * @param patterns
	 */
	public AhoCorasick(@NonNull Collection<String> patterns) {
		List<String> uniquePatterns = Lists.newArrayList(Sets.newHashSet(patterns));
		dictionarySize =  uniquePatterns.size();
		
		ahoCorasick = new AhoCorasickMap<>(uniquePatterns, uniquePatterns, CASE_SENSITIVE);
	}
	
	/**
	 * method will return all found patterns. it will stop reading the input 
	 * as soon as all possible matches are found.
	 * 
	 * Complexity O(size of inputText)
	 * 
	 * @param inputText
	 * @return all found matches. keep it mind it's a set so there is no information
	 *         about multiple matches
	 */
	@SneakyThrows(IOException.class)
	public Set<String> findUsedPatterns(Readable inputText) {
		MatchListener listener = new MatchListener();
		try {
			ahoCorasick.match(inputText, listener);
		} catch (AllMatchesFoundException e) {}
		return listener.allreadyMatched;
	}
	
	/**
	 * internal matcher 
	 *
	 */
	private class MatchListener implements ReadableMatchListener<String> {

		Set<String> allreadyMatched = new HashSet<>(); // direct access for performance

		@Override
		public boolean match(String value) {
			allreadyMatched.add(value);
			boolean shouldContinue = allreadyMatched.size() != dictionarySize;
			if (!shouldContinue) {
				// because of aho-corasick library's bug: https://github.com/RokLenarcic/AhoCorasick/issues/49
				throw new AllMatchesFoundException();
			}
			return shouldContinue;
		}
	};
    
	@SuppressWarnings("serial")
	private class AllMatchesFoundException extends RuntimeException {}
}
