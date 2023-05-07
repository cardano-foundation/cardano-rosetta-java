package org.cardanofoundation.rosetta.consumer.factory;

import com.bloxbean.cardano.client.transaction.spec.PlutusScript;
import org.cardanofoundation.rosetta.common.entity.Script;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.consumer.service.impl.plutus.PlutusScriptService;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class PlutusScriptFactory extends
    AbstractServiceFactory<PlutusScriptService<? extends PlutusScript>, PlutusScriptService> {// NOSONAR

  protected PlutusScriptFactory(
      List<PlutusScriptService<? extends PlutusScript>> services) {
    super(services);
  }

  @Override
  @PostConstruct
  void init() {
    serviceMap = services.stream().collect(Collectors.toMap(PlutusScriptService::supports,
        Function.identity())
    );
  }

  @SuppressWarnings("unchecked")
  public Script handle(PlutusScript plutusScript, Tx tx){
    return serviceMap.get(plutusScript.getClass()).handle(plutusScript,tx);
  }
}
