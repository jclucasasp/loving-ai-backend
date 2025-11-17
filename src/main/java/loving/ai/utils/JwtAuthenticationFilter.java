package loving.ai.utils;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import loving.ai.user.UserRepo;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private  final UserRepo userRepo;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepo userRepo) {
        this.jwtUtil = jwtUtil;
        this.userRepo = userRepo;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            Claims claims = jwtUtil.parse(token);
            String email = claims.getSubject();

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                userRepo.getUserByEmail(email).ifPresent(user -> {
                    if (jwtUtil.valid(token, email) &&
                            (user.active() == null || user.active())) {

                        var authorities = (user.roles() != null ? user.roles() : Set.of("USER"))
                                .stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .toList();

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        user,           // ← FULL USER OBJECT
                                        null,
                                        authorities
                                );
                        authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                });
            }
        } catch (Exception e) {
            // Invalid token → continue as unauthenticated
        }

        filterChain.doFilter(request, response);
    }
}
