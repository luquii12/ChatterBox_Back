package com.chatterbox.api_rest.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class MensajesRepository {
    private final JdbcClient jdbcClient;
}
