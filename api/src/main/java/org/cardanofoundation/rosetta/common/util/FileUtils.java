package org.cardanofoundation.rosetta.common.util;

import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FileUtils {

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
