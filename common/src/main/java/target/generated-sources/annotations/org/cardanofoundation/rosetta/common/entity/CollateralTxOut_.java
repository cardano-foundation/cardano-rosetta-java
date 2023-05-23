package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigInteger;
import javax.annotation.Generated;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(CollateralTxOut.class)
public abstract class CollateralTxOut_ extends org.cardanofoundation.rosetta.common.entity.BaseEntity_ {

	public static volatile SingularAttribute<CollateralTxOut, Long> inlineDatumId;
	public static volatile SingularAttribute<CollateralTxOut, String> address;
	public static volatile SingularAttribute<CollateralTxOut, String> paymentCred;
	public static volatile SingularAttribute<CollateralTxOut, String> multiAssetsDescr;
	public static volatile SingularAttribute<CollateralTxOut, String> dataHash;
	public static volatile SingularAttribute<CollateralTxOut, Long> stakeAddressId;
	public static volatile SingularAttribute<CollateralTxOut, Long> referenceScriptId;
	public static volatile SingularAttribute<CollateralTxOut, Long> txId;
	public static volatile SingularAttribute<CollateralTxOut, Short> index;
	public static volatile SingularAttribute<CollateralTxOut, byte[]> addressRaw;
	public static volatile SingularAttribute<CollateralTxOut, BigInteger> value;
	public static volatile SingularAttribute<CollateralTxOut, Boolean> addressHasScript;

	public static final String INLINE_DATUM_ID = "inlineDatumId";
	public static final String ADDRESS = "address";
	public static final String PAYMENT_CRED = "paymentCred";
	public static final String MULTI_ASSETS_DESCR = "multiAssetsDescr";
	public static final String DATA_HASH = "dataHash";
	public static final String STAKE_ADDRESS_ID = "stakeAddressId";
	public static final String REFERENCE_SCRIPT_ID = "referenceScriptId";
	public static final String TX_ID = "txId";
	public static final String INDEX = "index";
	public static final String ADDRESS_RAW = "addressRaw";
	public static final String VALUE = "value";
	public static final String ADDRESS_HAS_SCRIPT = "addressHasScript";

}

