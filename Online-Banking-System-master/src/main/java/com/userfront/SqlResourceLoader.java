package com.userfront;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
public class SqlResourceLoader {

    public String load(String path) {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load SQL resource: " + path, exception);
        }
    }
}
