package com.myfinbank.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myfinbank.exception.ErrorResponse;
import com.myfinbank.exception.ExpiredTokenException;
import com.myfinbank.exception.InvalidTokenException;
import com.myfinbank.exception.MissingTokenException;
import com.myfinbank.service.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService customUserDetailsService;


    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil, CustomUserDetailsService customUserDetailsService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        logger.info("Request received for URI: {}", request.getRequestURI());

        String header = request.getHeader("Authorization");
        logger.info("Authorization header: {}", header);

        logger.info("Request method: {}", request.getRequestURI());

        if(!request.getRequestURI().equals("/api/auth/login") && !request.getRequestURI().equals("/api/auth/register")) {

            if (header == null || !header.startsWith("Bearer ")) {
                throw new MissingTokenException("Token mancante nell'header Authorization");
            }

            String token = header.substring(7);
            logger.info("Extracted token: {}", token);

            try {

                jwtTokenUtil.validateToken(token);
                String username = jwtTokenUtil.getUsernameFromToken(token);
                logger.info("Valid token for user: {}", username);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(auth);
                logger.info("Authentication set successfully for user: {}", username);

            } catch (ExpiredJwtException ex) {
                logger.info("Token expired", ex.getClaims().getSubject());
                throw new ExpiredTokenException("Il token fornito è scaduto");
            } catch (UnsupportedJwtException | MalformedJwtException | SignatureException ex) {
                throw new InvalidTokenException("Il token fornito non è valido");
            } catch (IllegalArgumentException ex) { // include SignatureException, MalformedJwtException ecc.
                logger.info("Token error", ex);
                throw new InvalidTokenException("Il token fornito non è valido o vuoto");
            }
        }

        filterChain.doFilter(request, response);

    }
}
