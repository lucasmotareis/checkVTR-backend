package pmto._bpm.viaturas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class CoreBeansConfig {

    @Bean
    public Clock systemClockUtc() {
        return Clock.systemUTC();
    }
}
