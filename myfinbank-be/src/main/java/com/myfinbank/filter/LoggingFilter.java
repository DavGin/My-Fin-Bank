package com.myfinbank.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Enumeration;

@Component
public class LoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
        logger.info("Filtro di logging inizializzato.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // Controlla se la richiesta è di tipo HTTP
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // Registra informazioni sulla richiesta HTTP
            logRequestDetails(httpRequest);

            // Continua con il filtro successivo
            chain.doFilter(request, response);

            // Registra informazioni sulla risposta HTTP
            logResponseDetails(httpResponse);
        }
    }

    @Override
    public void destroy() {
        logger.info("Filtro di logging distrutto.");
    }

    private void logRequestDetails(HttpServletRequest request) {
        logger.info("------ Dettagli della richiesta ------");
        logger.info("Metodo HTTP: {}", request.getMethod());
        logger.info("URI: {}", request.getRequestURI());


//            logger.info("Intestazione {}: {}", headerName, request.getHeader(headerName));


        logger.info("------ Fine dettagli della richiesta ------");
    }

    private void logResponseDetails(HttpServletResponse response) {
        logger.info("------ Dettagli della risposta ------");
        logger.info("Codice di stato HTTP: {}", response.getStatus());
        logger.info("------ Fine dettagli della risposta ------");
    }
}
