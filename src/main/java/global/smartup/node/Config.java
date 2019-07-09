package global.smartup.node;

import global.smartup.node.filter.HeaderLocaleContextResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class Config {

    @Value("${spring.profiles.active}")
    public String profilesActive;

    @Value("${app.protocol}")
    public String appProtocol;

    @Value("${app.domain}")
    public String appDomain;

    @Value("${app.port}")
    public String appPort;

    @Value("${app.business.market.lock.expire}")
    public Integer appBusinessMarketLockExpire;

    @Value("${eth.protocol}")
    public String ethProtocol;

    @Value("${eth.domain}")
    public String  ethDomain;

    @Value("${eth.port}")
    public String ethPort;

    @Value("${eth.contract.exchange}")
    public String ethExchangeContract;

    @Value("${eth.contract.smartup}")
    public String ethSmartupContract;

    @Value("${eth.contract.sut}")
    public String ethSutContract;

    @Value("${eth.contract.ntt}")
    public String ethNttContract;

    public String ethAdminPublicKey;

    public String ethAdminPrivateKey;

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:i18n/language");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public LocalValidatorFactoryBean localValidatorFactoryBean(MessageSource messageSource) {
        LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
        localValidatorFactoryBean.setValidationMessageSource(messageSource);
        return localValidatorFactoryBean;
    }

    @Bean
    public LocaleResolver localeResolver () {
        return new HeaderLocaleContextResolver();
    }

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory factory){
        RedisTemplate redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        RedisSerializer<Object> jacksonSerializer = new GenericJackson2JsonRedisSerializer();
        redisTemplate.setEnableDefaultSerializer(true);
        redisTemplate.setDefaultSerializer(jacksonSerializer);
        redisTemplate.setValueSerializer(jacksonSerializer);
        redisTemplate.setHashValueSerializer(jacksonSerializer);
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        redisTemplate.setStringSerializer(stringSerializer);
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        return redisTemplate;
    }

    @Bean
    public Docket customDocket() {
        ParameterBuilder parameterBuilder = new ParameterBuilder();
        List<Parameter> parameters = new ArrayList<>();
        parameterBuilder.name("token")
                .description("[Header token]")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(false)
                .build();
        parameters.add(parameterBuilder.build());

        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("Swagger doc")
                .description("Smartup node api doc")
                .version("1.0.0")
                .build();
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("global.smartup.node.controller"))
                .paths(PathSelectors.any())
                .build()
                .globalOperationParameters(parameters)
                .pathMapping("/");

        // 不同环境配置
        docket.host(appDomain + ":" + appPort);
        return docket;
    }


}
