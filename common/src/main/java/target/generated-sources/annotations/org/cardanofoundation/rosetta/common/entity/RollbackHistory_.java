package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.sql.Timestamp;
import javax.annotation.Generated;
import org.cardanofoundation.rosetta.common.enumeration.BlocksDeletionStatus;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(RollbackHistory.class)
public abstract class RollbackHistory_ extends org.cardanofoundation.rosetta.common.entity.BaseEntity_ {

	public static volatile SingularAttribute<RollbackHistory, Long> blockNoEnd;
	public static volatile SingularAttribute<RollbackHistory, String> reason;
	public static volatile SingularAttribute<RollbackHistory, String> blockHashStart;
	public static volatile SingularAttribute<RollbackHistory, Long> blockSlotStart;
	public static volatile SingularAttribute<RollbackHistory, BlocksDeletionStatus> blocksDeletionStatus;
	public static volatile SingularAttribute<RollbackHistory, Long> blockNoStart;
	public static volatile SingularAttribute<RollbackHistory, String> blockHashEnd;
	public static volatile SingularAttribute<RollbackHistory, Timestamp> rollbackTime;
	public static volatile SingularAttribute<RollbackHistory, Long> blockSlotEnd;

	public static final String BLOCK_NO_END = "blockNoEnd";
	public static final String REASON = "reason";
	public static final String BLOCK_HASH_START = "blockHashStart";
	public static final String BLOCK_SLOT_START = "blockSlotStart";
	public static final String BLOCKS_DELETION_STATUS = "blocksDeletionStatus";
	public static final String BLOCK_NO_START = "blockNoStart";
	public static final String BLOCK_HASH_END = "blockHashEnd";
	public static final String ROLLBACK_TIME = "rollbackTime";
	public static final String BLOCK_SLOT_END = "blockSlotEnd";

}

