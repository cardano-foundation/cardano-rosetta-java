package org.cardanofoundation.rosetta.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;

public class FileUtils {

    private FileUtils() {
    }

    public static String fileReader(String path) throws IOException {
        //check if path exists in classpath
        try (
                InputStream input = new FileInputStream(path)
        ) {
            byte[] fileBytes = IOUtils.toByteArray(input);
            return new String(fileBytes, StandardCharsets.UTF_8);
        }
    }

    public static void validator(String path) {
        if (!new File(path).exists()) {
            throw ExceptionFactory.configNotFoundException(path);
        }
    }

}
