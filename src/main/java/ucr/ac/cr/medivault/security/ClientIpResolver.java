package ucr.ac.cr.medivault.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

public final class ClientIpResolver {
    private static final Pattern IP = Pattern.compile("^[0-9a-fA-F:.]{3,45}$");

    private ClientIpResolver() {
    }

    public static String resolve(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String first = xff.split(",")[0].trim();
            if (IP.matcher(first).matches()) {
                return first;
            }
        }
        return req.getRemoteAddr();
    }
}