package org.cardanofoundation.rosetta.consumer.service.impl;

import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.UnsignedInteger;
import org.cardanofoundation.rosetta.common.entity.Script;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.consumer.exception.HashScriptException;
import org.cardanofoundation.rosetta.consumer.factory.NativeScriptFactory;
import org.cardanofoundation.rosetta.consumer.factory.PlutusScriptFactory;
import org.cardanofoundation.rosetta.common.ledgersync.Witnesses;
import org.cardanofoundation.rosetta.common.ledgersync.nativescript.NativeScript;
import org.cardanofoundation.rosetta.common.ledgersync.plutus.PlutusV1Script;
import org.cardanofoundation.rosetta.common.ledgersync.plutus.PlutusV2Script;
import org.cardanofoundation.rosetta.common.util.CborSerializationUtil;
import org.cardanofoundation.rosetta.common.util.HexUtil;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedScriptRepository;
import org.cardanofoundation.rosetta.consumer.service.ScriptService;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScriptServiceImpl implements ScriptService {

  NativeScriptFactory nativeScriptFactory;

  PlutusScriptFactory plutusScriptFactory;

  CachedScriptRepository cachedScriptRepository;

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
    Map<String, Script> scriptMap = cachedScriptRepository.getScriptByHashes(hashes);

    Map<String, Script> scriptNeedSave = new HashMap<>();
    scripts.forEach(script -> {
      if (!scriptMap.containsKey(script.getHash())) {
        scriptNeedSave.put(script.getHash(), script);
      }
    });

    if (CollectionUtils.isEmpty(scriptNeedSave)) {
      return Collections.emptyList();
    }

    return cachedScriptRepository.saveAll(scriptNeedSave.values());
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
      log.error("Get hash of script {}, error: {}",hexReferScript,e.getMessage());
      throw new HashScriptException(e);
    }
  }

  @Override
  public Map<String, Script> getScriptsByHashes(Set<String> hashes) {
    return cachedScriptRepository.getScriptByHashes(hashes);
  }
}
