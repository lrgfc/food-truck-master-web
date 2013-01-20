package models;

import java.util.ArrayList;
import java.util.List;

import play.data.validation.Constraints.Required;


import com.google.code.morphia.annotations.*;

@Entity("trucks")
public class Truck {

	@Id
	public String id;
	@Required
	public String name;
	public String address;
	public double[] location;
	public String phone;
	public String genre;
	public String photo;
	public double averageStar;
	public int reviewCount;
	@Embedded
	public List<Review> reviews = new ArrayList<Review>();
	
	@Embedded
	public static class Review {
	        public Review(){};
	        public Review (String fid, String name, 
	                int star, String comments, String entree){
	            this.fid = fid;
	            this.name = name;
	            this.star = star;
	            this.comments = comments;
	            this.entree = entree;
	        }
		public String fid;
		public String name;
		public int star;
		public String comments;
		public String entree;
	}
	
}
