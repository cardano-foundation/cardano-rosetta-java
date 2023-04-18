package org.cardanofoundation.rosetta.api.constructionApiService;



import org.cardanofoundation.rosetta.api.addedenum.NetworkIdentifierEnum;
import org.cardanofoundation.rosetta.api.model.rest.*;

import java.io.IOException;

public interface ConstructionApiService {
    //    <T,R> AccountIdentifier withNetworkValidation(NetworkIdentifier networkIdentifier,T parameters, );
    ConstructionDeriveResponse constructionDeriveService(ConstructionDeriveRequest constructionDeriveRequest) throws IllegalAccessException;

   // ConstructionDeriveResponse constructionHashService(ConstructionHashRequest constructionHashRequest) throws IllegalAccessException, IOException, CborException;
    ConstructionPreprocessResponse constructionPreprocessService(ConstructionPreprocessRequest constructionPreprocessRequest) throws IOException;
    NetworkIdentifierEnum getNetworkIdentifierByRequestParameters(NetworkIdentifier networkRequestParameters);

    boolean isKeyValid(String publicKeyBytes,String curveType);

    boolean isAddressTypeValid(String type);
}
