package org.nature.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTokenUtil {

    /**
     * Token请求头
     */
    public static final String TOKEN_HEADER = "Authorization";
    /**
     * Token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 签名主题
     */
    public static final String SUBJECT = "nature";
    /**
     * 过期时间 7天
     */
    public static final long EXPIRE_TIME = 1000 * 24 * 60 * 60 * 7;
    /**
     * 应用密钥
     */
    public static final String SECRET_KET = "nature_secret";
    /**
     * 角色权限声明
     */
    private static final String ROLE_CLAIMS = "role";

    /**
     * 生成Token
     */
    public static String createToken(String username, String role) {
        Map<String, Object> map = new HashMap<>();
        map.put(ROLE_CLAIMS, role);

        return Jwts
                .builder()
                .setSubject(username)
                .setClaims(map)
                .claim("username", username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KET).compact();
    }

    /**
     * 校验Token
     */
    public static Claims checkJWT(String token) {
        return Jwts.parser().setSigningKey(SECRET_KET).parseClaimsJws(token).getBody();
    }

    /**
     * 从Token中获取username
     */
    public static String getUsername(String token) {
        Claims claims = Jwts.parser().setSigningKey(SECRET_KET).parseClaimsJws(token).getBody();
        return claims.get("username").toString();
    }

    /**
     * 从Token中获取用户角色
     */
    public static String getUserRole(String token) {
        Claims claims = Jwts.parser().setSigningKey(SECRET_KET).parseClaimsJws(token).getBody();
        return claims.get("role").toString();
    }

    /**
     * 校验Token是否过期
     */
    public static boolean isExpiration(String token) {
        Claims claims = Jwts.parser().setSigningKey(SECRET_KET).parseClaimsJws(token).getBody();
        return claims.getExpiration().before(new Date());
    }
}
