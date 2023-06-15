package org.cardanofoundation.rosetta.api.controller;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.util.HexUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.iwebpp.crypto.TweetNacl;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.model.PublicKey;
import org.cardanofoundation.rosetta.api.model.Signature;
import org.cardanofoundation.rosetta.api.model.SignatureType;
import org.cardanofoundation.rosetta.api.model.rest.*;
import org.cardanofoundation.rosetta.api.service.ConstructionApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class ConstructionApiDelegateImplementation implements ConstructionApiDelegate {
    @Autowired
    ConstructionApiService constructionApiService;
    @Override
    public ResponseEntity<ConstructionCombineResponse> constructionCombine(@RequestBody ConstructionCombineRequest constructionCombineRequest) throws CborException, CborSerializationException, JsonProcessingException {
        log.info("Construction Combine Request {}" , constructionCombineRequest);
        return ResponseEntity.ok(constructionApiService.constructionCombineService(constructionCombineRequest));
    }

    @Override
    public ResponseEntity<ConstructionDeriveResponse> constructionDerive(@RequestBody ConstructionDeriveRequest constructionDeriveRequest) throws IllegalAccessException, CborSerializationException {
        log.info("Construction Derive Request {}" , constructionDeriveRequest);
       return ResponseEntity.ok(constructionApiService.constructionDeriveService(constructionDeriveRequest));
    }

    @Override
    public ResponseEntity<TransactionIdentifierResponse> constructionHash(@RequestBody ConstructionHashRequest constructionHashRequest) {
        log.info("Construction Hash Request {}" , constructionHashRequest);
       return ResponseEntity.ok(constructionApiService.constructionHashService(constructionHashRequest));
    }

    @Override
    public ResponseEntity<ConstructionMetadataResponse> constructionMetadata(@RequestBody ConstructionMetadataRequest constructionMetadataRequest) throws CborException, CborSerializationException {
       log.info("Construction Metadata Request {}" , constructionMetadataRequest);
       return ResponseEntity.ok(constructionApiService.constructionMetadataService(constructionMetadataRequest));
    }

    @Override
    public ResponseEntity<ConstructionParseResponse> constructionParse(@RequestBody ConstructionParseRequest constructionParseRequest)
        throws UnknownHostException, AddressExcepion, CborDeserializationException, JsonProcessingException {
      log.info("Construction Metadata Request {}" , constructionParseRequest);
      return ResponseEntity.ok(constructionApiService.constructionParseService(constructionParseRequest));
    }

    @Override
    public ResponseEntity<ConstructionPayloadsResponse> constructionPayloads(@RequestBody ConstructionPayloadsRequest constructionPayloadsRequest) throws IOException, CborException, CborSerializationException, AddressExcepion {
        log.info("Construction Payload Request {} " , constructionPayloadsRequest);
       return ResponseEntity.ok(constructionApiService.constructionPayloadsService(constructionPayloadsRequest));
    }

    @Override
    public ResponseEntity<ConstructionPreprocessResponse> constructionPreprocess(@RequestBody ConstructionPreprocessRequest constructionPreprocessRequest)
        throws IOException, AddressExcepion, CborSerializationException, CborException {
        log.info("Construction Preprocess request {} " , constructionPreprocessRequest);
       return ResponseEntity.ok(constructionApiService.constructionPreprocessService(constructionPreprocessRequest));
    }

    @Override
    public ResponseEntity<TransactionIdentifierResponse> constructionSubmit(@RequestBody ConstructionSubmitRequest constructionSubmitRequest)
        throws CborDeserializationException, CborSerializationException, InterruptedException {
        log.info("Construction Submit request {} " , constructionSubmitRequest);
        return ResponseEntity.ok(constructionApiService.constructionSubmitService(constructionSubmitRequest));
    }

    @Override
    public ResponseEntity<SigningPayloadsResponse> constructionSigningPayloads(SigningPayloadsRequest signingPayloadsRequest) {
        TweetNacl.Signature.KeyPair keyPair=TweetNacl.Signature.keyPair_fromSecretKey(HexUtil.decodeHexString(signingPayloadsRequest.getPrivateKey()));
        List<Signature> signatures= signingPayloadsRequest.getPayloads().stream().map(signing_payload->{
            TweetNacl.Signature signature=new TweetNacl.Signature(null,keyPair.getSecretKey());
            byte[] result= signature.detached(HexUtil.decodeHexString(signing_payload.getHexBytes()));
            String string=HexUtil.encodeHexString(result);
            return new Signature(
                    signing_payload,
                    new PublicKey(HexUtil.encodeHexString(keyPair.getPublicKey()),"edwards25519"),
                    SignatureType.ED25519,
                    string
            );
        }).collect(Collectors.toList());
        return ResponseEntity.ok(new SigningPayloadsResponse(signatures));
    }
}
