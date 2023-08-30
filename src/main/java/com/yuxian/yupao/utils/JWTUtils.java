package com.yuxian.yupao.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JWTUtils {
    //密钥
    private static final String SING = "dshaw";
    /**
     * 生成token
     */
    public static String getToken(Map<String,String> map){
        Calendar instance = Calendar.getInstance();
        //默认3天过期
        instance.add(Calendar.DATE,3);
        //创建jwt builder
        JWTCreator.Builder builder = JWT.create();
        Optional.of(map).orElse(new HashMap<>()).forEach(builder::withClaim);
        String token = builder.withExpiresAt(instance.getTime())//有效期
                .sign(Algorithm.HMAC256(SING));//密钥
        return token;
    }
    /**
     * 验证token合法性
     */
    public static DecodedJWT verify(String token){
        //返回验证结果（结果是内置的）
        return JWT.require(Algorithm.HMAC256(SING)).build().verify(token);
    }
}