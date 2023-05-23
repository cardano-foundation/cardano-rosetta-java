package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import javax.annotation.Generated;
import org.cardanofoundation.rosetta.common.enumeration.SyncStateType;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(EpochSyncTime.class)
public abstract class EpochSyncTime_ extends org.cardanofoundation.rosetta.common.entity.BaseEntity_ {

	public static volatile SingularAttribute<EpochSyncTime, Long> no;
	public static volatile SingularAttribute<EpochSyncTime, Long> seconds;
	public static volatile SingularAttribute<EpochSyncTime, SyncStateType> state;

	public static final String NO = "no";
	public static final String SECONDS = "seconds";
	public static final String STATE = "state";

}

