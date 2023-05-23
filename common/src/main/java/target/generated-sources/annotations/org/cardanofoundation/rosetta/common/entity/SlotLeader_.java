package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import javax.annotation.Generated;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(SlotLeader.class)
public abstract class SlotLeader_ extends org.cardanofoundation.rosetta.common.entity.BaseEntity_ {

	public static volatile SingularAttribute<SlotLeader, String> description;
	public static volatile SingularAttribute<SlotLeader, String> hash;
	public static volatile SingularAttribute<SlotLeader, PoolHash> poolHash;

	public static final String DESCRIPTION = "description";
	public static final String HASH = "hash";
	public static final String POOL_HASH = "poolHash";

}

