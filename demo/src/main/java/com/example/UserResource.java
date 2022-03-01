package com.example;

import com.google.common.base.Strings;
import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.auth.LoginConfig;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.hibernate.Session;
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
    private static final Logger logger= LoggerFactory.getLogger(UserResource.class);
    @Inject
    EntityManager em;
    @Inject AuthHandler authHandler;
    @Inject
    JsonWebToken jwt;



    @POST
    @PermitAll
    public String login(AppUser user) {
        logger.warn("Login attempted by Username {} with pass {} ...",
                               user.getUsername(),user.getPassword());
        if (Strings.isNullOrEmpty(user.getUsername())){
            throw new BadRequestException();
        }
        return validateCredentials(user);
    }

    @Transactional
    protected String validateCredentials(AppUser user){
        Map<String,String> properties = new HashMap<>();
        AppUser existingUserInDB;
        try {
            String query = "SELECT * FROM users WHERE :username IN (name,username)";
            Query qu = em.unwrap(Session.class).createNativeQuery(query, AppUser.class).setParameter("username",user.getUsername());
            existingUserInDB = (AppUser) qu.getSingleResult();
            logger.warn("Data from DB : username/email {} ID {} ", existingUserInDB.getUsername(),existingUserInDB.getId());
            if( existingUserInDB.areEqual(user) ) {
                query = "SELECT name FROM roles WHERE id=:userId";
                qu = em.unwrap(Session.class).createNativeQuery(query).setParameter("userId",existingUserInDB.getId());
                logger.warn("ROLE: {} ",qu.getSingleResult());
                return authHandler.generateJwt(existingUserInDB.getUsername(),qu.getSingleResult().toString());
            }else{
                throw new BadRequestException();
            }
        }catch (Exception e){
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
        return String.format("hello + %s, isSecure: %s, authScheme: %s, hasJWT: %s", name, ctx.isSecure(), ctx.getAuthenticationScheme(), hasJWT);
    }
}
