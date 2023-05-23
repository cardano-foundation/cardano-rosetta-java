package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import javax.annotation.Generated;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ExtraKeyWitness.class)
public abstract class ExtraKeyWitness_ extends org.cardanofoundation.rosetta.common.entity.BaseEntity_ {

	public static volatile SingularAttribute<ExtraKeyWitness, Tx> tx;
	public static volatile SingularAttribute<ExtraKeyWitness, String> hash;

	public static final String TX = "tx";
	public static final String HASH = "hash";

}

