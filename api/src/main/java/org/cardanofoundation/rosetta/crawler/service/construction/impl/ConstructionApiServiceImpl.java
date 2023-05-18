package org.cardanofoundation.rosetta.crawler.service.construction.impl;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.*;
import co.nstant.in.cbor.model.Map;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import org.cardanofoundation.rosetta.crawler.construction.data.ProtocolParametersResponse;
import org.cardanofoundation.rosetta.crawler.construction.data.Signatures;
import org.cardanofoundation.rosetta.crawler.construction.data.UnsignedTransaction;
import org.cardanofoundation.rosetta.crawler.construction.data.type.AddressType;
import org.cardanofoundation.rosetta.crawler.construction.data.type.NetworkIdentifierType;
import org.cardanofoundation.rosetta.crawler.construction.data.repository.BlockRepository;
import org.cardanofoundation.rosetta.crawler.model.AccountIdentifierMetadata;
import org.cardanofoundation.rosetta.crawler.model.ConstructionPreprocessResponseOptions;
import org.cardanofoundation.rosetta.crawler.model.DepositParameters;
import org.cardanofoundation.rosetta.crawler.model.Operation;
import org.cardanofoundation.rosetta.crawler.model.ProtocolParameters;
import org.cardanofoundation.rosetta.crawler.model.PublicKey;
import org.cardanofoundation.rosetta.crawler.model.SigningPayload;
import org.cardanofoundation.rosetta.crawler.model.TransactionExtraData;
import org.cardanofoundation.rosetta.crawler.model.TransactionParsed;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionCombineRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionCombineResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionDeriveRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionDeriveResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionHashRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionMetadataRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionMetadataResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionMetadataResponseMetadata;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionParseRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionParseResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionPayloadsRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionPayloadsResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionPreprocessRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionPreprocessResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionSubmitRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.TransactionIdentifierResponse;
import org.cardanofoundation.rosetta.crawler.service.construction.CardanoService;
import org.cardanofoundation.rosetta.crawler.service.construction.ConstructionApiService;
import org.cardanofoundation.rosetta.api.model.*;
import org.cardanofoundation.rosetta.api.model.rest.*;
import org.cardanofoundation.rosetta.crawler.model.rest.AccountIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
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
    public ConstructionDeriveResponse constructionDeriveService(
        ConstructionDeriveRequest constructionDeriveRequest) throws IllegalAccessException, CborSerializationException {
        PublicKey publicKey = constructionDeriveRequest.getPublicKey();
        NetworkIdentifierType networkIdentifier = cardanoService.getNetworkIdentifierByRequestParameters(constructionDeriveRequest.getNetworkIdentifier());

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
    public ConstructionPreprocessResponse constructionPreprocessService(
        ConstructionPreprocessRequest constructionPreprocessRequest)
        throws IOException, AddressExcepion, CborSerializationException, CborException {
        NetworkIdentifierType networkIdentifier = NetworkIdentifierType.findByName(constructionPreprocessRequest.getNetworkIdentifier().getNetwork());
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
    public ConstructionMetadataResponse constructionMetadataService(
        ConstructionMetadataRequest constructionMetadataRequest) throws CborException, CborSerializationException {
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
    public ConstructionPayloadsResponse constructionPayloadsService(
        ConstructionPayloadsRequest constructionPayloadsRequest) throws IOException, CborException, CborSerializationException, AddressExcepion {
        String ttl = constructionPayloadsRequest.getMetadata().getTtl();
        List<Operation> operations = constructionPayloadsRequest.getOperations();
        NetworkIdentifierType networkIdentifier = cardanoService.getNetworkIdentifierByRequestParameters(constructionPayloadsRequest.getNetworkIdentifier());
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
    public ConstructionParseResponse constructionParseService(
        ConstructionParseRequest constructionParseRequest) {
        Boolean signed = constructionParseRequest.getSigned();
        NetworkIdentifierType networkIdentifier = cardanoService.getNetworkIdentifierByRequestParameters(constructionParseRequest.getNetworkIdentifier());
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
    public ConstructionCombineResponse constructionCombineService(
        ConstructionCombineRequest constructionCombineRequest) throws CborException, CborSerializationException, JsonProcessingException {
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
                return new Signatures(signature.getHexBytes(), signature.getPublicKey().getHexBytes(),
                    chainCode, address);
            }).collect(Collectors.toList()),
            extraData.getTransactionMetadataHex()
        );
        log.info(signedTransaction + "[constructionCombine] About to return signed transaction");
        // eslint-disable-next-line camelcase
        return new ConstructionCombineResponse(cardanoService.encodeExtraData(signedTransaction, extraData));
    }

    @Override
    public TransactionIdentifierResponse constructionHashService(
        ConstructionHashRequest constructionHashRequest) {
        Map map = cardanoService.decodeExtraData(constructionHashRequest.getSignedTransaction());
        log.info("[constructionHash] About to get hash of signed transaction");
        String transactionHash = cardanoService.getHashOfSignedTransaction(((UnicodeString) map.get(new UnicodeString("transaction"))).getString());
        log.info("[constructionHash] About to return hash of signed transaction");
        // eslint-disable-next-line camelcase
        return cardanoService.mapToConstructionHashResponse(transactionHash);
    }

    @Override
    public TransactionIdentifierResponse constructionSubmitService(
        ConstructionSubmitRequest constructionSubmitRequest) throws CborDeserializationException, CborSerializationException {
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
