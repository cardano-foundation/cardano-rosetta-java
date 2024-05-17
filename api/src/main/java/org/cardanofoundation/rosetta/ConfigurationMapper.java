package org.cardanofoundation.rosetta;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.hibernate.collection.spi.PersistentCollection;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

@Configuration
public class ConfigurationMapper {

  @Bean
  @SuppressWarnings("unused") //used through injection
  public ModelMapper modelMapper() {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    modelMapper.getConfiguration().setAmbiguityIgnored(true);
    modelMapper.getConfiguration().setSkipNullEnabled(true);
    modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
    modelMapper.getConfiguration().setPreferNestedProperties(false);

    modelMapper.getConfiguration()
        .setPropertyCondition(context ->
            !(context.getSource() instanceof PersistentCollection)
        );
    return modelMapper;
  }

}
