package com.userfront;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
public class SqlResourceLoader {

    public String load(String resourcePath) {
        try (InputStream inputStream = Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream(resourcePath),
                "SQL resource not found: " + resourcePath
        )) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load SQL resource: " + resourcePath, ex);
        }
    }
}
