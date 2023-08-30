package org.cardanofoundation.rosetta.consumer.service.impl;

import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.UnsignedInteger;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.common.entity.Script;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.ledgersync.Witnesses;
import org.cardanofoundation.rosetta.common.ledgersync.nativescript.NativeScript;
import org.cardanofoundation.rosetta.common.ledgersync.plutus.PlutusV1Script;
import org.cardanofoundation.rosetta.common.ledgersync.plutus.PlutusV2Script;
import org.cardanofoundation.rosetta.common.util.CborSerializationUtil;
import org.cardanofoundation.rosetta.common.util.HexUtil;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.consumer.exception.HashScriptException;
import org.cardanofoundation.rosetta.consumer.factory.NativeScriptFactory;
import org.cardanofoundation.rosetta.consumer.factory.PlutusScriptFactory;
import org.cardanofoundation.rosetta.consumer.projection.ScriptProjection;
import org.cardanofoundation.rosetta.consumer.repository.ScriptRepository;
import org.cardanofoundation.rosetta.consumer.service.ScriptService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScriptServiceImpl implements ScriptService {

  NativeScriptFactory nativeScriptFactory;

  PlutusScriptFactory plutusScriptFactory;

  ScriptRepository scriptRepository;

  @Override
  public void handleScripts(Collection<AggregatedTx> aggregatedTxs, Map<String, Tx> txMap) {
    Map<String, Script> scriptMap = new HashMap<>();

    aggregatedTxs
        .stream()
        .filter(aggregatedTx -> Objects.nonNull(aggregatedTx.getWitnesses()))
        .forEach(aggregatedTx -> {
          Witnesses txWitnesses = aggregatedTx.getWitnesses();
          getAllScript(txWitnesses, txMap.get(aggregatedTx.getHash()))
              .forEach(scriptMap::putIfAbsent);
        });

    saveNonExistsScripts(scriptMap.values());
  }

  @Override
  public Map<String, Script> getAllScript(Witnesses txWitnesses, Tx tx) {

    Map<String, Script> mScripts = new HashMap<>();
    //Native script
    mScripts.putAll(txWitnesses.getNativeScripts().stream()
        .map(nScript -> nativeScriptFactory.handle(nScript, tx))
        .collect(Collectors.toMap(Script::getHash,
            Function.identity(), (a, b) -> a)));

    //Plutus script v1
    mScripts.putAll(txWitnesses.getPlutusV1Scripts().stream()
        .map(plutusScript -> plutusScriptFactory.handle(plutusScript, tx))
        .collect(Collectors.toMap(Script::getHash,
            Function.identity(), (a, b) -> a)));

    //Plutus script v2
    mScripts.putAll(txWitnesses.getPlutusV2Scripts().stream()
        .map(plutusScript -> plutusScriptFactory.handle(plutusScript, tx))
        .collect(Collectors.toMap(Script::getHash,
            Function.identity(), (a, b) -> a)));

    return mScripts;
  }

  @Override
  public List<Script> saveNonExistsScripts(Collection<Script> scripts) {
    if (CollectionUtils.isEmpty(scripts)) {
      return Collections.emptyList();
    }

    Set<String> hashes = scripts.stream()
        .filter(Objects::nonNull)
        .map(Script::getHash)
        .collect(Collectors.toSet());
    Map<String, Script> scriptMap = getScriptsByHashes(hashes);

    Map<String, Script> scriptNeedSave = new HashMap<>();
    scripts.forEach(script -> {
      if (!scriptMap.containsKey(script.getHash())) {
        scriptNeedSave.put(script.getHash(), script);
      }
    });

    if (CollectionUtils.isEmpty(scriptNeedSave)) {
      return Collections.emptyList();
    }

    // Script records need to be saved in sequential order to ease out future queries
    return scriptRepository.saveAll(scriptNeedSave.values()
        .stream()
        .sorted((Comparator.comparing(script -> script.getTx().getId())))
        .toList());
  }

  @Override
  public String getHashOfReferenceScript(String hexReferScript) throws HashScriptException {
    try {
      Array scriptArray = (Array) CborSerializationUtil.deserialize(
          HexUtil.decodeHexString(hexReferScript));
      int type = ((UnsignedInteger) scriptArray.getDataItems().get(0)).getValue().intValue();
      org.cardanofoundation.rosetta.common.ledgersync.nativescript.Script script = null;
      switch (type) {
        case 0:
          script = NativeScript.deserialize((Array) scriptArray.getDataItems().get(1));
          break;
        case 1:
          script = PlutusV1Script.deserialize((ByteString) scriptArray.getDataItems().get(1));
          break;
        case 2:
          script = PlutusV2Script.deserialize((ByteString) scriptArray.getDataItems().get(1));
          break;
        default:
          log.error("Invalid script type {}, hex {}", type, hexReferScript);
          throw new HashScriptException("Script type invalid " + type);
      }
      return script.getPolicyId();
    } catch (Exception e) {
      log.error("Get hash of script {}, error: {}", hexReferScript, e.getMessage());
      throw new HashScriptException(e);
    }
  }

  @Override
  public Map<String, Script> getScriptsByHashes(Set<String> hashes) {
    return scriptRepository.getScriptByHashes(hashes)
        .stream()
        .collect(Collectors.toMap(
            ScriptProjection::getHash,
            scriptProjection -> Script.builder()
                .id(scriptProjection.getId())
                .hash(scriptProjection.getHash())
                .build())
        );
  }
}
