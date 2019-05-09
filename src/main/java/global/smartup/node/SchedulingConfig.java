package global.smartup.node;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Profile({"dev", "test", "beta", "prod"})
@EnableScheduling
@Configuration
public class SchedulingConfig {

}
