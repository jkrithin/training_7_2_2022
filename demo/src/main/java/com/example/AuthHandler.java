package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.primitives.Longs;
import com.lambdaworks.crypto.SCryptUtil;
import io.smallrye.jwt.build.Jwt;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;

@ApplicationScoped
public class AuthHandler {

    public static final String CLAIM_KEY_COUNTRY = "country";
    public static final String CLAIM_KEY_USER_ID = "userId";

    public static final String PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlivFI8qB4D0y2jy0CfEq\n" +
            "Fyy46R0o7S8TKpsx5xbHKoU1VWg6QkQm+ntyIv1p4kE1sPEQO73+HY8+Bzs75XwR\n" +
            "TYL1BmR1w8J5hmjVWjc6R2BTBGAYRPFRhor3kpM6ni2SPmNNhurEAHw7TaqszP5e\n" +
            "UF/F9+KEBWkwVta+PZ37bwqSE4sCb1soZFrVz/UT/LF4tYpuVYt3YbqToZ3pZOZ9\n" +
            "AX2o1GCG3xwOjkc4x0W7ezbQZdC9iftPxVHR8irOijJRRjcPDtA6vPKpzLl6CyYn\n" +
            "sIYPd99ltwxTHjr3npfv/3Lw50bAkbT4HeLFxTx4flEoZLKO/g0bAoV2uqBhkA9x\n" +
            "nQIDAQAB\n" +
            "-----END PUBLIC KEY-----";
    private final static String PRIVATE_KEY =
            "-----BEGIN PRIVATE KEY-----\n" +
                    "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCWK8UjyoHgPTLa\n" +
                    "PLQJ8SoXLLjpHSjtLxMqmzHnFscqhTVVaDpCRCb6e3Ii/WniQTWw8RA7vf4djz4H\n" +
                    "OzvlfBFNgvUGZHXDwnmGaNVaNzpHYFMEYBhE8VGGiveSkzqeLZI+Y02G6sQAfDtN\n" +
                    "qqzM/l5QX8X34oQFaTBW1r49nftvCpITiwJvWyhkWtXP9RP8sXi1im5Vi3dhupOh\n" +
                    "nelk5n0BfajUYIbfHA6ORzjHRbt7NtBl0L2J+0/FUdHyKs6KMlFGNw8O0Dq88qnM\n" +
                    "uXoLJiewhg9332W3DFMeOveel+//cvDnRsCRtPgd4sXFPHh+UShkso7+DRsChXa6\n" +
                    "oGGQD3GdAgMBAAECggEAAjfTSZwMHwvIXIDZB+yP+pemg4ryt84iMlbofclQV8hv\n" +
                    "6TsI4UGwcbKxFOM5VSYxbNOisb80qasb929gixsyBjsQ8284bhPJR7r0q8h1C+jY\n" +
                    "URA6S4pk8d/LmFakXwG9Tz6YPo3pJziuh48lzkFTk0xW2Dp4SLwtAptZY/+ZXyJ6\n" +
                    "96QXDrZKSSM99Jh9s7a0ST66WoxSS0UC51ak+Keb0KJ1jz4bIJ2C3r4rYlSu4hHB\n" +
                    "Y73GfkWORtQuyUDa9yDOem0/z0nr6pp+pBSXPLHADsqvZiIhxD/O0Xk5I6/zVHB3\n" +
                    "zuoQqLERk0WvA8FXz2o8AYwcQRY2g30eX9kU4uDQAQKBgQDmf7KGImUGitsEPepF\n" +
                    "KH5yLWYWqghHx6wfV+fdbBxoqn9WlwcQ7JbynIiVx8MX8/1lLCCe8v41ypu/eLtP\n" +
                    "iY1ev2IKdrUStvYRSsFigRkuPHUo1ajsGHQd+ucTDf58mn7kRLW1JGMeGxo/t32B\n" +
                    "m96Af6AiPWPEJuVfgGV0iwg+HQKBgQCmyPzL9M2rhYZn1AozRUguvlpmJHU2DpqS\n" +
                    "34Q+7x2Ghf7MgBUhqE0t3FAOxEC7IYBwHmeYOvFR8ZkVRKNF4gbnF9RtLdz0DMEG\n" +
                    "5qsMnvJUSQbNB1yVjUCnDAtElqiFRlQ/k0LgYkjKDY7LfciZl9uJRl0OSYeX/qG2\n" +
                    "tRW09tOpgQKBgBSGkpM3RN/MRayfBtmZvYjVWh3yjkI2GbHA1jj1g6IebLB9SnfL\n" +
                    "WbXJErCj1U+wvoPf5hfBc7m+jRgD3Eo86YXibQyZfY5pFIh9q7Ll5CQl5hj4zc4Y\n" +
                    "b16sFR+xQ1Q9Pcd+BuBWmSz5JOE/qcF869dthgkGhnfVLt/OQzqZluZRAoGAXQ09\n" +
                    "nT0TkmKIvlza5Af/YbTqEpq8mlBDhTYXPlWCD4+qvMWpBII1rSSBtftgcgca9XLB\n" +
                    "MXmRMbqtQeRtg4u7dishZVh1MeP7vbHsNLppUQT9Ol6lFPsd2xUpJDc6BkFat62d\n" +
                    "Xjr3iWNPC9E9nhPPdCNBv7reX7q81obpeXFMXgECgYEAmk2Qlus3OV0tfoNRqNpe\n" +
                    "Mb0teduf2+h3xaI1XDIzPVtZF35ELY/RkAHlmWRT4PCdR0zXDidE67L6XdJyecSt\n" +
                    "FdOUH8z5qUraVVebRFvJqf/oGsXc4+ex1ZKUTbY0wqY1y9E39yvB3MaTmZFuuqk8\n" +
                    "f3cg+fr8aou7pr9SHhJlZCU=\n" +
                    "-----END PRIVATE KEY-----";
    /**
     * This is the SCrypt N factor, the work factor or hash iteration count.
     * It affects memory based on the formula:
     * memory = 128 * N * r
     */
    public static final int SCRYPT_N = 2 << 13;
    /**
     * This is the SCrypt r factor. Both CPU and memory requirements scale linearly with it:
     * it is used to convert functions with k-bits input/output to functions with (2*r*k)-bits inputs/outputs
     */
    public static final int SCRYPT_r = 5;
    /**
     * This is the SCrypt p factor.
     * It is the parallelization count, deciding how many times the overall process will be performed.
     * This is useful in case of multi-core systems, so as to increase the required CPUs to perform the work.
     * However, as our impl is sequential (and most others are) we can safely leave it to a low number,
     * as the memory and CPU requirements are already high
     */
    public static final int SCRYPT_p = 3;

