package com.chatterbox.api_rest.security;

import com.chatterbox.api_rest.dto.usuario.UsuarioBdDto;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

/*
En el futuro añadir refresh token para que sea más segura y fluida la app
 */
@Component
public class JwtUtil {
    private final Key key = Keys.hmacShaKeyFor("una_vuelta_por_los_andamios_de_cuenca".getBytes(StandardCharsets.UTF_8));
    private final JwtParser parser = Jwts.parser()
            .verifyWith((SecretKey) key)
            .build();

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    // Uso UsuarioBdDto porque es el único DTO de Usuario con todos los campos
    public String generateToken(UsuarioBdDto usuario) {
        return Jwts.builder()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .subject(String.valueOf(usuario.getId_usuario()))
                .claim("apodo", usuario.getApodo())
                .claim("nombre_usuario", usuario.getNombre_usuario())
                .claim("email", usuario.getEmail())
                .claim("rol", usuario.isEs_admin_general() ? "ADMIN_GENERAL" : "USUARIO_NORMAL")
                .signWith(key)
                .compact();
    }

    public String getIdFromToken(String token) {
        return parser.parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String getEmailFromToken(String token) {
        return parser.parseSignedClaims(token)
                .getPayload()
                .get("email", String.class);
    }

    public List<GrantedAuthority> getAuthoritiesFromToken(String token) {
        String rol = parser.parseSignedClaims(token)
                .getPayload()
                .get("rol", String.class);
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    public boolean validateToken(String token, UsuarioBdDto usuario) {
        try {
            String idUsuario = getIdFromToken(token);
            String email = getEmailFromToken(token);
            return idUsuario.equals(String.valueOf(usuario.getId_usuario())) && email.equals(usuario.getEmail());
        } catch (Exception e) {
            return false;
        }
    }
}
