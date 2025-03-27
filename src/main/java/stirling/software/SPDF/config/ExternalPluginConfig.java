package stirling.software.SPDF.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ConditionalOnProperty(value = "enterpriseEdition.enabled", havingValue = "true")
@ImportResource("${enterpriseEdition.externalConfigLocation}")
public class ExternalPluginConfig {}
