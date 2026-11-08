package ucr.ac.cr.medivault.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    private static final int LOGIN_LIMIT = 5;  
    private static final int API_LIMIT = 30;    
    private static final int MAX_IPS = 10_000;  

    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> apiBuckets = new ConcurrentHashMap<>();

    private Bucket nuevoBucket(int limite) {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(limite, Refill.intervally(limite, Duration.ofMinutes(1))))
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String ip = ClientIpResolver.resolve(request);
        Bucket target = null;

        if (path.startsWith("/api/v1/auth/login")) {
            if (loginBuckets.size() > MAX_IPS) loginBuckets.clear();
            target = loginBuckets.computeIfAbsent(ip, k -> nuevoBucket(LOGIN_LIMIT));
        } else if (path.startsWith("/api/v1/expedientes")) {
            if (apiBuckets.size() > MAX_IPS) apiBuckets.clear();
            target = apiBuckets.computeIfAbsent(ip, k -> nuevoBucket(API_LIMIT));
        }

        if (target == null || target.tryConsume(1)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setHeader("Retry-After", "60");
        response.getWriter().write("""
                {"type":"https://medivault.ucr.ac.cr/rate-limit",\
                "title":"Demasiadas Peticiones","status":429,\
                "detail":"Limite superado. Espere 60s."}""");
    }
}