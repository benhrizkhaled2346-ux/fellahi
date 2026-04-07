package com.appfor.ne3ma.security;

import com.appfor.ne3ma.config.SecurityConfig;
import com.appfor.ne3ma.repository.InvalidTokenRepository;
import com.appfor.ne3ma.service.JWTService;
import com.appfor.ne3ma.service.MyuserdetailsServer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.core.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.RequiredArgsConstructor;
import java.io.IOException;

@RequiredArgsConstructor
@Service
public class JwtFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final MyuserdetailsServer userDetailsService;
    private final InvalidTokenRepository invalidtokenrepo;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println("verify");
        if (request.getRequestURI().equals("/auth/refresh")) {
            filterChain.doFilter(request, response);
            return;
        }
        System.out.println("verify");
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            token=token.trim();
            if (invalidtokenrepo.existsById(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                System.out.println("maymchich");
                return;
            }
            username = jwtService.extractUsername(token);
        }
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtService.validatetoken(token,userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request,response);
    }


}

