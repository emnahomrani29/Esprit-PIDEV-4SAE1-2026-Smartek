package com.smartek.trainingservice.config;

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
 * vers les appels Feign sortants (ex: training-service → course-service).
 *
 * Sans cet intercepteur, les appels Feign arrivent sans Authorization header
 * et sont rejetés avec 401 par le course-service, ce qui vide la liste des cours.
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
                    log.warn("Aucun token JWT trouvé dans la requête entrante pour l'appel Feign: {}",
                            requestTemplate.url());
                }
            } else {
                log.warn("Pas de contexte de requête HTTP disponible pour l'appel Feign (appel async?)");
            }
        };
    }
}
