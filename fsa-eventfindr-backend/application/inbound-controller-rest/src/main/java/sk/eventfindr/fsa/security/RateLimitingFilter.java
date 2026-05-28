package sk.eventfindr.fsa.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
class RateLimitingFilter extends OncePerRequestFilter {

    private static final long WINDOW_MILLIS = 60_000;
    private static final int DEFAULT_MUTATION_LIMIT = 120;
    private static final int MEDIA_UPLOAD_LIMIT = 20;

    private final Map<String, RequestWindow> windows = new ConcurrentHashMap<>();
    private final Clock clock;

    RateLimitingFilter() {
        this(Clock.systemUTC());
    }

    RateLimitingFilter(Clock clock) {
        this.clock = clock;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!isLimited(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        int limit = limitFor(request);
        String key = clientKey(request);
        long now = clock.millis();
        RequestWindow window = windows.compute(key, (ignored, current) -> {
            if (current == null || now >= current.expiresAtMillis()) {
                return new RequestWindow(now + WINDOW_MILLIS);
            }
            return current;
        });

        if (window.count().incrementAndGet() > limit) {
            reject(response);
            return;
        }

        if (windows.size() > 10_000) {
            windows.entrySet().removeIf(entry -> now >= entry.getValue().expiresAtMillis());
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLimited(HttpServletRequest request) {
        return switch (request.getMethod()) {
            case "POST", "PUT", "DELETE" -> true;
            default -> false;
        };
    }

    private int limitFor(HttpServletRequest request) {
        String path = request.getRequestURI().toLowerCase(Locale.ROOT);
        if (path.contains("/media")) {
            return MEDIA_UPLOAD_LIMIT;
        }
        return DEFAULT_MUTATION_LIMIT;
    }

    private String clientKey(HttpServletRequest request) {
        return clientIp(request) + ":" + request.getMethod() + ":" + request.getRequestURI();
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("""
                {"code":"RATE_LIMITED","message":"Too many requests","details":["Please retry later"]}
                """);
    }

    private record RequestWindow(long expiresAtMillis, AtomicInteger count) {
        private RequestWindow(long expiresAtMillis) {
            this(expiresAtMillis, new AtomicInteger());
        }
    }
}
