package com.lyentech.bdc.md.auth.config;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

/**
 * @author caihaopeng
 * @since 18:08 2022/1/5
 **/
@Configuration
public class RestTemplateConfig {
    private static final Logger logger = LoggerFactory.getLogger(RestTemplateConfig.class);

    /**
     * 连接超时时间
     */
    private static final int CONNECT_TIMEOUT = 15000;

    /**
     * 连接管理器连接请求超时时间
     */
    private static final int CONNECTION_MANAGER_CONNECTION_REQUEST_TIMEOUT = 1000;

    /**
     * socket请求超时时间
     */
    private static final int SOCKET_TIMEOUT = 120 * 1000;

    /**
     * 最大连接数
     */
    private static final int MAX_TOTAL = 500;

    /**
     * 每个路由的最大连接数
     */
    private static final int MAX_PER_ROUTE = 300;

    /**
     * 重试次数
     */
    private static final int RETRY_TIMES = 3;

    /**
     * 重试延迟
     */
    private static final int RETRY_INTERVAL_TIME = 1000;

    /**
     * @return {@link PoolingHttpClientConnectionManager }
     * @Description 请求池
     * @author wangwenying
     * @date 2023-10-11
     */
    @Bean
    public PoolingHttpClientConnectionManager poolingHttpClientConnectionManager() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(MAX_TOTAL);
        connectionManager.setDefaultMaxPerRoute(MAX_PER_ROUTE);
        connectionManager.setValidateAfterInactivity(CONNECT_TIMEOUT);
        return connectionManager;
    }

    @Bean
    public RequestConfig requestConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(CONNECTION_MANAGER_CONNECTION_REQUEST_TIMEOUT)
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .build();
    }

    @Bean
    public CloseableHttpClient httpClient(PoolingHttpClientConnectionManager poolingHttpClientConnectionManager, RequestConfig requestConfig) {
        HttpRequestRetryHandler handler = (exception, curRetryCount, context) -> {
            logger.error("请求发生错误，错误信息ExceptionName:{}", exception.getClass().getName(), exception);
            // curRetryCount 每一次都会递增，从1开始
            if (curRetryCount > RETRY_TIMES) {
                return false;
            }
            try {
                //重试延迟
                Thread.sleep((long) curRetryCount * RETRY_INTERVAL_TIME);
            } catch (InterruptedException e) {
                logger.error("延迟时间发生错误", e);
            }
            if (exception instanceof ConnectTimeoutException
                    || exception instanceof NoHttpResponseException
                    || exception instanceof SocketException
                    || exception instanceof SocketTimeoutException) {
                return true;
            }

            HttpClientContext clientContext = HttpClientContext.adapt(context);
            org.apache.http.HttpRequest request = clientContext.getRequest();
            // 如果请求被认为是幂等的，那么就重试。即重复执行不影响程序其他效果的
            return !(request instanceof HttpEntityEnclosingRequest);
        };
        return HttpClientBuilder
                .create()
                .setConnectionManager(poolingHttpClientConnectionManager)
                .setDefaultRequestConfig(requestConfig)
                .setRetryHandler(handler)
                .build();
    }

    @Bean("restTemplate")
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient(poolingHttpClientConnectionManager(), requestConfig()));
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(factory);
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }

}
