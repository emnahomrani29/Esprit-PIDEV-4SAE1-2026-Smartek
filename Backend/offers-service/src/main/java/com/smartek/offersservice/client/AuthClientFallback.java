package com.smartek.offersservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback Feign : si auth-service est indisponible, on retourne des valeurs par défaut
 * pour ne pas bloquer le service.
 */
@Component
@Slf4j
public class AuthClientFallback implements AuthClient {

    @Override
    public Boolean validateUser(Long userId) {
        log.warn("auth-service unavailable — fallback validateUser({})", userId);
        return true; // permissif en cas de panne du service d'auth
    }

    @Override
    public UserInfoResponse getUserById(Long userId) {
        log.warn("auth-service unavailable — fallback getUserById({})", userId);
        return null;
    }
}
