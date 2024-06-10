package org.cardanofoundation.rosetta;

import java.math.BigDecimal;
import java.util.List;

import lombok.experimental.UtilityClass;

import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.Allow;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.Block;
import org.openapitools.client.model.BlockIdentifier;
import org.openapitools.client.model.BlockRequest;
import org.openapitools.client.model.BlockResponse;
import org.openapitools.client.model.BlockTransactionRequest;
import org.openapitools.client.model.BlockTransactionResponse;
import org.openapitools.client.model.CoinAction;
import org.openapitools.client.model.CoinChange;
import org.openapitools.client.model.CoinIdentifier;
import org.openapitools.client.model.ConstructionCombineRequest;
import org.openapitools.client.model.ConstructionCombineResponse;
import org.openapitools.client.model.ConstructionDeriveRequest;
import org.openapitools.client.model.ConstructionDeriveResponse;
import org.openapitools.client.model.ConstructionHashRequest;
import org.openapitools.client.model.ConstructionMetadataRequest;
import org.openapitools.client.model.ConstructionMetadataRequestOption;
import org.openapitools.client.model.ConstructionMetadataResponse;
import org.openapitools.client.model.ConstructionMetadataResponseMetadata;
import org.openapitools.client.model.ConstructionParseRequest;
import org.openapitools.client.model.ConstructionParseResponse;
import org.openapitools.client.model.ConstructionPayloadsRequest;
import org.openapitools.client.model.ConstructionPayloadsResponse;
import org.openapitools.client.model.ConstructionPreprocessRequest;
import org.openapitools.client.model.ConstructionPreprocessResponse;
import org.openapitools.client.model.ConstructionSubmitRequest;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.CurveType;
import org.openapitools.client.model.MetadataRequest;
import org.openapitools.client.model.NetworkIdentifier;
import org.openapitools.client.model.NetworkListResponse;
import org.openapitools.client.model.NetworkOptionsResponse;
import org.openapitools.client.model.NetworkRequest;
import org.openapitools.client.model.NetworkStatusResponse;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationIdentifier;
import org.openapitools.client.model.OperationStatus;
import org.openapitools.client.model.PartialBlockIdentifier;
import org.openapitools.client.model.PublicKey;
import org.openapitools.client.model.Signature;
import org.openapitools.client.model.SignatureType;
import org.openapitools.client.model.SigningPayload;
import org.openapitools.client.model.TransactionIdentifier;
import org.openapitools.client.model.TransactionIdentifierResponse;
import org.openapitools.client.model.Version;

import org.cardanofoundation.rosetta.common.model.cardano.crypto.Signatures;

import static org.cardanofoundation.rosetta.common.util.Constants.*;
import static org.openapitools.client.model.CurveType.EDWARDS25519;

@UtilityClass
public class EntityGenerator {

    public static BlockTransactionResponse newBlockTransactionResponse() {
        return BlockTransactionResponse
                .builder()
                .transaction(
                        org.openapitools.client.model.Transaction
                                .builder()
                                .transactionIdentifier(new TransactionIdentifier("hash1"))
                                .build())
                .build();
    }

    public static BlockIdentifier newBlockId() {
        return new BlockIdentifier(123L, "hash1");
    }

    public static BlockResponse newBlockResponse() {
        return BlockResponse
                .builder()
                .block(Block
                        .builder()
                        .blockIdentifier(newBlockId())
                        .build())
                .build();
    }

    public static BlockTransactionRequest newBlockTransactionRequest() {
        return BlockTransactionRequest
                .builder()
                .blockIdentifier(newBlockId())
                .networkIdentifier(newNetworkId())
                .transactionIdentifier(new TransactionIdentifier("txHash1"))
                .build();
    }

    public static BlockRequest newBlockRequest() {
        return BlockRequest
                .builder()
                .blockIdentifier( new PartialBlockIdentifier(123L, "hash1"))
                .networkIdentifier(newNetworkId())
                .build();
    }

    public static NetworkIdentifier newNetworkId() {
        return NetworkIdentifier
                .builder()
                .blockchain(CARDANO_BLOCKCHAIN)
                .network(DEVKIT)
                .build();
    }

    public static ConstructionDeriveResponse givenConstructionDeriveResponse() {
        return ConstructionDeriveResponse
                .builder()
                .accountIdentifier(AccountIdentifier
                        .builder()
                        .address("addr")
                        .build())
                .build();
    }

    public static ConstructionDeriveRequest givenConstructionDeriveRequest() {
        return ConstructionDeriveRequest
                .builder()
                .networkIdentifier(newNetworkId())
                .publicKey(new PublicKey("43d39a2ac216e546", CurveType.EDWARDS25519))
                .build();
    }

    public static ConstructionCombineRequest givenConstructionCombineRequest() {
        return ConstructionCombineRequest
                .builder()
                .networkIdentifier(newNetworkId())
                .unsignedTransaction("unsignedTransaction")
                .signatures(List.of(Signature
                        .builder()
                        .hexBytes("hexBytes")
                        .publicKey(new PublicKey("hex",CurveType.EDWARDS25519))
                        .signatureType(SignatureType.ED25519)
                        .signingPayload(givenSigningPayload())
                        .build()))
                .build();
    }

    public static ConstructionCombineResponse givenConstructionCombineResponse() {
        return ConstructionCombineResponse
                .builder()
                .signedTransaction("signedTransaction")
                .build();
    }

    public static ConstructionSubmitRequest givenConstructionSubmitRequest() {
        return new ConstructionSubmitRequest(newNetworkId(), "signedTransaction");
    }

