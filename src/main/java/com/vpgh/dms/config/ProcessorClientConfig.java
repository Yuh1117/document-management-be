package com.vpgh.dms.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ProcessorProperties.class)
public class ProcessorClientConfig {

    @Bean(name = "processorRestClient")
    public RestClient processorRestClient(RestClient.Builder restClientBuilder, ProcessorProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(120_000);

        return restClientBuilder
                .baseUrl(properties.getBaseUrl().replaceAll("/$", ""))
                .requestFactory(factory)
                .build();
    }
}
