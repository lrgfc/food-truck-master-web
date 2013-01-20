
import java.net.UnknownHostException;

import play.GlobalSettings;
import play.Logger;

import com.google.code.morphia.Morphia;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;

import controllers.MorphiaObject;

public class Global extends GlobalSettings {

	@Override
	public void onStart(play.Application arg0) {
		super.beforeStart(arg0);
		Logger.debug("** onStart **"); 
		try {
//			 MorphiaObject.mongo = new Mongo("127.0.0.1", 27017);
			MorphiaObject.mongo = new Mongo(new MongoURI("mongodb://root:root@linus.mongohq.com:10094/foodtruck"));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		MorphiaObject.morphia = new Morphia();
                MorphiaObject.datastore = MorphiaObject.morphia.createDatastore(MorphiaObject.mongo, "foodtruck", "root", new String("root").toCharArray());
//                MorphiaObject.datastore = MorphiaObject.morphia.createDatastore(MorphiaObject.mongo, "foodtruck");
		MorphiaObject.datastore.ensureIndexes();   
		MorphiaObject.datastore.ensureCaps();  

		Logger.debug("** Morphia datastore: " + MorphiaObject.datastore.getDB());
	}
}
