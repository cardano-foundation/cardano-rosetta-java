package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import com.sotatek.cardano.common.entity.Address;
import com.sotatek.cardano.common.entity.AddressToken;
import com.sotatek.cardano.common.entity.AddressTxBalance;
import com.sotatek.cardano.common.entity.Block;
import com.sotatek.cardano.common.entity.Datum;
import com.sotatek.cardano.common.entity.Delegation;
import com.sotatek.cardano.common.entity.Epoch;
import com.sotatek.cardano.common.entity.ExtraKeyWitness;
import com.sotatek.cardano.common.entity.FailedTxOut;
import com.sotatek.cardano.common.entity.MaTxMint;
import com.sotatek.cardano.common.entity.MaTxOut;
import com.sotatek.cardano.common.entity.MultiAsset;
import com.sotatek.cardano.common.entity.ParamProposal;
import com.sotatek.cardano.common.entity.PoolHash;
import com.sotatek.cardano.common.entity.PoolMetadataRef;
import com.sotatek.cardano.common.entity.PoolOwner;
import com.sotatek.cardano.common.entity.PoolRelay;
import com.sotatek.cardano.common.entity.PoolRetire;
import com.sotatek.cardano.common.entity.PoolUpdate;
import com.sotatek.cardano.common.entity.PotTransfer;
import com.sotatek.cardano.common.entity.Redeemer;
import com.sotatek.cardano.common.entity.RedeemerData;
import com.sotatek.cardano.common.entity.ReferenceTxIn;
import com.sotatek.cardano.common.entity.Reserve;
import com.sotatek.cardano.common.entity.Script;
import com.sotatek.cardano.common.entity.SlotLeader;
import com.sotatek.cardano.common.entity.StakeAddress;
import com.sotatek.cardano.common.entity.StakeDeregistration;
import com.sotatek.cardano.common.entity.StakeRegistration;
import com.sotatek.cardano.common.entity.Treasury;
import com.sotatek.cardano.common.entity.Tx;
import com.sotatek.cardano.common.entity.TxIn;
import com.sotatek.cardano.common.entity.TxMetadata;
import com.sotatek.cardano.common.entity.TxOut;
import com.sotatek.cardano.common.entity.UnconsumeTxIn;
import com.sotatek.cardano.common.entity.Withdrawal;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.annotation.PostConstruct;
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
  Map<String, Block> blockMap; // Key is block hash
  Map<String, Tx> txMap; // Key is txHash
  Queue<TxIn> txIns;
  List<UnconsumeTxIn> unconsumeTxIns;
  List<ReferenceTxIn> referenceTxIns;
  Map<Pair<String, Short>, TxOut> txOutMap; // Key is a pair of txHash and index
  List<FailedTxOut> failedTxOuts;
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
  Map<Pair<String, Short>, Queue<MaTxOut>> maTxOutMap; // Key is a pair of txHash and index
  List<MaTxMint> maTxMints;
  Map<String, ExtraKeyWitness> extraKeyWitnessMap; // Key is hash
  List<Redeemer> redeemers;
  Map<String, RedeemerData> redeemerDataMap; // Key is data hash
  List<Withdrawal> withdrawals;
  List<ParamProposal> paramProposals;
  List<TxMetadata> txMetadata;
  Map<String, Address> addressMap; // Key is address (Base58 or Bech32 format)
  List<AddressToken> addressTokens;
  List<AddressTxBalance> addressTxBalances;

  @PostConstruct
  private void postConstruct() {
    slotLeaderMap = new LinkedHashMap<>();
    epochMap = new LinkedHashMap<>();
    blockMap = new LinkedHashMap<>();
    txMap = new LinkedHashMap<>();
    txIns = new ConcurrentLinkedQueue<>();
    unconsumeTxIns = new LinkedList<>();
    referenceTxIns = new LinkedList<>();
    txOutMap = new ConcurrentHashMap<>();
    failedTxOuts = new LinkedList<>();
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
    addressMap = new ConcurrentHashMap<>();
    addressTokens = new LinkedList<>();
    addressTxBalances = new LinkedList<>();
  }
}