    public static ConstructionPreprocessRequest givenConstructionPreprocessRequest() {
        return ConstructionPreprocessRequest
                .builder()
                .operations(List.of(Operation
                        .builder()
                        .status("success")
                        .type("type")
                        .operationIdentifier(OperationIdentifier
                                .builder()
                                .index(1L)
                                .build())
                        .build()))
                .networkIdentifier(newNetworkId())
                .build();
    }

    public static ConstructionPayloadsResponse givenConstructionPayloadsResponse() {
        return ConstructionPayloadsResponse
                .builder()
                .unsignedTransaction("unsignedTransaction")
                .build();
    }

    public static ConstructionPayloadsRequest givenConstructionPayloadsRequest() {
        return ConstructionPayloadsRequest
                .builder()
                .operations(List.of(Operation
                        .builder()
                        .status("success")
                        .type("type")
                        .operationIdentifier(OperationIdentifier
                                .builder()
                                .index(1L)
                                .build())
                        .build()))
                .networkIdentifier(newNetworkId())
                .build();
    }

    public static ConstructionParseResponse givenConstructionMetadataResponse() {
        return ConstructionParseResponse
                .builder()
                .operations(List.of(Operation
                        .builder()
                        .type("input")
                        .status("success")
                        .build()))
                .build();
    }

    public static ConstructionParseRequest givenConstructionParseRequest() {
        return ConstructionParseRequest
                .builder()
                .transaction("transaction")
                .signed(false)
                .networkIdentifier(newNetworkId())
                .build();
    }

    public static ConstructionMetadataResponse givenTransactionMetadataResponse() {
        return ConstructionMetadataResponse
                .builder()
                .metadata(ConstructionMetadataResponseMetadata
                        .builder()
                        .ttl(BigDecimal.valueOf(600))
                        .build())
                .build();
    }

    public static ConstructionMetadataRequest givenConstructionMetadataRequest() {
        return ConstructionMetadataRequest
                .builder()
                .networkIdentifier(newNetworkId())
                .options(ConstructionMetadataRequestOption
                        .builder()
                        .relativeTtl(BigDecimal.valueOf(10))
                        .transactionSize(BigDecimal.valueOf(40))
                        .build())
                .build();
    }

    public static ConstructionHashRequest givenConstructionHashRequest() {
        return ConstructionHashRequest
                .builder()
                .networkIdentifier(newNetworkId())
                .signedTransaction("signedTransaction")
                .build();
    }

    public static TransactionIdentifierResponse givenTransactionIdentifierResponse() {
        return TransactionIdentifierResponse
                .builder()
                .transactionIdentifier(TransactionIdentifier
                        .builder()
                        .hash("hash")
                        .build())
                .build();
    }

    public static NetworkOptionsResponse givenNetworkOptionsResponse() {
        return NetworkOptionsResponse
                .builder()
                .version(Version.builder().rosettaVersion("1.14.13").nodeVersion("8.9.0").build())
                .allow(Allow.builder()
                        .operationStatuses(List.of(OperationStatus
                                .builder()
                                .status("success")
                                .successful(true)
                                .build()))
                        .build())
                .build();
    }

    public static NetworkListResponse givenNetworkListResponse() {
        return NetworkListResponse
                .builder()
                .networkIdentifiers(List.of(newNetworkId()))
                .build();
    }

    public static MetadataRequest givenMetadataRequest() {
        return MetadataRequest
                .builder()
                .metadata(new Object())
                .build();
    }

    public static NetworkRequest givenNetworkRequest() {
        return NetworkRequest
                .builder()
                .networkIdentifier(newNetworkId())
                .build();
    }

    public static NetworkStatusResponse givenNetworkStatusResponse() {
        return NetworkStatusResponse
                .builder()
                .currentBlockIdentifier(BlockIdentifier
                        .builder()
                        .hash("123")
                        .index(123L)
                        .build())
                .currentBlockTimestamp(123123123L)
                .build();
    }

    public static ConstructionPreprocessResponse givenConstructionPreprocessResponse() {
        return ConstructionPreprocessResponse
                .builder()
                .requiredPublicKeys(List.of(AccountIdentifier
                        .builder()
                        .address("addr")
                        .build()))
                .build();
    }

    public static PublicKey givenPublicKey() {
        return new PublicKey("1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F",
                EDWARDS25519);

    }

    public static SigningPayload givenSigningPayload() {
        return SigningPayload.builder().hexBytes("hex").build();
    }

    public static Operation givenOperation() {
        return Operation
            .builder()
            .operationIdentifier(OperationIdentifier
                .builder()
                .index(1L)
                .build())
            .status("success")
            .type("input")
            .account(AccountIdentifier
                .builder()
                .address("addr1")
                .build())
            .amount(Amount.builder()
                .value("-90000")
                .currency(Currency.builder().symbol("ADA").build())
                .build())
            .coinChange(CoinChange.builder()
                .coinIdentifier(CoinIdentifier.builder()
                    .identifier("2f:1").build())
                .coinAction(CoinAction.SPENT)
                .build())
            .build();
    }

    public static Signatures givenSignatures() {
        return new Signatures("dc2a1948bfa9411b37e8d280b04c48a85af5588bcf509c0fca798f7b462ebca92d6733dacc1f1c6c1463623c085401be07ea422ad4f1c543375e7d3d2393aa0b",
            "73fea80d424276ad0978d4fe5310e8bc2d485f5f6bb3bf87612989f112ad5a7d",
            "dd75e154da417becec55cdd249327454138f082110297d5e87ab25e15fad150f",
            "Ae2tdPwUPEZC6WJfVQxTNN2tWw4skGrN6zRVukvxJmTFy1nYkVGQBuURU3L");
    }
}
