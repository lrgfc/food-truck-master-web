package models;

import models.Trucks.Reviews;
import play.data.validation.Constraints.Required;

import com.google.code.morphia.annotations.*;

@Entity("users")
public class User {
    @Id
    public String id;
    @Required
    public Boolean facebookUser;
    @Required
    public String email;
    @Required
    public String name;
    @Required
    public String pwd;
    @Embedded
    public Checkin[] checkins;
    
    @Embedded
    class Checkin {
        public String time;
        public String location;
        public String truck;    // truck's id
    }
    
}
