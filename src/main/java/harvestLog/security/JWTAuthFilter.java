package harvestLog.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Component
public class JWTAuthFilter extends OncePerRequestFilter {
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLE_CLAIM = "role";
    private static final String FARMER_ROLE = "FARMER";


    @Autowired
    private JwtUtilityService jwtUtilityService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Skip filter for login endpoint
//        if (request.getServletPath().equals("/api/auth/login")) {
//            filterChain.doFilter(request,response);
//            return;
//        }

        // Check if the security context is already set up
      //  var auth = SecurityContextHolder.getContext().getAuthentication();
        // Look for the Authorizataion header
        String authorizationHeader = request.getHeader(AUTH_HEADER);
        if ( authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            // exit without setting up the security context
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the JWT token
        try {
            String jwtToken = authorizationHeader.substring(7);
      //  Print incoming JWT
            System.out.println("Incoming JWT: " + jwtToken);
            Claims claims= jwtUtilityService.extractAllClaims(jwtToken);
            String username = claims.getSubject();
            if (username == null || !jwtUtilityService.isTokenValid(jwtToken)) {
                sendUnauthorizedError(response, "Invalid token");
                return;
            }
            String role = claims.get(ROLE_CLAIM,String.class);
           if (!FARMER_ROLE.equals(role)) {
               sendUnauthorizedError(response, "Invalid role");
               return;
           }

           // create authentication with single Farmer role
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            List.of(new SimpleGrantedAuthority(FARMER_ROLE))
                    );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            sendUnauthorizedError(response, "Invalid JWT: " + e.getMessage());
        }

    }
    private void sendUnauthorizedError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }

}
