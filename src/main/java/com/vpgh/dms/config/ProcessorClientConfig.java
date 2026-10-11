package com.vpgh.dms.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;

import java.time.Duration;

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
                .defaultHeader("X-API-Key", properties.getApiKey())
                .build();
    }

    @Bean(name = "processorWebClient")
    public WebClient processorWebClient(WebClient.Builder webClientBuilder, ProcessorProperties properties) {
        HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofMinutes(5));

        return webClientBuilder
                .baseUrl(properties.getBaseUrl().replaceAll("/$", ""))
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("X-API-Key", properties.getApiKey())
                .build();
    }
}
