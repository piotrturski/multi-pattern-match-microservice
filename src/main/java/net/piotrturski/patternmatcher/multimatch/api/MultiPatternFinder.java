package net.piotrturski.patternmatcher.multimatch.api;

import java.util.Set;

public interface MultiPatternFinder {

	Set<String> findUsedPatterns(Readable inputText);

}