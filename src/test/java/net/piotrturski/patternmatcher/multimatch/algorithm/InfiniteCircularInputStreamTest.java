package net.piotrturski.patternmatcher.multimatch.algorithm;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

public class InfiniteCircularInputStreamTest {

	@Test
	public void should_cycle_bytes() throws IOException {
		byte[] input    = new byte[]{1,2};
		byte[] expected = new byte[]{1,2,1,2,1};
		
		assertThatStreamProducesExpectedArray(input, expected);
	}
	
	@Test
	public void should_handle_whole_range_of_bytes() throws IOException {
		byte[] input = new byte[Byte.MAX_VALUE - Byte.MIN_VALUE + 1];
		byte value = Byte.MIN_VALUE;
		for (int i = 0; i < input.length; i++) {
			input[i] = value++;
		}
		
		byte[] expected = ArrayUtils.addAll(input, input);

		assertThatStreamProducesExpectedArray(input, expected);
	}
	
	private void assertThatStreamProducesExpectedArray(byte[] input, byte[] expected) throws IOException {
		@SuppressWarnings("resource")
		InputStream stream = new InfiniteCircularInputStream(input);
		
		byte[] actual = new byte[expected.length];
		stream.read(actual);
		
		assertThat(actual).isEqualTo(expected);
	}
	
}
