package com.chatterbox.api_rest.security;

import com.chatterbox.api_rest.dto.UsuarioBdDto;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private final Key key = Keys.hmacShaKeyFor("una_vuelta_por_los_andamios_de_cuenca".getBytes(StandardCharsets.UTF_8));
    private final JwtParser parser = Jwts.parser().verifyWith((SecretKey) key).build();

    public String generateToken(UsuarioBdDto usuario) {
        return Jwts.builder()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hora
                .subject(usuario.getEmail())
                .claim("id_usuario", usuario.getId_usuario())
                .claim("apodo", usuario.getApodo())
                .claim("nombre_usuario", usuario.getNombre_usuario())
                .claim("es_admin_general", usuario.isEs_admin_general())
                .signWith(key)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return parser.parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean isTokenExpired(String token) {
        Date expiration = parser.parseSignedClaims(token).getPayload().getExpiration();
        return expiration.before(new Date());
    }

    public boolean validateToken(String token, UsuarioBdDto usuario) {
        try {
            String email = getEmailFromToken(token);
            return email.equals(usuario.getEmail()) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
