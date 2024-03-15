package org.cardanofoundation.rosetta.api.block.mapper;

import lombok.AllArgsConstructor;

import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.common.annotation.OpenApiMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.openapitools.client.model.BlockResponse;

@OpenApiMapper
@AllArgsConstructor
public class BlockToBlockResponse {

    final ModelMapper modelMapper;

    public BlockResponse toDto(Block model) { //TODO saa: impl test and fix mapping
        PropertyMap<Block, BlockResponse> pmap = new PropertyMap<>() {
            @Override
            protected void configure() {
                map().getBlock().getBlockIdentifier().setHash(source.getHash());
            }
        };

        return modelMapper.addMappings(pmap).map(model);

    }


}
