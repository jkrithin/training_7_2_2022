package com.example;

import org.flywaydb.core.Flyway;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MigrationService {
        // You can Inject the object if you want to use it manually
        //@Inject
        //Flyway flyway;
        //@Inject
        //@FlywayDataSource("inventory")
        //Flyway flywayForInventory;
        //@Inject
        //@Named("flyway_users")
        //Flyway flywayForUsers;
        //public void checkMigration() {
            // Use the flyway instance manually
            //flyway.clean();
            //flyway.migrate();
            // This will print 1.0.0
            //System.out.println(flyway.info().current().getVersion().toString());
        //}
}

