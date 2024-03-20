# Introduction
Altough ```Cardano Rosetta Java``` is compliant with [Rosetta Spec](https://docs.cloud.coinbase.com/rosetta/docs/welcome) 
some changes were added, mostly as metadata, as they contain Cardano specific information that needs to be either processed or returned.


To keep it easy, clear and upgradable for changes in the future, all specific metadata are added at the end of the api documentation. 
Instruction how to do an API upgrade can be found in the Upgrade Doc (TODO: Add link to Upgrade Doc, when it's written).

To get a detailed view of the API and the changes use e.g. [Swagger](https://swagger.io/).

## Endpoint specific changes

### ``/block``
The following metadata is also returned, when querying for block information:
```json
"transactionsCount": { "type": "number" },                      // amount of transactions in the block
"createdBy": { "type": "string" },                              // block creation time in UTC expressed as linux timestamp
"size": { "type": "number" },                                   // block size in bytes
"epochNo": { "type": "number" },                                // epoch where the block has been included
"slotNo": { "type": "integer", "format": "int64" }              // block slot number
```
For transaction fields for the ``size`` and ``scriptSize`` are added to Transaction metadata.
```json
"size": { "type": "integer", "format": "int64" },               // transaction size in bytes
"scriptSize": { "type": "integer", "format": "int64" }          // transaction script size in bytes
```

### ``Operation``
Operations are used to represent transactions. Operations will be returned from API Endpoints from the Data API and used from Construction API to build transactions. 
Cardano Rosetta additionally supports also all Operations currently available within the Cardano Blockchain. Available Operations are: Input, Output, Stake_Key_Registration, Stake_Key_Deregistration, Stake_Delegation, Withdrawal, Pool Registraion, Pool Retirement and Vote Registration.
To support all of these operations extra metadata in addition to the Rosetta Spec are needed and these are added to the metadata:
```json
"withdrawalAmount": { "type": "Amount"}                             // The amount of ADA that is withdrawn from the staking account. Only use if it's a withdrawal operation.
"depositAmount": { "type": "Amount"}                                // The amount of ADA that is deposited to register for example a stake address or a pool. Only use if it's a deposit operation.
"refundAmount": { "type": "Amount"}                                 // The amount of ADA that is refunded after deregistering a stake address or a pool. Only use if it's a refund operation.
"staking_credential": { "type": "PublicKey"}                        // The credentials used for staking.
"pool_key_hash": { "type": "string"}                                // The hash of the pool key. Only use if it's a pool registration or retirement operation.
"epoch": { "type": "number"}                                        // The epoch number.
"tokenBundle": { "type": "TokenBundleItem"}                         // List of token bundles . Only use if it's a multi-asset transaction.
"poolRegistrationCert": { "type": "string"}                         // The Certificate used for pool registration. Only use if it's a pool registration operation.
"poolRegistrationParams": { "type": "PoolRegistrationParams"}       // Extra Parameters for a pool registration. Only use if it's a pool registration operation.
"voteRegistrationMetadata": { "type": "VoteRegistrationMetadata"}   // Metadata to register votes. Only use if it's a vote registration operation.
```
###  ``/construction/derive``
Following the rosetta specification this endpoint returns an Enterprise address. 
In addition to that Cardano Rosetta Java allows the creation of Reward and Base addresses, which aren't supported in the Rosetta specification.
Therefore, following optional parameters were added as metadata:
- ``address_type``: Either Reward, Base or Enterprise.
- ```staking_credential```: The public key that will be used for creating a Base. It will be ignored if the ```address_type``` is Reward or Enterprise, since only one key is needed to derive that addresses.

#### Examples
##### Base address
```json
{
  "network_identifier": { "blockchain": "cardano", "network": "mainnet" },
  "public_key": {
    "hex_bytes": "159abeeecdf167ccc0ea60b30f9522154a0d74161aeb159fb43b6b0695f057b3",
    "curve_type": "edwards25519"
  },
  "metadata": {
    "address_type": "Base",
    "staking_credential": {
      "hex_bytes": "964774728c8306a42252adbfb07ccd6ef42399f427ade25a5933ce190c5a8760",
      "curve_type": "edwards25519"
    }
  }
}
```
##### Reward address
```json
{
  "network_identifier": { "blockchain": "cardano", "network": "mainnet" },
  "public_key": {
    "hex_bytes": "964774728c8306a42252adbfb07ccd6ef42399f427ade25a5933ce190c5a8760",
    "curve_type": "edwards25519"
  },
  "metadata": {
    "address_type": "Reward"
  }
}
```
##### Enterprise address
In this case the metadata is optional, since it will be ignored anyway. 
```json
{
  "network_identifier": { "blockchain": "cardano", "network": "mainnet" },
  "public_key": {
    "hex_bytes": "159abeeecdf167ccc0ea60b30f9522154a0d74161aeb159fb43b6b0695f057b3",
    "curve_type": "edwards25519"
  },
  "metadata": {
    "address_type": "Enterprise"
  }
}
```

###  ``/construction/preprocess``
To be able to stay compliant with Rosetta spec, but also let the user define a specific ```ttl``` a new optional parameter within the metadata was introduced. If not set a ```DEFAULT_RELATIVE_TTL``` will be used.
Additionally deposit parameters can be added to the metadata, which are used to take staking  and pool operations into account while calculating the transaction size.

#### Examples of the metadata
```json
{
   "network_identifier": {
      "blockchain": "cardano",
      "network": "mainnet"
   },
  "operations": [...],
  "metadata": {
    "relative_ttl": "100",
    "deposit_parameters": {
      "poolDeposit": "500000000",
      "keyDeposit": "2000000"
    }
  }
}
```