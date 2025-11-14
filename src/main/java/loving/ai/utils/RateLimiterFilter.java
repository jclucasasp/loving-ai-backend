package loving.ai.utils;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimiterFilter extends OncePerRequestFilter {
    private final Bucket bucket;

    public RateLimiterFilter(
            @Value("${rate.limiting.bucket.capacity:20}") long capacity,
            @Value("${rate.limiting.bucket.tokens:20}") long tokens,
            @Value("${rate.limiting.bucket.refill-interval:1}") long refillInterval
    ) {
        this.bucket = Bucket.builder()
                .addLimit(Bandwidth.classic(capacity, Refill.greedy(tokens, Duration.ofMinutes(refillInterval))))
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/api/chat")) {
            if (!bucket.tryConsume(1)) {
                response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
                response.getWriter().write("Too many requests! Please try again later");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
