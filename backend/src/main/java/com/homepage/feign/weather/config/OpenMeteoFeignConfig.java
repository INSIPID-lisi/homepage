package com.homepage.feign.weather.config;

import feign.RequestInterceptor;
import feign.codec.Decoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;

/**
 * Feign configuration for OpenMeteo API.
 * Uses Spring's default Jackson decoder — no custom beans needed.
 */
public class OpenMeteoFeignConfig {

}
