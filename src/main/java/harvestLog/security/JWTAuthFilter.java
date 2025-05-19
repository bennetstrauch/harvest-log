package harvestLog.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JWTAuthFilter extends OncePerRequestFilter {
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLE_CLAIM = "role";
    private static final String FARMER_ROLE = "FARMER";
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(JWTAuthFilter.class);

    @Autowired
    private JwtUtilityService jwtUtilityService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Skip filter for login endpoint
        if (request.getServletPath().equals("/api/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Look for the Authorization header
        String authorizationHeader = request.getHeader(AUTH_HEADER);
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            logger.debug("No valid Authorization header found");
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the JWT token
        try {
            String jwtToken = authorizationHeader.substring(7);
            logger.debug("Incoming JWT: {}", jwtToken);
            Claims claims = jwtUtilityService.extractAllClaims(jwtToken);
            String username = claims.getSubject();
            Long farmerId = claims.get("farmerId", Long.class);

            if (username == null || farmerId == null || !jwtUtilityService.isTokenValid(jwtToken)) {
                sendUnauthorizedError(response, "Invalid token");
                return;
            }
            String role = claims.get(ROLE_CLAIM, String.class);
            if (!FARMER_ROLE.equals(role)) {
                sendUnauthorizedError(response, "Invalid role");
                return;
            }

            // Create authentication with single Farmer role
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + FARMER_ROLE))
                    );
            authToken.setDetails(farmerId); // Store farmerId in authentication details
            SecurityContextHolder.getContext().setAuthentication(authToken);
            logger.debug("Authentication set for user: {}, farmerId: {}", username, farmerId);
            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            logger.error("JWT validation failed: {}", e.getMessage());
            sendUnauthorizedError(response, "Invalid JWT: " + e.getMessage());
        }
    }

    private void sendUnauthorizedError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}