package models;

import java.util.ArrayList;
import java.util.List;

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
    public List<Checkin> checkins = new ArrayList<Checkin>();
    
    @Embedded
    public static class Checkin {
        public Checkin(){}
        public Checkin(long time, double[] location, String tid, String name) {
            this.time = time;
            this.location = location;
            this.tid = tid;
            this.name = name;
            this.reviewed = false;
        }
        
        public long time;
        public double[] location;
        public String tid;    // truck's id
        public String name;
        public boolean reviewed; 
    }
    
}
