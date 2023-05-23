package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import javax.annotation.Generated;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(StakeDeregistration.class)
public abstract class StakeDeregistration_ extends org.cardanofoundation.rosetta.common.entity.BaseEntity_ {

	public static volatile SingularAttribute<StakeDeregistration, Integer> certIndex;
	public static volatile SingularAttribute<StakeDeregistration, Integer> epochNo;
	public static volatile SingularAttribute<StakeDeregistration, Tx> tx;
	public static volatile SingularAttribute<StakeDeregistration, Redeemer> redeemer;
	public static volatile SingularAttribute<StakeDeregistration, StakeAddress> addr;

	public static final String CERT_INDEX = "certIndex";
	public static final String EPOCH_NO = "epochNo";
	public static final String TX = "tx";
	public static final String REDEEMER = "redeemer";
	public static final String ADDR = "addr";

}

