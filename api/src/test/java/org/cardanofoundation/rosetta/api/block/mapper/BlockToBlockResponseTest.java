package org.cardanofoundation.rosetta.api.block.mapper;

import org.cardanofoundation.rosetta.api.block.model.dto.BlockDto;
import org.cardanofoundation.rosetta.api.block.model.dto.TransactionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.TypeMap;
import org.openapitools.client.model.Block;
import org.openapitools.client.model.BlockIdentifier;
import org.openapitools.client.model.BlockResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class BlockToBlockResponseTest {

    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        modelMapper = new ModelMapper();
    }

    @Test
    void toDto_Ok() { //TODO how we agree to name tests? Maybe shouldMapDtoOK?
        BlockDto blockDto = newBlockDto();

        BlockToBlockResponse my = new BlockToBlockResponse(modelMapper);


        my.modelMapper.validate();
        BlockResponse resp = my.toDto(newBlockDto());

        assertThat(blockDto).isEqualTo(resp.getBlock());


    }

    private BlockDto newBlockDto() {
        return new BlockDto(
                "hash",
                1L,
                2L,
                "prevHashBlock",
                21L,
                3L,
                "createdAt",
                4, 5,
                6L, newTransactions(),
                "poolDeposit");
    }

    private List<TransactionDto> newTransactions() {
        return List.of(new TransactionDto()); //TODO saa: fill TransactionDto with data
    }


}


