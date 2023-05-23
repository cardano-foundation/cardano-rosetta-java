package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigInteger;
import javax.annotation.Generated;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(EpochStake.class)
public abstract class EpochStake_ extends org.cardanofoundation.rosetta.common.entity.BaseEntity_ {

	public static volatile SingularAttribute<EpochStake, BigInteger> amount;
	public static volatile SingularAttribute<EpochStake, Integer> epochNo;
	public static volatile SingularAttribute<EpochStake, PoolHash> pool;
	public static volatile SingularAttribute<EpochStake, StakeAddress> addr;

	public static final String AMOUNT = "amount";
	public static final String EPOCH_NO = "epochNo";
	public static final String POOL = "pool";
	public static final String ADDR = "addr";

}

