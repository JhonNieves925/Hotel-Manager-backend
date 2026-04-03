package co.hotelmanager.backend.security;

import co.hotelmanager.backend.model.HuespedUsuario;
import co.hotelmanager.backend.repository.HuespedUsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil                    jwtUtil;
    private final UserDetailsServiceImpl     userDetailsService;
    private final HuespedUsuarioRepository   huespedRepo;

    @Override
    protected void doFilterInternal(
        HttpServletRequest  request,
        HttpServletResponse response,
        FilterChain         filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        try {
            final String email = jwtUtil.extractEmail(token);

            if (email != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

                // Verificar si el token es de un huésped
                String rol = jwtUtil.extractRol(token);

                if ("huesped".equals(rol)) {
                    // Autenticar como huésped
                    HuespedUsuario huesped = huespedRepo
                        .findByEmail(email)
                        .orElse(null);

                    if (huesped != null && huesped.getActivo()) {
                        UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_HUESPED"))
                            );
                        authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                        );
                        SecurityContextHolder.getContext()
                            .setAuthentication(authToken);
                    }
                } else {
                    // Autenticar como empleado/admin (flujo original)
                    UserDetails userDetails =
                        userDetailsService.loadUserByUsername(email);

                    if (jwtUtil.isTokenValid(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                            );
                        authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                        );
                        SecurityContextHolder.getContext()
                            .setAuthentication(authToken);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("JWT inválido o expirado: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}