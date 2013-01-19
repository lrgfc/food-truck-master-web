package controllers;

import java.util.List;

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
import org.codehaus.jackson.node.ObjectNode;
import play.mvc.*;
import play.libs.Json;

public class Application extends Controller {

    static Form<Group> groupForm = form(Group.class);

    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result getTrucks() {		 
        Gson gson = new Gson();
        List<Truck> groups = MorphiaObject.datastore.createQuery(Truck.class).retrievedFields(false, "reviews").limit(10).asList();
        String json = gson.toJson(groups);

        return ok(json);
    }


        public static Result getTrucksByType(String genre) {
            Gson gson = new Gson();
            List<Truck> truck = MorphiaObject.datastore.find(Truck.class)
                            .field("genre").equal(genre).retrievedFields(false, "reviews").asList();
            String json = gson.toJson(truck);
            return ok(json);
    }
    
    public static Result getNearByTrucks(String lon, String lat) {
        Gson gson = new Gson();
        List<Truck> truck = MorphiaObject.datastore.find(Truck.class)
                        .field("location")
                        .near(Double.parseDouble(lon), Double.parseDouble(lat), 0.0001)
                        .limit(5).retrievedFields(false, "reviews").asList();
        String json = gson.toJson(truck);
        return ok(json);
    }

    public static Result getTopTrucks(String rank) {   
        Gson gson = new Gson();
        List<Truck> truck = MorphiaObject.datastore.find(Truck.class)
                        .order("-averageStar").retrievedFields(false, "reviews").limit(Integer.parseInt(rank)).asList();
        String json = gson.toJson(truck);
        return ok(json);
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
            String name = json.findPath("fid").getTextValue();
            String truckid = json.findPath("truckid").getTextValue();
            ObjectNode ok = Json.newObject();
            ok.put("name", name);
            ok.put("truckid", truckid);
            return ok(ok);
        }
    }

    public static Result checkinFailed() {
        return authFailed();
    }

    public static Result reviewFailed(String str) {
        return authFailed();
    }

    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result reviewFb(String truckid, String fid) {
//        if (authenticated(fid)) {
//            return Application.review(truckid);
//        } else {
//            return Application.authFailed();
//        }
        return Application.review(truckid);     // TODO: delete
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
            Truck truck = MorphiaObject.datastore.createQuery(Truck.class).retrievedFields(true, "average_star", "review_count").field("_id").equal(tid).get();
            int star = json.findPath("star").asInt();
            double average_star = (truck.averageStar * truck.reviewCount + star) 
                    / (truck.reviewCount + 1);
            // construct new review
            String fid = json.findPath("fid").getTextValue();
            String name = json.findPath("name").getTextValue();
            String comment = json.findPath("comment").getTextValue();
            String entree = json.findPath("entree").getTextValue();
            Truck.Review review = truck.new Review(fid, name, star, comment, entree);
            // create update query and operation
            Query<Truck> updateQuery = MorphiaObject.datastore.createQuery(Truck.class).field("_id").equal(tid);
            UpdateOperations<Truck> ops = MorphiaObject.datastore.
                    createUpdateOperations(Truck.class).set("average_star", average_star).inc("review_count").add("reviews", review);
            // update truck
            MorphiaObject.datastore.update(updateQuery, ops);            
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
        User user = MorphiaObject.datastore.get(User.class, fid);
        return user != null;
    }

    private static boolean authenticated(String usr, String pwd) {
        if (usr == null || pwd == null) return false;
        User user = MorphiaObject.datastore.get(User.class, usr);
        if (user == null) return false;
        else return pwd.equals(user.pwd);
    }

}