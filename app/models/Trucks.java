package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import play.Logger;
import play.data.validation.Constraints.Required;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

import controllers.MorphiaObject;

@Entity("trucks")
public class Trucks {

	@Id
	public String id;
	@Required
	public String name;
	public String address;
	public double[] location;
	public String phone;
	public String genre;
	public String photo;
	public double average_star;
	public int review_count;
	public Reviews[] reviews;
	class Reviews {}
	
}
