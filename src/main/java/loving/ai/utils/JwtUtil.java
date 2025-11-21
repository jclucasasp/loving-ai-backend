package loving.ai.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Set;

@Log4j2
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-min:15}")
    private long accessMin;

    @Value("${jwt.refresh-min:10080}")
    private long refreshMin;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String accessToken(String email, Set<String> roles) {
        return Jwts.builder()
                .subject(email)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessMin * 60L * 1000L))
                .signWith(key())
                .compact();
    }

    public String refreshToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshMin * 60L * 1000L))
                .signWith(key())
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean valid(String token, String email) {
         try {
        Claims c = parse(token);
        boolean valid = c.getSubject().equals(email) && c.getExpiration().after(new Date());
        log.debug("JWT valid check: subject match = {}, not expired = {}", c.getSubject().equals(email), valid);
        return valid;
    } catch (ExpiredJwtException e) {
        log.warn("Expired token for {}", email);
        return false;
    } catch (SignatureException e) {
        log.warn("Invalid signature for {}", email);
        return false;
    } catch (Exception e) {
        log.warn("JWT validation error: {}", e.getMessage());
        return false;
    }
    }
}
