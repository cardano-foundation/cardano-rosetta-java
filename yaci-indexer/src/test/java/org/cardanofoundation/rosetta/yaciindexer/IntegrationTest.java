package org.cardanofoundation.rosetta.yaciindexer;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

@Profile("test-integration")
@SpringBootTest(classes = {YaciIndexerApplication.class})
@Transactional
public abstract class IntegrationTest {
}
