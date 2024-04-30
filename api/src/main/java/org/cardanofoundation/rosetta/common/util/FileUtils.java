package org.cardanofoundation.rosetta.common.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

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

}
