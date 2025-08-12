package dev.guilhermeluan.todo_list.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;

@Component
public class FileUtils {
    @Autowired
    private ResourceLoader resourceLoader;

    public String readResourceFile(String filename) throws IOException {
        var file = resourceLoader.getResource("classpath:%s".formatted(filename)).getFile();
        return new String(Files.readAllBytes(file.toPath()));
    }
}