package com.chatterbox.api_rest.service;

import com.chatterbox.api_rest.repository.MensajesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class MensajesService {
    private final MensajesRepository mensajesRepository;
}
