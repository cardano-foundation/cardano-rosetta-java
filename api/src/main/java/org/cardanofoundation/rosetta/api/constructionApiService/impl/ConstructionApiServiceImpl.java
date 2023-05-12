package org.cardanofoundation.rosetta.api.constructionApiService.impl;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.*;
import co.nstant.in.cbor.model.Map;
import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.address.ByronAddress;
import com.bloxbean.cardano.client.address.util.AddressUtil;
import com.bloxbean.cardano.client.common.cbor.CborSerializationUtil;
import com.bloxbean.cardano.client.crypto.bip32.key.HdPublicKey;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.*;
import com.bloxbean.cardano.client.transaction.spec.cert.*;
import com.bloxbean.cardano.yaci.core.model.PoolParams;
import com.bloxbean.cardano.yaci.core.protocol.localtx.model.TxSubmissionRequest;
import com.bloxbean.cardano.yaci.helper.LocalTxSubmissionClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.adabox.util.HexUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import org.aspectj.lang.annotation.DeclareError;
import org.bouncycastle.util.encoders.Hex;
import org.cardanofoundation.rosetta.api.addedClass.*;
import org.cardanofoundation.rosetta.api.addedClass.BlockResponse;
import org.cardanofoundation.rosetta.api.addedRepo.BlockRepository;
import org.cardanofoundation.rosetta.api.addedconsotant.Const;
import org.cardanofoundation.rosetta.api.addedenum.*;
import org.cardanofoundation.rosetta.api.constructionApiService.CardanoService;
import org.cardanofoundation.rosetta.api.constructionApiService.ConstructionApiService;
import org.cardanofoundation.rosetta.api.model.*;
import org.cardanofoundation.rosetta.api.model.Currency;
import org.cardanofoundation.rosetta.api.model.rest.*;
import org.cardanofoundation.rosetta.api.model.rest.AccountIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ConstructionApiServiceImpl implements ConstructionApiService {

    @Autowired
    CardanoService cardanoService;

    @Autowired
    BlockRepository blockRepository;

//    private final LocalTxSubmissionClient localTxSubmissionClient;

    @Override
    public ConstructionDeriveResponse constructionDeriveService(ConstructionDeriveRequest constructionDeriveRequest) throws IllegalAccessException, CborSerializationException {
        PublicKey publicKey = constructionDeriveRequest.getPublicKey();
        NetworkIdentifierEnum networkIdentifier = cardanoService.getNetworkIdentifierByRequestParameters(constructionDeriveRequest.getNetworkIdentifier());

        log.info("[constructionDerive] About to check if public key has valid length and curve type");
        if (!cardanoService.isKeyValid(publicKey.getHexBytes(), publicKey.getCurveType())) {
            log.info("[constructionDerive] Public key has an invalid format");
            throw new IllegalArgumentException("invalidPublicKeyFormat");
        }
        log.info("[constructionDerive] Public key has a valid format");

        // eslint-disable-next-line camelcase
        PublicKey stakingCredential = ObjectUtils.isEmpty(constructionDeriveRequest.getMetadata()) ? null : constructionDeriveRequest.getMetadata().getStakingCredential();
        if (!ObjectUtils.isEmpty(stakingCredential)) {
            log.info("[constructionDerive] About to check if staking credential has valid length and curve type");
            if (!cardanoService.isKeyValid(stakingCredential.getHexBytes(), stakingCredential.getCurveType())) {
                log.info("[constructionDerive] Staking credential has an invalid format");
                throw new IllegalArgumentException("invalidStakingKeyFormat");
            }
            log.info("[constructionDerive] Staking credential key has a valid format");
        }

        // eslint-disable-next-line camelcase
        String addressType = ObjectUtils.isEmpty(constructionDeriveRequest.getMetadata()) ? null : constructionDeriveRequest.getMetadata().getAddressType();
        if (addressType != null) {
            log.info("[constructionDerive] About to check if address type is valid");
            if (!cardanoService.isAddressTypeValid(addressType)) {
                log.info("[constructionDerive] Address type has an invalid value");
                throw new IllegalArgumentException("invalidAddressTypeError");
            }
            log.info("[constructionDerive] Address type has a valid value");
        }

        log.info("[constructionDerive] About to generate address");
        String address = cardanoService.generateAddress(
                networkIdentifier,
                publicKey.getHexBytes(),
                // eslint-disable-next-line camelcase
                ObjectUtils.isEmpty(stakingCredential)?null:stakingCredential.getHexBytes(),
                AddressType.findByValue(addressType)
        );
        if (address == null) {
            log.error("[constructionDerive] There was an error generating address");
            throw new IllegalArgumentException("addressGenerationError");
        }
        log.info("[constructionDerive] new address is {}", address);
        return new ConstructionDeriveResponse(new AccountIdentifier(address));
    }

    @Override
    public ConstructionPreprocessResponse constructionPreprocessService(ConstructionPreprocessRequest constructionPreprocessRequest)
        throws IOException, AddressExcepion, CborSerializationException, CborException {
        NetworkIdentifierEnum networkIdentifier = NetworkIdentifierEnum.findByName(constructionPreprocessRequest.getNetworkIdentifier().getNetwork());
        // eslint-disable-next-line camelcase
        Double relativeTtl = cardanoService.calculateRelativeTtl(!ObjectUtils.isEmpty(constructionPreprocessRequest.getMetadata()) ? constructionPreprocessRequest.getMetadata().getRelativeTtl() : null);
        Double transactionSize = cardanoService.calculateTxSize(
                networkIdentifier,
                (ArrayList<Operation>) constructionPreprocessRequest.getOperations(),
                0,
                ObjectUtils.isEmpty(constructionPreprocessRequest.getMetadata()) ? null : constructionPreprocessRequest.getMetadata().getDepositParameters()
        );
        // eslint-disable-next-line camelcase
        return new ConstructionPreprocessResponse(new ConstructionPreprocessResponseOptions(relativeTtl, transactionSize), null);
    }


    @Override
    public ConstructionMetadataResponse constructionMetadataService(ConstructionMetadataRequest constructionMetadataRequest) throws CborException, CborSerializationException {
        Double ttlOffset = constructionMetadataRequest.getOptions().getRelativeTtl();
        Double txSize = constructionMetadataRequest.getOptions().getTransactionSize();
        log.debug("[constructionMetadata] Calculating ttl based on {} relative ttl", ttlOffset);
        Long ttl = cardanoService.calculateTtl(ttlOffset.longValue());
        log.debug("[constructionMetadata] ttl is {}", ttl);
        // As we have calculated tx assuming ttl as 0, we need to properly update the size
        // now that we have it already
        log.debug("[constructionMetadata] updating tx size from {}", txSize);
        Long updatedTxSize = cardanoService.updateTxSize(txSize.longValue(), 0L, ttl);
        log.debug("[constructionMetadata] updated txSize size is ${updatedTxSize}");
        ProtocolParametersResponse protocolParametersResponse = cardanoService.getProtocolParameters();
        log.debug("[constructionMetadata] received protocol parameters from block-service {}", protocolParametersResponse);
        Long suggestedFee = cardanoService.calculateTxMinimumFee(updatedTxSize, protocolParametersResponse);
        log.debug("[constructionMetadata] suggested fee is ${suggestedFee}");
        // eslint-disable-next-line camelcase
        ProtocolParameters protocol_parameters = new ProtocolParameters();
        protocol_parameters.setCoinsPerUtxoSize(protocolParametersResponse.getCoinsPerUtxoSize());
        protocol_parameters.setMaxTxSize(protocolParametersResponse.getMaxTxSize());
        protocol_parameters.setMaxValSize(protocolParametersResponse.getMaxValSize());
        protocol_parameters.setKeyDeposit(protocolParametersResponse.getKeyDeposit());
        protocol_parameters.setMaxCollateralInputs(protocolParametersResponse.getMaxCollateralInputs());
        protocol_parameters.setMinFeeConstant(protocolParametersResponse.getMinFeeConstant());
        protocol_parameters.setMinFeeCoefficient(protocolParametersResponse.getMinFeeCoefficient());
        protocol_parameters.setMinPoolCost(protocolParametersResponse.getMinPoolCost());
        protocol_parameters.setPoolDeposit(protocolParametersResponse.getPoolDeposit());
        protocol_parameters.setProtocol(protocolParametersResponse.getProtocolMajor());
        return new ConstructionMetadataResponse(new ConstructionMetadataResponseMetadata(ttl.toString(), protocol_parameters),
                new ArrayList<>(List.of(cardanoService.mapAmount(suggestedFee.toString(), null, null, null))));
    }

    @Override
    public ConstructionPayloadsResponse constructionPayloadsService(ConstructionPayloadsRequest constructionPayloadsRequest) throws IOException, CborException, CborSerializationException, AddressExcepion {
        String ttl = constructionPayloadsRequest.getMetadata().getTtl();
        List<Operation> operations = constructionPayloadsRequest.getOperations();
        NetworkIdentifierEnum networkIdentifier = cardanoService.getNetworkIdentifierByRequestParameters(constructionPayloadsRequest.getNetworkIdentifier());
        log.info(operations + "[constuctionPayloads] Operations about to be processed");
        ProtocolParameters protocolParameters = constructionPayloadsRequest.getMetadata().getProtocolParameters();
        UnsignedTransaction unsignedTransaction = cardanoService.createUnsignedTransaction(
                networkIdentifier,
                operations,
                Integer.parseInt(ttl),
                new DepositParameters(protocolParameters.getKeyDeposit(), protocolParameters.getPoolDeposit())
        );
        List<SigningPayload> payloads = cardanoService.constructPayloadsForTransactionBody(unsignedTransaction.getHash(), unsignedTransaction.getAddresses());
        String unsignedTransactionString=cardanoService.encodeExtraData(unsignedTransaction.getBytes(),
            new TransactionExtraData(constructionPayloadsRequest.getOperations(), unsignedTransaction.getMetadata()));
        return new ConstructionPayloadsResponse(unsignedTransactionString, payloads);
    }

    @Override
    public ConstructionParseResponse constructionParseService(ConstructionParseRequest constructionParseRequest) {
        Boolean signed = constructionParseRequest.getSigned();
        NetworkIdentifierEnum networkIdentifier = cardanoService.getNetworkIdentifierByRequestParameters(constructionParseRequest.getNetworkIdentifier());
        log.info(constructionParseRequest.getTransaction() + "[constructionParse] Processing");
        Map map = cardanoService.decodeExtraData(constructionParseRequest.getTransaction());
        TransactionExtraData extraData = cardanoService.changeFromMaptoObject((Map) map.get(new UnicodeString("extraData")));
        log.info(map.toString() + "[constructionParse] Decoded");
        TransactionParsed result = cardanoService.parseSignedTransaction(networkIdentifier, ((UnicodeString) map.get(new UnicodeString("transaction"))).getString(), extraData);
        if (signed) {
            return new ConstructionParseResponse(constructionParseRequest.getNetworkIdentifier(), result.getOperations(), result.getAccount_identifier_signers());
        }
        TransactionParsed result2 = cardanoService.parseSignedTransaction(networkIdentifier, ((UnicodeString) map.get(new UnicodeString("transaction"))).getString(), extraData);
        return new ConstructionParseResponse(constructionParseRequest.getNetworkIdentifier(), result2.getOperations(), result2.getAccount_identifier_signers());
    }

    @Override
    public ConstructionCombineResponse constructionCombineService(ConstructionCombineRequest constructionCombineRequest) throws CborException, CborSerializationException, JsonProcessingException {
        log.info("[constructionCombine] Request received to sign a transaction");
        Map map = cardanoService.decodeExtraData(constructionCombineRequest.getUnsignedTransaction());
        TransactionExtraData extraData = cardanoService.changeFromMaptoObject((Map) map.get(new UnicodeString("extraData")));
        String signedTransaction = cardanoService.buildTransaction(
                ((UnicodeString) map.get(new UnicodeString("transaction"))).getString(),
                constructionCombineRequest.getSignatures().stream().map(signature -> {
                    String chainCode = null;
                    String address = null;
                    AccountIdentifier accountIdentifier = signature.getSigningPayload().getAccountIdentifier();
                    if (!ObjectUtils.isEmpty(accountIdentifier)) {
                        AccountIdentifierMetadata accountIdentifierMetadata = accountIdentifier.getMetadata();
                        if (!ObjectUtils.isEmpty(accountIdentifierMetadata)) {
                            chainCode = accountIdentifierMetadata.getChainCode();
                        }
                        address = accountIdentifier.getAddress();
                    }
                    return new AddedSignatures(signature.getHexBytes(), signature.getPublicKey().getHexBytes(),
                            chainCode, address);
                }).collect(Collectors.toList()),
                extraData.getTransactionMetadataHex()
        );
        log.info(signedTransaction + "[constructionCombine] About to return signed transaction");
        // eslint-disable-next-line camelcase
        return new ConstructionCombineResponse(cardanoService.encodeExtraData(signedTransaction, extraData));
    }

    @Override
    public TransactionIdentifierResponse constructionHashService(ConstructionHashRequest constructionHashRequest) {
        Map map = cardanoService.decodeExtraData(constructionHashRequest.getSignedTransaction());
        log.info("[constructionHash] About to get hash of signed transaction");
        String transactionHash = cardanoService.getHashOfSignedTransaction(((UnicodeString) map.get(new UnicodeString("transaction"))).getString());
        log.info("[constructionHash] About to return hash of signed transaction");
        // eslint-disable-next-line camelcase
        return cardanoService.mapToConstructionHashResponse(transactionHash);
    }

    @Override
    public TransactionIdentifierResponse constructionSubmitService(ConstructionSubmitRequest constructionSubmitRequest) throws CborDeserializationException, CborSerializationException {
//        byte[] signedTransactionBytes = HexUtil.decodeHexString(constructionSubmitRequest.getSignedTransaction());
//        Transaction parsed = Transaction.deserialize(signedTransactionBytes);
//        TxSubmissionRequest txnRequest = new TxSubmissionRequest(parsed.serialize());
//        localTxSubmissionClient.submitTxCallback(txnRequest);
//        Map map = cardanoService.decodeExtraData(constructionSubmitRequest.getSignedTransaction());
//        String transactionHash = cardanoService.getHashOfSignedTransaction(((UnicodeString) map.get(new UnicodeString("transaction"))).getString());
//        return cardanoService.mapToConstructionHashResponse(transactionHash);
        return null;
    }
}
