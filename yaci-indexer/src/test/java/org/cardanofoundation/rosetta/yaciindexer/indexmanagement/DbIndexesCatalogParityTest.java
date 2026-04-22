package org.cardanofoundation.rosetta.yaciindexer.indexmanagement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Issue #9: Ensure the db-indexes.yaml catalog in yaci-indexer stays in sync with the api module.
 * If either file is modified without updating the other, this test will fail, alerting developers
 * to a potential index drift risk.
 */
class DbIndexesCatalogParityTest {

    private static final Path PROJECT_ROOT = resolveProjectRoot();

    private static final Path API_DB_INDEXES =
            PROJECT_ROOT.resolve("api/src/main/resources/config/db-indexes.yaml");

    private static final Path INDEXER_DB_INDEXES =
            PROJECT_ROOT.resolve("yaci-indexer/src/main/resources/config/db-indexes.yaml");

    private static Path resolveProjectRoot() {
        // Walk up from the test class output directory to find the project root
        Path current = Path.of(System.getProperty("user.dir"));
        // If running from a submodule (yaci-indexer), go up one level
        if (current.getFileName().toString().equals("yaci-indexer")) {
            return current.getParent();
        }
        return current;
    }

    @Test
    @DisplayName("api and yaci-indexer db-indexes.yaml should be identical")
    void dbIndexesCatalogsShouldMatch() throws IOException {
        assertTrue(Files.exists(API_DB_INDEXES),
                "API db-indexes.yaml not found at: " + API_DB_INDEXES);
        assertTrue(Files.exists(INDEXER_DB_INDEXES),
                "Indexer db-indexes.yaml not found at: " + INDEXER_DB_INDEXES);

        byte[] apiContent = Files.readAllBytes(API_DB_INDEXES);
        byte[] indexerContent = Files.readAllBytes(INDEXER_DB_INDEXES);

        assertArrayEquals(apiContent, indexerContent,
                "db-indexes.yaml files are out of sync between api/ and yaci-indexer/ modules. " +
                "Update both files to keep the index catalog consistent.");
    }
}
