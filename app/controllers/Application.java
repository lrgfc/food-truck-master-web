package controllers;

import java.util.List;

import org.codehaus.jackson.node.ObjectNode;

import com.google.gson.Gson;

import models.Group;
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
        List<Group> groups = MorphiaObject.datastore.createQuery(Group.class).retrievedFields(false, "groupName").asList();
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
        return TODO;
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

    public static Result checkinFb(String fid) {
        if (authenticated(fid)) {
            return Application.checkin();
        } else {
            return Application.checkinFailed();
        }
    }

    public static Result checkinFm(String usr, String pwd) {
        if (authenticated(usr, pwd)) {
            return Application.checkin();
        } else {
            return Application.checkinFailed();
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

    public static Result reviewFb(String truckid, String fid) {

        return TODO;
    }

    public static Result reviewFm(String truckid, String usr, String pwd) {

        return TODO;
    }

    private static Result authFailed() {
        ObjectNode failed = Json.newObject();
        failed.put("error", "authentication failed");
        return badRequest(failed);
    }

    private static boolean authenticated(String fid) {
        return true;
    }

    private static boolean authenticated(String usr, String pwd) {
        return true;
    }

}