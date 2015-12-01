package net.piotrturski.patternmatcher.multimatch.algorithm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Sets;
import com.googlecode.zohhak.api.TestWith;
import com.googlecode.zohhak.api.runners.ZohhakRunner;

import net.piotrturski.patternmatcher.crosscutting.Coercer;

@RunWith(ZohhakRunner.class)
public class AhoCorasickTest {
    
    @TestWith(coercers=Coercer.class, value={ 
    	"abcd abde abced,  abcbabcabcdabc  ,  abcd",
    	// corner cases
    	"a aa aaa aaaa b,     aaa ,      a aa aaa",
    	"a b cc,              ''  , ",
    	// case sensitive
    	"A b,                 ab  , b  ",
    	// handles both forms of unicode and right to left alphabets; ڴ = \u06B4  㰕 = \u3C15
    	"ą Ą л ڴ 㰕 ,  aAĄb ڴ \u3C15  , Ą \u06B4 \u3C15",  
    	"ڴ 㰕      ,  a \u06B4        , \u06B4 ",
    	"ڴ 㰕      ,  a \u06B4        , ڴ ",
    	"ڴ 㰕      ,  a ڴ            ,  \u06B4 ",
    	"ڴ 㰕      ,  a ڴ  ,            ڴ ",
    	"\u06B4 㰕 ,  a ڴ             , \u06B4 ",
    	"\u06B4 㰕 ,  a ڴ  ,             ڴ",
    	"\u06B4 㰕 ,  a \u06B4        , \u06B4 ",
    	"\u06B4 㰕 ,  a \u06B4        , ڴ ",
    	// space is like any other char; . -> space in lists
    	". ca .b,      'c a b',    . .b",
    })
    public void should_find_matches(List<String> patterns, Readable text, List<String> matched) {
    	
    	assertThat(new AhoCorasick(patterns).findUsedPatterns(text))
    											.containsOnlyElementsOf(matched);
    }
    
    @Test
    public void should_stop_reading_from_stream_when_answer_is_known() throws IOException {
    	
    	HashSet<String> patterns = Sets.newHashSet("a", "b");

    	Reader infiniteStream = new InputStreamReader(new InfiniteCircularInputStream("acb".getBytes()));
	
		assertThat(new AhoCorasick(patterns).findUsedPatterns(infiniteStream)).isEqualTo(patterns);
    }
    
    @Test
    public void should_reject_null_parameter() {
    	assertThatThrownBy(() -> new AhoCorasick(null))
    									.isInstanceOf(IllegalArgumentException.class);
    }

}
