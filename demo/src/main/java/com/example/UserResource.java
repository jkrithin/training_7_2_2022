package com.example;

import com.google.common.base.Strings;
import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.auth.LoginConfig;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.hibernate.Session;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Path("/login")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class UserResource {
    private static final Logger logger = LoggerFactory.getLogger(UserResource.class);
    @Inject
    EntityManager em;
    @Inject
    AuthHandler authHandler;
    @Inject
    JsonWebToken jwt;

    @POST
    @PermitAll
    public String login(AppUser user) {
        String validate;
        logger.warn("Login attempted by Username {}  ...",user.getUsername());

        if (Strings.isNullOrEmpty(user.getUsername())) {
            throw new BadRequestException();
        }
        JSONObject json = new JSONObject();
        validate = validateCredentials(user);
        json.put("jwt", validate);
        logger.warn("Generating JWT token {}  ...",validate);
        return json.toString();
    }

    @Transactional
    protected String validateCredentials(AppUser user) {
        Map<String,String> properties = new HashMap<>();
        AppUser existingUserInDB;
        try {
            String query = "SELECT * FROM users as a " +
                    "JOIN roles as role USING(id) WHERE :username IN (a.name,a.username)";
            Query qu = em.unwrap(Session.class).createNativeQuery(query, AppUser.class)
                    .setParameter("username",user.getUsername());
            existingUserInDB = (AppUser) qu.getSingleResult();
            logger.warn("GOT THIS : {}",existingUserInDB.getRole());
            logger.warn("username/email {} ID {} ", existingUserInDB.getUsername(),existingUserInDB.getId());
            if (existingUserInDB.areEqual(user) &&
                    authHandler.checkPassword(user.getPassword(), existingUserInDB.getPassword())) {
                return authHandler.generateJwt(existingUserInDB.getUsername(),existingUserInDB.getRole());
            } else {
                throw new BadRequestException();
            }
        } catch (Exception e) {
            logger.warn("ValidateCredentials : username : {}",user.getUsername());
            logger.warn("Exception at validateCredentials : {}",e);
            throw new BadRequestException();
        }
    }

    @GET
    @Path("test-security-jwt")
    public String testSecurityOidc(@Context SecurityContext ctx) {
        logger.warn("TESTING SECURITY with token {} ", jwt);
        logger.warn("TESTING SECURITY with token {} ", jwt.getClaimNames());
        logger.warn("TESTING SECURITY with token {} ", jwt.getGroups());
        logger.warn("TESTING SECURITY with token {}",jwt.getSubject());
        return jwt.getName() + ":" + jwt.getGroups().iterator().next();
    }

    @GET()
    @Path("roles-allowed")
    @RolesAllowed({ "user", "admin" })
    @Produces(MediaType.TEXT_PLAIN)
    public String helloRolesAllowed(@Context SecurityContext ctx) {
        Principal caller =  ctx.getUserPrincipal();
        String name = caller == null ? "anonymous" : caller.getName();
        boolean hasJWT = jwt.getClaimNames() != null;
        return String.format("hello + %s, isSecure: %s, authScheme: %s, hasJWT: %s",
                name, ctx.isSecure(), ctx.getAuthenticationScheme(), hasJWT);
    }
}
