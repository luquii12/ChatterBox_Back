package com.chatterbox.api_rest.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatterBoxConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
