package net.piotrturski.patternmatcher.multimatch.dictionary;

import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.google.common.base.Stopwatch;

import net.piotrturski.patternmatcher.multimatch.algorithm.AhoCorasick;

/**
 * Quick & dirty exploratory analysis of dictionary data
 *
 */
class DictionaryStats {

	static private void analyzeDictionary() {
		List<String> lines = DictionaryLoader.loadDictionary();
		Stopwatch stopwatch = Stopwatch.createStarted();
		new AhoCorasick(lines); //will work only before JIT kicks in
		System.out.println("bulding trie took: "+stopwatch);
		
		int totalSize = lines.size();
		System.out.println("lengths (raw stats): "+ stats(lines));
		lines = lines.parallelStream().distinct().collect(Collectors.toList());
		
		System.out.println("lenghts (unique stats): "+ stats(lines));
		System.out.println("duplicates: "+(totalSize - lines.size()));
		
		System.out.println("hashcode collisions:"+ 
				lines.parallelStream()
					.collect(Collectors.groupingBy(String::hashCode, Collectors.counting()))
					.values().parallelStream().filter(x -> x.longValue() > 1).collect(Collectors.toList())
		);
		
		System.out.println("count by length; length=count: "+ new TreeMap<>(
				lines.parallelStream().collect(Collectors.groupingBy(String::length, Collectors.counting())) 
		));
		
		System.out.println("chars:"+
				lines.parallelStream()
					.flatMap(s -> s.chars().mapToObj(c -> String.valueOf((char) c) ))
					.distinct().sorted().collect(Collectors.joining()));
	}

	private static IntSummaryStatistics stats(List<String> lines) {
		return lines.parallelStream().mapToInt(String::length).summaryStatistics();
	}
	
	public static void main(String[] args) {
		analyzeDictionary();
	}
	
}
