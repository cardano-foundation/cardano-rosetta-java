package org.cardanofoundation.rosetta.api.block.model.repository.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.rosetta.api.block.model.entity.BlockEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.TransactionSizeEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;
import org.jooq.JSONB;
import org.jooq.Record;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.rosetta.api.jooq.Tables.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TxRepositoryQueryBuilderTest {

    @Mock
    private Record mockRecord;

    private ObjectMapper objectMapper;
    private TxRepositoryQueryBuilder queryBuilder;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        queryBuilder = new TxRepositoryQueryBuilder(objectMapper);
    }

    @Nested
    class MapRecordToTxnEntityTests {

        @Test
        void shouldMapRecordWithTxIndex() throws JsonProcessingException {
            // given
            String txHash = "tx123";
            String blockHash = "block456";
            Long blockNumber = 100L;
            Long blockSlot = 1000L;
            Long fee = 170000L;
            Integer txIndex = 5;
            Integer txSize = 300;
            Integer scriptSize = 50;

            List<UtxoKey> inputKeys = List.of(
                    new UtxoKey("prevtx1", 0)
            );
            List<UtxoKey> outputKeys = List.of(
                    new UtxoKey(txHash, 0)
            );

            JSONB inputsJsonb = JSONB.jsonb(objectMapper.writeValueAsString(inputKeys));
            JSONB outputsJsonb = JSONB.jsonb(objectMapper.writeValueAsString(outputKeys));

            when(mockRecord.get(TRANSACTION.TX_HASH)).thenReturn(txHash);
            when(mockRecord.get(TRANSACTION.INPUTS)).thenReturn(inputsJsonb);
            when(mockRecord.get(TRANSACTION.OUTPUTS)).thenReturn(outputsJsonb);
            when(mockRecord.get(TRANSACTION.FEE)).thenReturn(fee);
            when(mockRecord.get(TRANSACTION.TX_INDEX)).thenReturn(txIndex);
            when(mockRecord.get("joined_block_hash", String.class)).thenReturn(blockHash);
            when(mockRecord.get("joined_block_number", Long.class)).thenReturn(blockNumber);
            when(mockRecord.get("joined_block_slot", Long.class)).thenReturn(blockSlot);
            when(mockRecord.get("joined_tx_size", Integer.class)).thenReturn(txSize);
            when(mockRecord.get("joined_tx_script_size", Integer.class)).thenReturn(scriptSize);

            // when
            TxnEntity result = queryBuilder.mapRecordToTxnEntity(mockRecord);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTxHash()).isEqualTo(txHash);
            assertThat(result.getFee()).isEqualTo(BigInteger.valueOf(fee));
            assertThat(result.getTxIndex()).isEqualTo(txIndex);
            assertThat(result.getInputKeys()).hasSize(1);
            assertThat(result.getOutputKeys()).hasSize(1);

            assertThat(result.getBlock()).isNotNull();
            assertThat(result.getBlock().getHash()).isEqualTo(blockHash);
            assertThat(result.getBlock().getNumber()).isEqualTo(blockNumber);
            assertThat(result.getBlock().getSlot()).isEqualTo(blockSlot);

            assertThat(result.getSizeEntity()).isNotNull();
            assertThat(result.getSizeEntity().getTxHash()).isEqualTo(txHash);
            assertThat(result.getSizeEntity().getSize()).isEqualTo(txSize);
            assertThat(result.getSizeEntity().getScriptSize()).isEqualTo(scriptSize);
        }

        @Test
        void shouldHandleNullTxIndex() throws JsonProcessingException {
            // given - simulating old data before tx_index was added
            String txHash = "tx789";
            Long fee = 150000L;

            List<UtxoKey> inputKeys = List.of();
            List<UtxoKey> outputKeys = List.of(
                    new UtxoKey(txHash, 0)
            );

            JSONB inputsJsonb = JSONB.jsonb(objectMapper.writeValueAsString(inputKeys));
            JSONB outputsJsonb = JSONB.jsonb(objectMapper.writeValueAsString(outputKeys));

            when(mockRecord.get(TRANSACTION.TX_HASH)).thenReturn(txHash);
            when(mockRecord.get(TRANSACTION.INPUTS)).thenReturn(inputsJsonb);
            when(mockRecord.get(TRANSACTION.OUTPUTS)).thenReturn(outputsJsonb);
            when(mockRecord.get(TRANSACTION.FEE)).thenReturn(fee);
            when(mockRecord.get(TRANSACTION.TX_INDEX)).thenReturn(null);
            when(mockRecord.get("joined_block_hash", String.class)).thenReturn(null);

            // when
            TxnEntity result = queryBuilder.mapRecordToTxnEntity(mockRecord);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTxHash()).isEqualTo(txHash);
            assertThat(result.getTxIndex()).isNull();
            assertThat(result.getFee()).isEqualTo(BigInteger.valueOf(fee));
        }

        @Test
        void shouldHandleTxIndexZero() throws JsonProcessingException {
            // given - first transaction in a block has tx_index = 0
            String txHash = "first_tx_in_block";
            Integer txIndex = 0;

            List<UtxoKey> inputKeys = List.of();
            List<UtxoKey> outputKeys = List.of();

            JSONB inputsJsonb = JSONB.jsonb(objectMapper.writeValueAsString(inputKeys));
            JSONB outputsJsonb = JSONB.jsonb(objectMapper.writeValueAsString(outputKeys));

            when(mockRecord.get(TRANSACTION.TX_HASH)).thenReturn(txHash);
            when(mockRecord.get(TRANSACTION.INPUTS)).thenReturn(inputsJsonb);
            when(mockRecord.get(TRANSACTION.OUTPUTS)).thenReturn(outputsJsonb);
            when(mockRecord.get(TRANSACTION.FEE)).thenReturn(null);
            when(mockRecord.get(TRANSACTION.TX_INDEX)).thenReturn(txIndex);
            when(mockRecord.get("joined_block_hash", String.class)).thenReturn(null);

            // when
            TxnEntity result = queryBuilder.mapRecordToTxnEntity(mockRecord);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTxIndex()).isEqualTo(0);
        }

        @Test
        void shouldHandleHighTxIndex() throws JsonProcessingException {
            // given - block with many transactions
            String txHash = "last_tx_in_large_block";
            Integer txIndex = 9999;

            List<UtxoKey> inputKeys = List.of();
            List<UtxoKey> outputKeys = List.of();

            JSONB inputsJsonb = JSONB.jsonb(objectMapper.writeValueAsString(inputKeys));
            JSONB outputsJsonb = JSONB.jsonb(objectMapper.writeValueAsString(outputKeys));

            when(mockRecord.get(TRANSACTION.TX_HASH)).thenReturn(txHash);
            when(mockRecord.get(TRANSACTION.INPUTS)).thenReturn(inputsJsonb);
            when(mockRecord.get(TRANSACTION.OUTPUTS)).thenReturn(outputsJsonb);
            when(mockRecord.get(TRANSACTION.FEE)).thenReturn(null);
            when(mockRecord.get(TRANSACTION.TX_INDEX)).thenReturn(txIndex);
            when(mockRecord.get("joined_block_hash", String.class)).thenReturn(null);

            // when
            TxnEntity result = queryBuilder.mapRecordToTxnEntity(mockRecord);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTxIndex()).isEqualTo(9999);
        }

        @Test
        void shouldMapWithoutBlockData() throws JsonProcessingException {
            // given - transaction without joined block data
            String txHash = "tx_without_block";
            Integer txIndex = 3;

            List<UtxoKey> inputKeys = List.of();
            List<UtxoKey> outputKeys = List.of();

            JSONB inputsJsonb = JSONB.jsonb(objectMapper.writeValueAsString(inputKeys));
            JSONB outputsJsonb = JSONB.jsonb(objectMapper.writeValueAsString(outputKeys));

            when(mockRecord.get(TRANSACTION.TX_HASH)).thenReturn(txHash);
            when(mockRecord.get(TRANSACTION.INPUTS)).thenReturn(inputsJsonb);
            when(mockRecord.get(TRANSACTION.OUTPUTS)).thenReturn(outputsJsonb);
            when(mockRecord.get(TRANSACTION.FEE)).thenReturn(null);
            when(mockRecord.get(TRANSACTION.TX_INDEX)).thenReturn(txIndex);
            when(mockRecord.get("joined_block_hash", String.class)).thenReturn(null);
            // No need to mock block_number, block_slot, tx_size, script_size when block_hash is null

            // when
            TxnEntity result = queryBuilder.mapRecordToTxnEntity(mockRecord);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTxHash()).isEqualTo(txHash);
            assertThat(result.getTxIndex()).isEqualTo(txIndex);
            assertThat(result.getBlock()).isNull();
            assertThat(result.getSizeEntity()).isNull();
        }

        @Test
        void shouldHandleNullInputsAndOutputs() {
            // given
            String txHash = "tx_with_null_io";
            Integer txIndex = 1;

            when(mockRecord.get(TRANSACTION.TX_HASH)).thenReturn(txHash);
            when(mockRecord.get(TRANSACTION.INPUTS)).thenReturn(null);
            when(mockRecord.get(TRANSACTION.OUTPUTS)).thenReturn(null);
            when(mockRecord.get(TRANSACTION.FEE)).thenReturn(null);
            when(mockRecord.get(TRANSACTION.TX_INDEX)).thenReturn(txIndex);
            when(mockRecord.get("joined_block_hash", String.class)).thenReturn(null);

            // when
            TxnEntity result = queryBuilder.mapRecordToTxnEntity(mockRecord);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTxHash()).isEqualTo(txHash);
            assertThat(result.getTxIndex()).isEqualTo(txIndex);
            assertThat(result.getInputKeys()).isEmpty();
            assertThat(result.getOutputKeys()).isEmpty();
        }

        @Test
        void shouldHandleComplexUtxoKeysWithTxIndex() throws JsonProcessingException {
            // given
            String txHash = "complex_tx";
            Integer txIndex = 42;

            List<UtxoKey> inputKeys = List.of(
                    new UtxoKey("input1", 0),
                    new UtxoKey("input2", 1),
                    new UtxoKey("input3", 2)
            );
            List<UtxoKey> outputKeys = List.of(
                    new UtxoKey(txHash, 0),
                    new UtxoKey(txHash, 1)
            );

            JSONB inputsJsonb = JSONB.jsonb(objectMapper.writeValueAsString(inputKeys));
            JSONB outputsJsonb = JSONB.jsonb(objectMapper.writeValueAsString(outputKeys));

            when(mockRecord.get(TRANSACTION.TX_HASH)).thenReturn(txHash);
            when(mockRecord.get(TRANSACTION.INPUTS)).thenReturn(inputsJsonb);
            when(mockRecord.get(TRANSACTION.OUTPUTS)).thenReturn(outputsJsonb);
            when(mockRecord.get(TRANSACTION.FEE)).thenReturn(200000L);
            when(mockRecord.get(TRANSACTION.TX_INDEX)).thenReturn(txIndex);
            when(mockRecord.get("joined_block_hash", String.class)).thenReturn(null);

            // when
            TxnEntity result = queryBuilder.mapRecordToTxnEntity(mockRecord);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTxHash()).isEqualTo(txHash);
            assertThat(result.getTxIndex()).isEqualTo(42);
            assertThat(result.getInputKeys()).hasSize(3);
            assertThat(result.getOutputKeys()).hasSize(2);
            assertThat(result.getInputKeys().get(0).getTxHash()).isEqualTo("input1");
            assertThat(result.getInputKeys().get(1).getTxHash()).isEqualTo("input2");
            assertThat(result.getInputKeys().get(2).getTxHash()).isEqualTo("input3");
        }
    }

    @Nested
    class BackwardCompatibilityTests {

        @Test
        void shouldHandleLegacyRecordsWithoutTxIndex() throws JsonProcessingException {
            // given - simulating records from database before tx_index column was added
            String txHash = "legacy_tx";
            String blockHash = "legacy_block";
            Long blockNumber = 50L;
            Long blockSlot = 500L;

            List<UtxoKey> inputKeys = List.of();
            List<UtxoKey> outputKeys = List.of();

            JSONB inputsJsonb = JSONB.jsonb(objectMapper.writeValueAsString(inputKeys));
            JSONB outputsJsonb = JSONB.jsonb(objectMapper.writeValueAsString(outputKeys));

            when(mockRecord.get(TRANSACTION.TX_HASH)).thenReturn(txHash);
            when(mockRecord.get(TRANSACTION.INPUTS)).thenReturn(inputsJsonb);
            when(mockRecord.get(TRANSACTION.OUTPUTS)).thenReturn(outputsJsonb);
            when(mockRecord.get(TRANSACTION.FEE)).thenReturn(null);
            when(mockRecord.get(TRANSACTION.TX_INDEX)).thenReturn(null); // No tx_index
            when(mockRecord.get("joined_block_hash", String.class)).thenReturn(blockHash);
            when(mockRecord.get("joined_block_number", Long.class)).thenReturn(blockNumber);
            when(mockRecord.get("joined_block_slot", Long.class)).thenReturn(blockSlot);
            when(mockRecord.get("joined_tx_size", Integer.class)).thenReturn(null);
            when(mockRecord.get("joined_tx_script_size", Integer.class)).thenReturn(null);

            // when
            TxnEntity result = queryBuilder.mapRecordToTxnEntity(mockRecord);

            // then - should work fine with null txIndex
            assertThat(result).isNotNull();
            assertThat(result.getTxHash()).isEqualTo(txHash);
            assertThat(result.getTxIndex()).isNull();
            assertThat(result.getBlock()).isNotNull();
            assertThat(result.getBlock().getHash()).isEqualTo(blockHash);
        }
    }

    @Nested
    class EdgeCaseTests {

        @Test
        void shouldHandleTransactionSizeWithoutScriptSize() throws JsonProcessingException {
            // given
            String txHash = "tx_no_script";
            Integer txIndex = 1;
            Integer txSize = 250;

            List<UtxoKey> inputKeys = List.of();
            List<UtxoKey> outputKeys = List.of();

            JSONB inputsJsonb = JSONB.jsonb(objectMapper.writeValueAsString(inputKeys));
            JSONB outputsJsonb = JSONB.jsonb(objectMapper.writeValueAsString(outputKeys));

            when(mockRecord.get(TRANSACTION.TX_HASH)).thenReturn(txHash);
            when(mockRecord.get(TRANSACTION.INPUTS)).thenReturn(inputsJsonb);
            when(mockRecord.get(TRANSACTION.OUTPUTS)).thenReturn(outputsJsonb);
            when(mockRecord.get(TRANSACTION.FEE)).thenReturn(null);
            when(mockRecord.get(TRANSACTION.TX_INDEX)).thenReturn(txIndex);
            when(mockRecord.get("joined_block_hash", String.class)).thenReturn("block1");
            when(mockRecord.get("joined_block_number", Long.class)).thenReturn(10L);
            when(mockRecord.get("joined_block_slot", Long.class)).thenReturn(100L);
            when(mockRecord.get("joined_tx_size", Integer.class)).thenReturn(txSize);
            when(mockRecord.get("joined_tx_script_size", Integer.class)).thenReturn(null);

            // when
            TxnEntity result = queryBuilder.mapRecordToTxnEntity(mockRecord);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTxIndex()).isEqualTo(txIndex);
            assertThat(result.getSizeEntity()).isNotNull();
            assertThat(result.getSizeEntity().getSize()).isEqualTo(txSize);
            assertThat(result.getSizeEntity().getScriptSize()).isEqualTo(0); // Default value
        }

        @Test
        void shouldHandleNullFee() throws JsonProcessingException {
            // given
            String txHash = "tx_no_fee";
            Integer txIndex = 2;

            List<UtxoKey> inputKeys = List.of();
            List<UtxoKey> outputKeys = List.of();

            JSONB inputsJsonb = JSONB.jsonb(objectMapper.writeValueAsString(inputKeys));
            JSONB outputsJsonb = JSONB.jsonb(objectMapper.writeValueAsString(outputKeys));

            when(mockRecord.get(TRANSACTION.TX_HASH)).thenReturn(txHash);
            when(mockRecord.get(TRANSACTION.INPUTS)).thenReturn(inputsJsonb);
            when(mockRecord.get(TRANSACTION.OUTPUTS)).thenReturn(outputsJsonb);
            when(mockRecord.get(TRANSACTION.FEE)).thenReturn(null);
            when(mockRecord.get(TRANSACTION.TX_INDEX)).thenReturn(txIndex);
            when(mockRecord.get("joined_block_hash", String.class)).thenReturn(null);

            // when
            TxnEntity result = queryBuilder.mapRecordToTxnEntity(mockRecord);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTxHash()).isEqualTo(txHash);
            assertThat(result.getTxIndex()).isEqualTo(txIndex);
            assertThat(result.getFee()).isNull();
        }
    }
}
