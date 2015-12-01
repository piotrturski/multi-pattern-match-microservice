package net.piotrturski.patternmatcher.multimatch;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.netflix.hystrix.exception.HystrixRuntimeException;

import net.piotrturski.patternmatcher.Application;
import net.piotrturski.patternmatcher.crosscutting.MockUtils;
import net.piotrturski.patternmatcher.multimatch.api.MultiPatternFinder;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes=Application.class)
public class HystrixLayerTest {

	static final RuntimeException AN_EXCEPTION = new RuntimeException("computation failed"); 
	@Autowired MultiPatternFinder hystrixLayer;
	
	@Test
	public void should_use_hystrix_to_proxy_computations() throws Exception {
		
		assertThatThrownBy(()-> hystrixLayer.findUsedPatterns(null))
										.isInstanceOf(HystrixRuntimeException.class)
										.hasCause(AN_EXCEPTION);
	}
	
	@Profile("test")
	@Configuration
	static class FailingCompareService {
		
		@Bean @Primary
		public MultiPatternFinder compareService() {
			
			return new FinderService(MockUtils.finderFailingWith(AN_EXCEPTION));		
		}
	}
	
}