    private static final Logger logger = LoggerFactory.getLogger(AuthHandler.class);
    PrivateKey jwtSignKey;
    PublicKey jwtPublicSignKey;
    @Inject
    ObjectMapper objectMapper;

    @PostConstruct
    public void init() throws Exception {

        StringBuilder pkcs8Lines = new StringBuilder();
        BufferedReader rdr = new BufferedReader(new StringReader(PRIVATE_KEY));
        String line;
        while ((line = rdr.readLine()) != null) {
            pkcs8Lines.append(line);
        }
        // Remove the "BEGIN" and "END" lines, as well as any whitespace
        String pkcs8Pem = pkcs8Lines.toString();
        pkcs8Pem = pkcs8Pem.replace("-----BEGIN PRIVATE KEY-----", "");
        pkcs8Pem = pkcs8Pem.replace("-----END PRIVATE KEY-----", "");
        pkcs8Pem = pkcs8Pem.replaceAll("\\s+","");
        // Base64 decode the result
        byte [] pkcs8EncodedBytes = Base64.getDecoder().decode(pkcs8Pem);
        // extract the private key
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privKey = kf.generatePrivate(keySpec);
        jwtSignKey = privKey ;
    }

    public String generateJwt(String username, String role) {

        Instant curr = Instant.now();
        Instant exp = curr.plusSeconds(600);
        final String iss = "jkrithin";
        logger.warn("Generating JWT token with username {} and ROLE {}",username,role);
        username = username.toLowerCase();


        return Jwt.claims()
                .subject(username)
                .groups(role)
                .upn(username)
                .issuer(iss)
                .issuedAt(curr.toEpochMilli() / 1000) //this takes seconds
                .expiresAt(exp.toEpochMilli() / 1000)
                .claim(CLAIM_KEY_COUNTRY, Strings.nullToEmpty("GREECE"))
                .jws()
                .keyId(iss)
                .sign(jwtSignKey);
    }


    public String hashPassword(String plain) {
        return SCryptUtil.scrypt(plain, SCRYPT_N, SCRYPT_r, SCRYPT_p);
    }

    public boolean checkPassword(String plain, String hash) {
        return SCryptUtil.check(plain, hash);
    }

    public static boolean hasInvalidSubject(JsonWebToken jwt) {
        return jwt == null || Strings.isNullOrEmpty(jwt.getSubject());
    }

    public static Long getUserId(JsonWebToken jwt) {
        if (jwt == null) {
            return null;
        }
        String id = jwt.getClaim(CLAIM_KEY_USER_ID);
        if (Strings.isNullOrEmpty(id)) {
            return null;
        }
        return Longs.tryParse(id);
    }

    public String getUserRole(JsonWebToken jwt) {
        return jwt == null ? null : jwt.getGroups() == null ? null : jwt.getGroups().iterator().next();
    }
}
