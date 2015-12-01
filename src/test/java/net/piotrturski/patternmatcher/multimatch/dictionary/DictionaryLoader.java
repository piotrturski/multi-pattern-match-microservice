package net.piotrturski.patternmatcher.multimatch.dictionary;

import static net.piotrturski.patternmatcher.multimatch.FinderService.DICTIONARY_FILENAME;
import static org.apache.commons.lang3.CharEncoding.UTF_8;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DictionaryLoader {

	private final File dictionaryFile = new File("./src/main/resources/"+DICTIONARY_FILENAME); 
	
	@SneakyThrows
	public String loadDictionaryText() {
		return FileUtils.readFileToString(dictionaryFile, UTF_8);
	}
	
	@SneakyThrows
	public List<String> loadDictionary() {
		return FileUtils.readLines(dictionaryFile, UTF_8);
	}
	
}
