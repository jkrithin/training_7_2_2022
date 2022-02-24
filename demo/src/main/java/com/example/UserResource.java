package com.example;

import com.google.common.base.Strings;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

@Path("/login")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Transactional
public class UserResource {
    private static final Logger logger= LoggerFactory.getLogger(UserResource.class);
    @Inject
    EntityManager em;

    @POST
    @PermitAll
    public String login(AppUser user) {
        logger.warn("Login attempted by Username {} with pass {} ...",
                               user.getUsername(),user.getPassword());
        if (Strings.isNullOrEmpty(user.getUsername())){
            throw new BadRequestException();
        }
//        return validateCredentials(user);
        return "true";


    }

    private boolean validateCredentials(AppUser user){
        Map<String,String> properties = new HashMap<>();
        AppUser existingUserInDB;
        try {
            String query = "SELECT * FROM users WHERE :username IN (name,username)";
            Query qu = em.unwrap(Session.class).createNativeQuery(query, AppUser.class).setParameter("username",user.getUsername());
            existingUserInDB = (AppUser) qu.getSingleResult();
            logger.warn("Data from DB : username/email {} ", existingUserInDB.getUsername());
            return existingUserInDB.areEqual(user);
        }catch (Exception e){
            throw new BadRequestException();
        }
    }
}
