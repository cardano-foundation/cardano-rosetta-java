package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.Block;
import org.cardanofoundation.rosetta.common.entity.Datum;
import org.cardanofoundation.rosetta.common.entity.Delegation;
import org.cardanofoundation.rosetta.common.entity.Epoch;
import org.cardanofoundation.rosetta.common.entity.EpochParam;
import org.cardanofoundation.rosetta.common.entity.ExtraKeyWitness;
import org.cardanofoundation.rosetta.common.entity.MaTxMint;
import org.cardanofoundation.rosetta.common.entity.MaTxOut;
import org.cardanofoundation.rosetta.common.entity.MultiAsset;
import org.cardanofoundation.rosetta.common.entity.ParamProposal;
import org.cardanofoundation.rosetta.common.entity.PoolHash;
import org.cardanofoundation.rosetta.common.entity.PoolMetadataRef;
import org.cardanofoundation.rosetta.common.entity.PoolOwner;
import org.cardanofoundation.rosetta.common.entity.PoolRelay;
import org.cardanofoundation.rosetta.common.entity.PoolRetire;
import org.cardanofoundation.rosetta.common.entity.PoolUpdate;
import org.cardanofoundation.rosetta.common.entity.PotTransfer;
import org.cardanofoundation.rosetta.common.entity.Redeemer;
import org.cardanofoundation.rosetta.common.entity.RedeemerData;
import org.cardanofoundation.rosetta.common.entity.ReferenceTxIn;
import org.cardanofoundation.rosetta.common.entity.Reserve;
import org.cardanofoundation.rosetta.common.entity.Script;
import org.cardanofoundation.rosetta.common.entity.SlotLeader;
import org.cardanofoundation.rosetta.common.entity.StakeAddress;
import org.cardanofoundation.rosetta.common.entity.StakeDeregistration;
import org.cardanofoundation.rosetta.common.entity.StakeRegistration;
import org.cardanofoundation.rosetta.common.entity.Treasury;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.entity.TxIn;
import org.cardanofoundation.rosetta.common.entity.TxMetadata;
import org.cardanofoundation.rosetta.common.entity.TxOut;
import org.cardanofoundation.rosetta.common.entity.Withdrawal;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Getter
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InMemoryCachedEntities {

  Map<String, SlotLeader> slotLeaderMap; // Key is slot leader hash
  Map<Integer, Epoch> epochMap; // Key is epoch number
  Map<byte[], Block> blockMap; // Key is block hash
  Map<byte[], Tx> txMap; // Key is txHash
  Queue<TxIn> txIns;
  List<ReferenceTxIn> referenceTxIns;
  Map<Pair<byte[], Short>, TxOut> txOutMap; // Key is a pair of txHash and index
  Map<String, StakeAddress> stakeAddressMap; // Key is stake address hash (stake reference)
  Map<String, PoolHash> poolHashMap; // Key is raw hash
  Set<PoolMetadataRef> poolMetadataRefs;
  MultiValueMap<PoolHash, PoolUpdate> poolUpdateMap;
  List<PoolOwner> poolOwners;
  List<PoolRelay> poolRelays;
  List<Reserve> reserves;
  List<Treasury> treasuries;
  List<PotTransfer> potTransfers;
  List<PoolRetire> poolRetires;
  List<Delegation> delegations;
  List<StakeDeregistration> stakeDeregistrations;
  List<StakeRegistration> stakeRegistrations;
  Map<String, Script> scriptMap; // Key is script hash
  Map<String, Datum> datumMap; // Key is datum hash
  Map<String, MultiAsset> multiAssetMap; // Key is fingerprint
  Map<Pair<byte[], Short>, Queue<MaTxOut>> maTxOutMap; // Key is a pair of txHash and index
  List<MaTxMint> maTxMints;
  Map<String, ExtraKeyWitness> extraKeyWitnessMap; // Key is hash
  List<Redeemer> redeemers;
  Map<String, RedeemerData> redeemerDataMap; // Key is data hash
  List<Withdrawal> withdrawals;
  List<ParamProposal> paramProposals;
  List<TxMetadata> txMetadata;
  List<EpochParam> epochParams;

  @PostConstruct
  private void postConstruct() {
    slotLeaderMap = new LinkedHashMap<>();
    epochMap = new LinkedHashMap<>();
    blockMap = new LinkedHashMap<>();
    txMap = new LinkedHashMap<>();
    txIns = new ConcurrentLinkedQueue<>();
    referenceTxIns = new LinkedList<>();
    txOutMap = new ConcurrentHashMap<>();
    stakeAddressMap = new ConcurrentHashMap<>();
    poolHashMap = new LinkedHashMap<>();
    poolMetadataRefs = new HashSet<>();
    poolUpdateMap = new LinkedMultiValueMap<>();
    poolOwners = new LinkedList<>();
    poolRelays = new LinkedList<>();
    reserves = new LinkedList<>();
    treasuries = new LinkedList<>();
    potTransfers = new LinkedList<>();
    poolRetires = new LinkedList<>();
    delegations = new LinkedList<>();
    stakeDeregistrations = new LinkedList<>();
    stakeRegistrations = new LinkedList<>();
    scriptMap = new LinkedHashMap<>();
    datumMap = new LinkedHashMap<>();
    multiAssetMap = new ConcurrentHashMap<>();
    maTxOutMap = new ConcurrentHashMap<>();
    maTxMints = new LinkedList<>();
    extraKeyWitnessMap = new LinkedHashMap<>();
    redeemers = new LinkedList<>();
    redeemerDataMap = new LinkedHashMap<>();
    withdrawals = new LinkedList<>();
    paramProposals = new LinkedList<>();
    txMetadata = new LinkedList<>();
    epochParams = new LinkedList<>();
  }
}
