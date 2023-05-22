package org.cardanofoundation.rosetta.consumer.service;

import org.cardanofoundation.rosetta.common.entity.Script;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.ledgersync.Witnesses;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ScriptService {

  /**
   * Get all script entities from tx witnesses
   *
   * @param txWitnesses tx witnesses in process
   * @param tx          tx witnesses' associated tx
   * @return            a map with key is script hash and value is associated
   *                    script entity
   */
  Map<String, Script> getAllScript(Witnesses txWitnesses, Tx tx);

  /**
   * Save non-existent scripts
   *
   * @param scripts collection of scripts for saving
   * @return        list of saved non-existent scripts
   */
  List<Script> saveNonExistsScripts(Collection<Script> scripts);

  /**
   * Get script has of reference script raw data
   *
   * @param hexReferScript raw script data in hex
   * @return script hash
   */
  String getHashOfReferenceScript(String hexReferScript);

  /**
   * Get all scripts by hashes
   *
   * @param hashes script hashes
   * @return a map with key is script hash and value is associated
   *         script entity
   */
  Map<String, Script> getScriptsByHashes(Set<String> hashes);
}
