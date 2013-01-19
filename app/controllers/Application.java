package controllers;

import java.util.List;

import org.codehaus.jackson.node.ObjectNode;

import com.google.gson.Gson;

import models.Group;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {
	static Form<Group> groupForm = form(Group.class);

//	 @BodyParser.Of(Json.class)
	public static Result getTrucks() {		 
		 Gson gson = new Gson();
		 List<Group> groups = MorphiaObject.datastore.createQuery(Group.class).retrievedFields(false, "groupName").asList();
		 String json = gson.toJson(groups);
		 
		 return ok(json);
	}

	// "name": String, 
 //        "location": [Number, Number],
 //        "address": String,
 //        "phone": String,
 //        "genre": String,
 //        “photo”: String,
 //        “average”: Number,
 //        “count”: Number,

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

}