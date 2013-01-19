package controllers;

import java.util.List;

import org.codehaus.jackson.node.ObjectNode;

import com.google.code.morphia.query.Query;
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
        List<Truck> groups = MorphiaObject.datastore.createQuery(Truck.class).retrievedFields(false, "groupName").limit(10).asList();
        String json = gson.toJson(groups);

        return ok(json);
    }


    public static Result getTrucksByType(String genre) {           
        return TODO;
    }

    public static Result getNearByTrucks(String lon, String lat) {           
        return TODO;
    }

    public static Result getTopTrucks(String rank) {           
        return TODO;
    } 

    public static Result getTruckById(String truckid) {
    	Gson gson = new Gson();
    	Truck truck = MorphiaObject.datastore.get(Truck.class,truckid);
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
        if (authenticated(fid)) {
            return Application.review(truckid);
        } else {
            return Application.authFailed();
        }
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
            String fid = json.findPath("fid").getTextValue();
            int star = json.findPath("star").asInt();
            String comment = json.findPath("comment").getTextValue();
            String entree = json.findPath("entree").getTextValue();
            Trucks truck = MorphiaObject.datastore.get(Trucks.class, truckid);
            truck.average_star = (truck.average_star * truck.review_count + star) 
                    / (truck.review_count + 1);
            truck.review_count += 1;
           
//            MorphiaObject.datastore.update(truckid, truck);
            
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