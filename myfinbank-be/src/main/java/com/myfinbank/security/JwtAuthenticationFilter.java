package com.myfinbank.security;

import com.myfinbank.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

        logger.debug("Request received for URI: {}", request.getRequestURI());

        String header = request.getHeader("Authorization");
        logger.debug("Authorization header: {}", header);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            logger.debug("Extracted token: {}", token);

            if (jwtTokenUtil.validateToken(token)) {
                String username = jwtTokenUtil.getUsername(token);
                logger.info("Valid token for user: {}", username);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);


                var ruoli = jwtTokenUtil.getRuoli(token).stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());
                logger.debug("User roles: {}", ruoli);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userDetails, null, ruoli);

                SecurityContextHolder.getContext().setAuthentication(auth);
                logger.info("Authentication set successfully for user: {}", username);
            } else {
                logger.warn("Invalid token provided.");
            }
        } else {
            logger.debug("Authorization header not found or malformed.");
        }

        filterChain.doFilter(request, response);
    }
}
