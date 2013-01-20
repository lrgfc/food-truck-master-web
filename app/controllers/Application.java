package controllers;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;
import org.codehaus.jackson.node.ObjectNode;

import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.google.gson.Gson;

import models.*;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import org.codehaus.jackson.JsonNode;
import play.mvc.*;

public class Application extends Controller {

    static Form<Group> groupForm = form(Group.class);

    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result getTrucks() {		 
        Gson gson = new Gson();
        List<Truck> groups = MorphiaObject.datastore.createQuery(Truck.class).retrievedFields(false,"reviews").limit(10).asList();
        String json = gson.toJson(groups);
        String output = removeReviews(json);
        //        System.out.println(y);
        return ok(output);
    }


    private static String removeReviews(String json) {
        String input = json;
        Pattern r = Pattern.compile(",\"reviews\":\\[(\\{.*\\})*\\]\\}");
        Matcher m = r.matcher(input);
        String output = m.replaceAll("}");
        return output;
    }


    public static Result getTrucksByType(String genre) {
        Gson gson = new Gson();
        Pattern regexp = Pattern.compile(genre);
        List<Truck> truck = MorphiaObject.datastore.createQuery(Truck.class).filter("genre", regexp).retrievedFields(false, "reviews").asList();
        //            List<Truck> truck = MorphiaObject.datastore.find(Truck.class)
        //                            .field("genre").equal(genre).retrievedFields(false, "reviews").asList();
        String json = gson.toJson(truck);
        String output = removeReviews(json);
        return ok(output);
    }

    public static Result getNearByTrucks(String lon, String lat) {
        Gson gson = new Gson();
        List<Truck> truck = MorphiaObject.datastore.find(Truck.class)
                .field("location")
                .near(Double.parseDouble(lon), Double.parseDouble(lat), 0.0001)
                .limit(5).retrievedFields(false, "reviews").asList();
        String json = gson.toJson(truck);
        String output = removeReviews(json);
        return ok(output);
    }

    public static Result getTopTrucks(String rank) {   
        Gson gson = new Gson();
        List<Truck> truck = MorphiaObject.datastore.find(Truck.class)
                .order("-averageStar").retrievedFields(false, "reviews").limit(Integer.parseInt(rank)).asList();
        String json = gson.toJson(truck);
        String output = removeReviews(json);
        return ok(output);
    } 


    public static Result getTruckById(String truckid) {
        Gson gson = new Gson();
        Truck truck = MorphiaObject.datastore.find(Truck.class).field("_id")
                .equal(new ObjectId(truckid)).get();
        String json = gson.toJson(truck);
        return ok(json);
    }

    public static Result index() throws Exception {
        // redirect to the "group Result
        return redirect(routes.Application.group());
    }

    public static Result group() {
        return ok(views.html.index.render(Group.all(), groupForm));
    }

    public static Result newGroup() {
        Form<Group> filledForm = groupForm.bindFromRequest();
        if(filledForm.hasErrors()) {
            return badRequest(views.html.index.render(Group.all(), filledForm));
        } else {
            Group.create(filledForm.get());
            return redirect(routes.Application.group());  
        }
    }

    public static Result deleteGroup(String id) {
        Group.delete(id);
        return redirect(routes.Application.group());
    }

    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result checkinFb(String fid) {
        if (authenticated(fid)) {
            return Application.checkin();
        } else {
            return Application.authFailed();
        }
    }

    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result checkinFm(String usr, String pwd) {
        if (authenticated(usr, pwd)) {
            return Application.checkin();
        } else {
            return Application.authFailed();
        }
    }

    private static Result checkin() {
        JsonNode json = request().body().asJson();
        if (json == null) {
            return Application.checkinFailed();
        } else {
            JsonNode loc = json.findPath("location");
            long time = System.currentTimeMillis();
            double[] location = {loc.get(0).asDouble(), loc.get(1).asDouble()};
            String tid = json.findPath("truckid").getTextValue();
            System.out.println("!!!!! " + tid);
            Truck truck= MorphiaObject.datastore.createQuery(Truck.class).retrievedFields(true, "name").field("_id").equal(new ObjectId(tid)).get();
            User.Checkin checkin = new User.Checkin(time, location, tid, truck.name);
            System.out.println(checkin.time);
            // create update query and operation
            String fid = json.findPath("fid").getTextValue();
            Query<User> updateQuery = MorphiaObject.datastore.createQuery(User.class).field("_id").equal(new ObjectId(fid));
            UpdateOperations<User> ops = MorphiaObject.datastore.
                    createUpdateOperations(User.class).add("checkins", checkin);
            // update User
            MorphiaObject.datastore.update(updateQuery, ops);            

            ObjectNode ok = Json.newObject();
            ok.put("fid", fid);
            ok.put("tid", tid);
            return ok(ok);
        }
    }

    public static Result getHistoryFb(String fid) {
        if (authenticated(fid)) {
            return Application.getHistory(fid);
        } else {
            return Application.authFailed();
        }

    }

