package net.piotrturski.patternmatcher.multimatch.algorithm;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import java.io.StringReader;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.caliper.Param;
import com.google.caliper.api.BeforeRep;
import com.google.caliper.api.Macrobenchmark;
import com.google.caliper.api.VmOptions;
import com.google.caliper.runner.CaliperMain;

import lombok.SneakyThrows;
import net.piotrturski.patternmatcher.multimatch.algorithm.AhoCorasick;
import net.piotrturski.patternmatcher.multimatch.dictionary.DictionaryLoader;

@VmOptions("-XX:-TieredCompilation") // because of caliper's bug
public class AhoCorasickBenchmark {

	@Param({"10", "100", "10000"}) 
	int inputSizeKilo;
	
	private Readable randomInput, dictionaryRepeatedInput;
	private AhoCorasick ahoCorasick;
	
	@BeforeRep
	@SneakyThrows
	public void prepareText() {
		List<String> allPatterns = DictionaryLoader.loadDictionary();
		allPatterns.add("!"); //to make sure search won't terminate after using whole dictionary file as a search input 
		ahoCorasick = new AhoCorasick(allPatterns);

		int size = inputSizeKilo * 1024;
		randomInput = new StringReader(randomAlphanumeric(size));
		
		String fullDictText = DictionaryLoader.loadDictionaryText();
		String repeatedDict = StringUtils.rightPad("", size, fullDictText);
		dictionaryRepeatedInput = new StringReader(repeatedDict);
	}

	@Macrobenchmark
	public int randomText() {
		return ahoCorasick.findUsedPatterns(randomInput).size();
	}
	
	@Macrobenchmark
	public int dictionaryText() {
		return ahoCorasick.findUsedPatterns(dictionaryRepeatedInput).size();
	}
	
	public static void main(String[] args) {
		CaliperMain.main(AhoCorasickBenchmark.class, args);
	}
	
}
