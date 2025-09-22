package com.myfinbank.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myfinbank.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        // Crea un oggetto ErrorResponse
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Accesso negato",
                "Non sei autorizzato ad accedere a questa risorsa",
                request.getRequestURI()
        );

        // Imposta lo status e il contenuto della risposta
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
    }
}
