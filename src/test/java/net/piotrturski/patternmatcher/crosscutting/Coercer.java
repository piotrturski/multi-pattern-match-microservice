package net.piotrturski.patternmatcher.crosscutting;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_STRING_ARRAY;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class Coercer {

	/**
	 * split string on spaces and makes a list. within each element dot is converted to space
	 * @param input
	 * @return
	 */
	public List<String> createAnyList(String input) {
		return Arrays.stream(
					StringUtils.isEmpty(input) ? EMPTY_STRING_ARRAY : input.split(" ")
				)
				.map(s -> s.replace('.', ' '))
				.collect(Collectors.toList());
	}
	
	public Readable toReadable(String input) {
		return new StringReader(input);
	}

	
}
