package io.soffa.foundation.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.SneakyThrows;
import org.hamcrest.Matcher;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class HttpResult {

    private final ResultActions result;
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    static {
        SimpleModule simpleModule = new SimpleModule();
        MAPPER.registerModule(simpleModule);
    }
    HttpResult(ResultActions result) {
        this.result = result;
    }

    @SneakyThrows
    public HttpResult isOK() {
        result.andExpect(MockMvcResultMatchers.status().isOk());
        return this;
    }

    @SneakyThrows
    public HttpResult isBadRequest() {
        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
        return this;
    }

    @SneakyThrows
    public HttpResult status(int statusCode) {
        result.andExpect(MockMvcResultMatchers.status().is(statusCode));
        return this;
    }

    @SneakyThrows
    public HttpResult is2xxSuccessful() {
        result.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
        return this;
    }

    @SneakyThrows
    public HttpResult is3xxRedirection() {
        result.andExpect(MockMvcResultMatchers.status().is3xxRedirection());
        return this;
    }

    @SneakyThrows
    public HttpResult is4xxClientError() {
        result.andExpect(MockMvcResultMatchers.status().is4xxClientError());
        return this;
    }

    @SneakyThrows
    public HttpResult is5xxServerError() {
        result.andExpect(MockMvcResultMatchers.status().is5xxServerError());
        return this;
    }

    @SneakyThrows
    public HttpResult isForbidden() {
        result.andExpect(MockMvcResultMatchers.status().isForbidden());
        return this;
    }

    @SneakyThrows
    public HttpResult isUnauthorized() {
        result.andExpect(MockMvcResultMatchers.status().isUnauthorized());
        return this;
    }

    @SneakyThrows
    public HttpResult json(String path, Matcher<?> matcher) {
        result.andExpect(jsonPath(path).value(matcher));
        return this;
    }
    @SneakyThrows
    public <T> T read(Class<T> expectedClass) {
        return MAPPER.readValue(result.andReturn().getResponse().getContentAsString(), expectedClass);
    }

    @SneakyThrows
    public HttpResult hasJson(String path) {
        result.andExpect(jsonPath(path).exists());
        return this;
    }

    @SneakyThrows
    public HttpResult contentIs(String content) {
        result.andExpect(MockMvcResultMatchers.content().string(content));
        return this;
    }
}
