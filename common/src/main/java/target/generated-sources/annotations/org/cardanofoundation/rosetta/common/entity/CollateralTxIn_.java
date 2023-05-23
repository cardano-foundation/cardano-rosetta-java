package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import javax.annotation.Generated;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(CollateralTxIn.class)
public abstract class CollateralTxIn_ extends org.cardanofoundation.rosetta.common.entity.BaseEntity_ {

	public static volatile SingularAttribute<CollateralTxIn, Short> txOutIndex;
	public static volatile SingularAttribute<CollateralTxIn, Long> txInId;
	public static volatile SingularAttribute<CollateralTxIn, Long> txOutId;

	public static final String TX_OUT_INDEX = "txOutIndex";
	public static final String TX_IN_ID = "txInId";
	public static final String TX_OUT_ID = "txOutId";

}

