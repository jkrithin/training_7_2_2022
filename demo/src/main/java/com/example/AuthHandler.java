package com.example;


import com.google.common.base.Strings;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.Longs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.lambdaworks.crypto.SCryptUtil;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import io.smallrye.jwt.build.Jwt;
import org.eclipse.microprofile.jwt.JsonWebToken;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;

@ApplicationScoped
public class AuthHandler {
    public static final String CLAIM_KEY_USER_ID = "userId";
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
    public static final int SCRYPT_r = 8;
    /**
     * This is the SCrypt p factor.
     * It is the parallelization count, deciding how many times the overall process will be performed.
     * This is useful in case of multi-core systems, so as to increase the required CPUs to perform the work.
     * However, as our impl is sequential (and most others are) we can safely leave it to a low number,
     * as the memory and CPU requirements are already high
     */
    public static final int SCRYPT_p = 2;

    private static final Logger logger = LoggerFactory.getLogger(AuthHandler.class);
    PrivateKey jwtSignKey;


    @PostConstruct
    public void init() throws Exception {
        jwtSignKey = KeyFactory.getInstance("RSA").generatePrivate(
                new PKCS8EncodedKeySpec(Base64.getDecoder().decode("kalinuxta")));
    }

    public String generateJwt(long userId, String username, String role) {

        Instant curr = Instant.now();
        Instant exp = curr.plusSeconds(600);
        final String iss = "ME";
        username = username.toLowerCase();


        return Jwt.claims()
                .subject(username)
                .groups(role)
                .upn(username)
                .issuer(iss)
                .issuedAt(curr.toEpochMilli() / 1000) //this takes seconds
                .expiresAt(exp.toEpochMilli() / 1000)
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
