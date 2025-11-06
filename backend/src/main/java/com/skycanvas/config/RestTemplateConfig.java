package com.skycanvas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * RestTemplate配置
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);  // 10秒
        factory.setReadTimeout(300000);     // 5分钟
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        // 配置消息转换器，支持微信接口返回的text/plain类型的JSON
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.TEXT_PLAIN);  // 支持text/plain类型
        converter.setSupportedMediaTypes(supportedMediaTypes);
        
        // 将配置好的转换器添加到RestTemplate
        restTemplate.getMessageConverters().add(0, converter);
        
        return restTemplate;
    }
}

