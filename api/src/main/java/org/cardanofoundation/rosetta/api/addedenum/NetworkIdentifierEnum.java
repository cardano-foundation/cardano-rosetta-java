package org.cardanofoundation.rosetta.api.addedenum;

import com.fasterxml.jackson.annotation.JsonValue;

public enum NetworkIdentifierEnum {
    CARDANO_MAINNET_NETWORK("mainnet",1,764824073L),
    CARDANO_TESTNET_NETWORK("testnet",0,1097911063L);

    String networkId;
    private int value;
    private long protocolMagic;

    NetworkIdentifierEnum(String name,int value,long protocolMagic) {
        this.networkId=networkId;
        this.value = value;
        this.protocolMagic = protocolMagic;
    }
    NetworkIdentifierEnum() {
    }

    @JsonValue
    public String getNetworkId() {
        return networkId;
    }
    @JsonValue
    public int getValue() {
        return value;
    }

    @JsonValue
    public long getProtocolMagic() {
        return protocolMagic;
    }

    public static NetworkIdentifierEnum find(int network){
        for(NetworkIdentifierEnum networkIdentifierEnum :NetworkIdentifierEnum.values()){
            if(network==networkIdentifierEnum.getValue()){
                return networkIdentifierEnum;
            }
        }
        return null;
    }
    public static NetworkIdentifierEnum findByName(String name){
        for(NetworkIdentifierEnum networkIdentifierEnum :NetworkIdentifierEnum.values()){
            if(name.equals(networkIdentifierEnum.getNetworkId())){
                return networkIdentifierEnum;
            }
        }
        return null;
    }

}
