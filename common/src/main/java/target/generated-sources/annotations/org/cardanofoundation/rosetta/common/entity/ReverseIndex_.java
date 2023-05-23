package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import javax.annotation.Generated;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ReverseIndex.class)
public abstract class ReverseIndex_ extends org.cardanofoundation.rosetta.common.entity.BaseEntity_ {

	public static volatile SingularAttribute<ReverseIndex, Long> blockId;
	public static volatile SingularAttribute<ReverseIndex, String> minIds;

	public static final String BLOCK_ID = "blockId";
	public static final String MIN_IDS = "minIds";

}

