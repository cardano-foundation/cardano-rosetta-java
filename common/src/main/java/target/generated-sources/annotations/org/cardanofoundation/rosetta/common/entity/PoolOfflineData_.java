package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import javax.annotation.Generated;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(PoolOfflineData.class)
public abstract class PoolOfflineData_ extends org.cardanofoundation.rosetta.common.entity.BaseEntity_ {

	public static volatile SingularAttribute<PoolOfflineData, byte[]> bytes;
	public static volatile SingularAttribute<PoolOfflineData, PoolHash> pool;
	public static volatile SingularAttribute<PoolOfflineData, Long> poolId;
	public static volatile SingularAttribute<PoolOfflineData, String> tickerName;
	public static volatile SingularAttribute<PoolOfflineData, String> json;
	public static volatile SingularAttribute<PoolOfflineData, Long> pmrId;
	public static volatile SingularAttribute<PoolOfflineData, String> hash;
	public static volatile SingularAttribute<PoolOfflineData, String> poolName;
	public static volatile SingularAttribute<PoolOfflineData, PoolMetadataRef> poolMetadataRef;

	public static final String BYTES = "bytes";
	public static final String POOL = "pool";
	public static final String POOL_ID = "poolId";
	public static final String TICKER_NAME = "tickerName";
	public static final String JSON = "json";
	public static final String PMR_ID = "pmrId";
	public static final String HASH = "hash";
	public static final String POOL_NAME = "poolName";
	public static final String POOL_METADATA_REF = "poolMetadataRef";

}

