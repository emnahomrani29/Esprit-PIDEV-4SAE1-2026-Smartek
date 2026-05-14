package com.smartek.examservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Intercepteur Feign qui propage le token JWT de la requête entrante
 * vers les appels Feign sortants (exam-service → course-service, training-service).
 *
 * Sans cet intercepteur, les appels Feign arrivent sans Authorization header
 * et sont rejetés avec 401 par les services cibles.
 */
@Configuration
@Slf4j
public class FeignClientInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Bean
    public RequestInterceptor requestTokenBearerInterceptor() {
        return (RequestTemplate requestTemplate) -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

                if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
                    requestTemplate.header(AUTHORIZATION_HEADER, authorizationHeader);
                    log.debug("Token JWT propagé vers l'appel Feign: {}", requestTemplate.url());
                } else {
                    log.warn("Aucun token JWT trouvé pour l'appel Feign: {}", requestTemplate.url());
                }
            } else {
                log.warn("Pas de contexte HTTP disponible pour l'appel Feign (appel async?)");
            }
        };
    }
}
