package net.piotrturski.patternmatcher.crosscutting;

import net.piotrturski.patternmatcher.multimatch.api.MultiPatternFinder;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockUtils {

	public static MultiPatternFinder finderFailingWith(RuntimeException e) {
		return when(mock(MultiPatternFinder.class).findUsedPatterns(any())).thenThrow(e).getMock();
	}

}