    public static Result getHistoryFm(String usr, String pwd) {
        if (authenticated(usr, pwd)) {
            return Application.getHistory(usr);
        } else {
            return Application.authFailed();
        }

    }

    private static Result getHistory(String fid) {
        Gson gson = new Gson();
        User user = MorphiaObject.datastore.createQuery(User.class).field("_id").equal(new ObjectId(fid)).retrievedFields(false, "pwd", "facebookUser").get();
        String json = gson.toJson(user);
        return ok(json);
    }

    public static Result checkinFailed() {
        return Application.authFailed();
    }

    public static Result reviewFailed(String str) {
        return Application.authFailed();
    }

    public static Result getHistoryFailed() {
        return Application.authFailed();
    }

    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result reviewFb(String truckid, String fid) {
        if (authenticated(fid)) {
            return Application.review(truckid);
        } else {
            return Application.authFailed();
        }
        //        return Application.review(truckid);     // TODO: delete
    }

    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result reviewFm(String truckid, String usr, String pwd) {
        if (authenticated(usr, pwd)) {
            return Application.review(truckid);
        } else {
            return Application.authFailed();
        }
    }

    private static Result review(String truckid) {
        JsonNode json = request().body().asJson();
        if (json == null) {
            return Application.reviewFailed(truckid);
        } else {
            // construct truckid
            ObjectId tid = new ObjectId(truckid);
            // get new average_star and review_count 
            Truck truck = MorphiaObject.datastore.createQuery(Truck.class).retrievedFields(true, "reviewCount", "averageStar").field("_id").equal(tid).get();
            System.out.println(truck.toString());
            int star = json.findPath("star").asInt();
            double average_star = (truck.averageStar * truck.reviewCount + star) 
                    / (truck.reviewCount + 1);
            // construct new review
            String fid = json.findPath("fid").getTextValue();
            String name = json.findPath("name").getTextValue();
            String comment = json.findPath("comment").getTextValue();
            String entree = json.findPath("entree").getTextValue();
            Truck.Review review = new Truck.Review(fid, name, star, comment, entree);
            // create update query and operation for truck
            Query<Truck> qTruck = MorphiaObject.datastore.createQuery(Truck.class).field("_id").equal(tid);
            UpdateOperations<Truck> opsTruck = MorphiaObject.datastore.
                    createUpdateOperations(Truck.class).set("averageStar", average_star).inc("reviewCount").add("reviews", review);
            // update truck
            MorphiaObject.datastore.update(qTruck, opsTruck);
            // create update query and operation for user
            Query<User> qUser = MorphiaObject.datastore.createQuery(User.class).field("_id").equal(new ObjectId(fid));
            List<User.Checkin> checkins = qUser.get().checkins;
            System.out.println(checkins);
            for (User.Checkin c : checkins) {
                if (truckid.equals(c.tid) && c.reviewed == false) {
                    //                    System.out.println("set reviewed to true");
                    c.reviewed = true;
                }// else System.out.println("Fuck :-)");
            }
            UpdateOperations<User> opsUser = MorphiaObject.datastore.
                    createUpdateOperations(User.class).set("checkins", checkins);
            // update User
            MorphiaObject.datastore.update(qUser, opsUser); 
            return ok();
        }

    }

    private static Result authFailed() {
        ObjectNode failed = Json.newObject();
        failed.put("error", "authentication failed");
        return badRequest(failed);
    }

    private static boolean authenticated(String fid) {
        if (fid == null) return false;
        User user = MorphiaObject.datastore.get(User.class, new ObjectId(fid));
        return user != null;
    }

    private static boolean authenticated(String usr, String pwd) {
        if (usr == null || pwd == null) return false;
        User user = MorphiaObject.datastore.get(User.class, new ObjectId(usr));
        if (user == null) return false;
        else return pwd.equals(user.pwd);
    }
    
    private static Result registerFailed() {
        ObjectNode failed = Json.newObject();
        failed.put("response", "Error");
        return badRequest(failed);
    }
    
    private static Result registerDuplicate() {
        ObjectNode failed = Json.newObject();
        failed.put("response", "Account already exists");
        return badRequest(failed);
    }

    public static Result register() {
        JsonNode json = request().body().asJson();
        if (json == null) {
            return Application.registerFailed();
        } else {
            User user = new User();
            String email = json.findPath("email").getTextValue();
            String name = json.findPath("name").getTextValue();
            String pwd = json.findPath("pwd").getTextValue();
            boolean facebookUser = json.findPath("facebookUser").asBoolean();
            if (!facebookUser) {
                if (email == null || name == null || pwd == null) {
                    return Application.registerFailed();
                } else {
                    if (Application.authenticated(email, pwd))
                        return Application.registerDuplicate();
                    user.email = email;
                    user.name = name;
                    user.pwd = pwd;
                }
            }
            user.id = email;
            user.facebookUser = facebookUser;
            MorphiaObject.datastore.save(user);
            ObjectNode ok = Json.newObject();
            ok.put("response", "OK");
            return ok(ok);
        }
    }

}