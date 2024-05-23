package org.cardanofoundation.rosetta.yaciindexer;

import com.bloxbean.cardano.yaci.core.config.YaciConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@Import(ConfigurationH2.class)
@EntityScan({
    "org.cardanofoundation.rosetta.yaciindexer.stores.txsize.model"
})
@EnableJpaRepositories({
    "org.cardanofoundation.rosetta.yaciindexer.stores.txsize.model"
})
public class YaciIndexerApplication {

  public static void main(String[] args) {
    YaciConfig.INSTANCE.setReturnBlockCbor(true);
    YaciConfig.INSTANCE.setReturnTxBodyCbor(true);
    SpringApplication.run(YaciIndexerApplication.class, args);
  }

}
