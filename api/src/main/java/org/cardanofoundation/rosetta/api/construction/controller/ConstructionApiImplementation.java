package org.cardanofoundation.rosetta.api.construction.controller;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.cardanofoundation.rosetta.api.construction.service.ConstructionApiService;
import org.openapitools.client.api.ConstructionApi;
import org.openapitools.client.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ConstructionApiImplementation implements ConstructionApi {
    private final ConstructionApiService constructionApiService;
    @Override
    public ResponseEntity<ConstructionCombineResponse> constructionCombine(@RequestBody ConstructionCombineRequest constructionCombineRequest) {
      try {
        return ResponseEntity.ok(constructionApiService.constructionCombineService(constructionCombineRequest));
      } catch (CborException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public ResponseEntity<ConstructionDeriveResponse> constructionDerive(@RequestBody ConstructionDeriveRequest constructionDeriveRequest) {
      try {
        return ResponseEntity.ok(constructionApiService.constructionDeriveService(constructionDeriveRequest));
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public ResponseEntity<TransactionIdentifierResponse> constructionHash(@RequestBody ConstructionHashRequest constructionHashRequest) {
        return ResponseEntity.ok(constructionApiService.constructionHashService(constructionHashRequest));
    }

    @Override
    public ResponseEntity<ConstructionMetadataResponse> constructionMetadata(@RequestBody ConstructionMetadataRequest constructionMetadataRequest)  {
      try {
        return ResponseEntity.ok(constructionApiService.constructionMetadataService(constructionMetadataRequest));
      } catch (CborException e) {
        throw new RuntimeException(e);
      } catch (CborSerializationException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public ResponseEntity<ConstructionParseResponse> constructionParse(@RequestBody @Valid ConstructionParseRequest constructionParseRequest) {
        return ResponseEntity.ok(constructionApiService.constructionParseService(constructionParseRequest));
    }

    @Override
    public ResponseEntity<ConstructionPayloadsResponse> constructionPayloads(@RequestBody @Validated ConstructionPayloadsRequest constructionPayloadsRequest) {
      try {
        return ResponseEntity.ok(constructionApiService.constructionPayloadsService(constructionPayloadsRequest));
      } catch (CborException e) {
        throw new RuntimeException(e);
      } catch (AddressExcepion e) {
        throw new RuntimeException(e);
      } catch (IOException e) {
        throw new RuntimeException(e);
      } catch (CborSerializationException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public ResponseEntity<ConstructionPreprocessResponse> constructionPreprocess(@RequestBody ConstructionPreprocessRequest constructionPreprocessRequest) {
      try {
        return ResponseEntity.ok(constructionApiService.constructionPreprocessService(constructionPreprocessRequest));
      } catch (IOException e) {
        throw new RuntimeException(e);
      } catch (AddressExcepion e) {
        throw new RuntimeException(e);
      } catch (CborSerializationException e) {
        throw new RuntimeException(e);
      } catch (CborException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public ResponseEntity<TransactionIdentifierResponse> constructionSubmit(@RequestBody ConstructionSubmitRequest constructionSubmitRequest) {
        return ResponseEntity.ok(constructionApiService.constructionSubmitService(constructionSubmitRequest));
    }

//    @Override
//    public ResponseEntity<SigningPayloadsResponse> constructionSigningPayloads(@RequestBody SigningPayloadsRequest signingPayloadsRequest) {
//        List<Signature> signatures= signingPayloadsRequest.getPayloads().stream().map(signing_payload->{
//            String privateKey= signingPayloadsRequest.getAddress_privateKey().get(signing_payload.getAccountIdentifier().getAddress());
//            if(privateKey!=null){
//                TweetNacl.Signature.KeyPair keyPair = TweetNacl.Signature.keyPair_fromSecretKey(
//                        HexUtil.decodeHexString(privateKey));
//                TweetNacl.Signature signature = new TweetNacl.Signature(null,
//                        keyPair.getSecretKey());
//                byte[] result = signature.detached(
//                        HexUtil.decodeHexString(signing_payload.getHexBytes()));
//                String string = HexUtil.encodeHexString(result);
//                return new Signature(
//                        signing_payload,
//                        new PublicKey(HexUtil.encodeHexString(keyPair.getPublicKey()), "edwards25519"),
//                        SignatureType.ED25519,
//                        string
//                );
//            }
//
//            return null;
//        }).collect(Collectors.toList());
//        signatures.removeAll(Collections.singleton(null));
//        return ResponseEntity.ok(new SigningPayloadsResponse(signatures));
//    }
}
