package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import javax.annotation.Generated;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(MultiAsset.class)
public abstract class MultiAsset_ extends org.cardanofoundation.rosetta.common.entity.BaseEntity_ {

	public static volatile SingularAttribute<MultiAsset, String> name;
	public static volatile SingularAttribute<MultiAsset, String> fingerprint;
	public static volatile SingularAttribute<MultiAsset, String> policy;

	public static final String NAME = "name";
	public static final String FINGERPRINT = "fingerprint";
	public static final String POLICY = "policy";

}

