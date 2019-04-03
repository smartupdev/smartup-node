package global.smartup.node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import tk.mybatis.spring.annotation.MapperScan;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.List;

@EnableSwagger2
@MapperScan("global.smartup.node.mapper")
@ComponentScan("global.smartup.node")
@SpringBootApplication
public class Starter extends WebMvcConfigurerAdapter {

    @Autowired
    private LocalValidatorFactoryBean localValidatorFactoryBean;

    public static void main(String[] args) {
        SpringApplication.run(Starter.class, args);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        StringHttpMessageConverter stringConverter  = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        converters.add(stringConverter);
        Jackson2ObjectMapperBuilder jacksonMapperBuilder = new Jackson2ObjectMapperBuilder();
        jacksonMapperBuilder.indentOutput(true).dateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter(jacksonMapperBuilder.build());
        converters.add(jsonConverter);
        super.configureMessageConverters(converters);
    }

    @Override
    public Validator getValidator() {
        return localValidatorFactoryBean;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        super.addResourceHandlers(registry);
    }

}
