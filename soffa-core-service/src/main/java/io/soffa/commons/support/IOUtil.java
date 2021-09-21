package io.soffa.commons.support;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class IOUtil {

    private IOUtil() {
    }

    @SneakyThrows
    public static Optional<String> readFileToString(File input) {
        if (input==null || !input.isFile() || !input.canRead()) {
            return Optional.empty();
        }
        String content = FileUtils.readFileToString(input, StandardCharsets.UTF_8);
        if (StringUtils.isBlank(content)) {
            return Optional.empty();
        }
        return Optional.of(content);
    }

    @SneakyThrows
    public static Optional<String> toString(InputStream input) {
        if (input==null) {
            return Optional.empty();
        }
        return Optional.of(IOUtils.toString(input, StandardCharsets.UTF_8));
    }

    @SneakyThrows
    public static String toStringSafe(InputStream input) {
        return IOUtils.toString(input, StandardCharsets.UTF_8);
    }

    @SneakyThrows
    public static String getResourceAsString(String path) {
        return IOUtil.toStringSafe(IOUtil.class.getResourceAsStream(path));
    }

    @SneakyThrows
    public static void write(String data, File output) {
        try (FileWriter fw = new FileWriter(output)) {
            IOUtils.write(data, fw);
            fw.flush();
        }
    }


}
