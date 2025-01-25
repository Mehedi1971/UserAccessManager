package com.mahedi.useraccessmanager.service;

import com.mahedi.useraccessmanager.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private String secretKey = null;

    public String generateToken(User user) {
        Map<String, Objects> claims =new HashMap<>();
        return Jwts
                .builder()
                .claims()
                .add(claims)
                .subject(user.getUsername())
                .issuer("mahedi")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+60*10*1000))
                .and()
                .signWith(generateKey())
                .compact();
    }

    private SecretKey generateKey() {
        byte[] decode = Decoders.BASE64.decode(getSecretKey());

        return Keys.hmacShaKeyFor(decode);
    }

    public String getSecretKey() {
        return secretKey = "6e05baf346690c949930d831d74a65a189de99b408accc6c0ab02a889633c4c341917648be5c48346ebdd4241c45019f8cdc719b4083aae5feee0f871b3ede18da2eec8eab421fac533e73152d8c197df89513f166b777e4bce1038bad6b27a6d2f06554aa228292647922654cf5ed47804a31639a1a8d825328354e0b82af7c672fdada02d832414f6f72ed9a1ff9d5fb41ea67e42a655ed3659e646e90d102acb340b9f8c6eda117c492368781a90e01952cd17bb75bd1e2d70b307fbb07ec07c585d5ea3008298fa9bde77a7cebef48cc7b6a1e824adc63d7d6f6d8629bfccbd7b5c54a88eed3d6376e3949f52d047e689b3c239f6fb9eb2ffdb86863ef42";
    }

    public String extractUserName(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    private <T> T extractClaims(String token, Function<Claims,T> claimResolver) {
        Claims claims=extractClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(generateKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);

        return (userName.equals(userDetails.getUsername())&& !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaims(token,Claims::getExpiration);
    }
}
