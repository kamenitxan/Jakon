package cz.kamenitxan.jakon.core.model.Dao;

import com.mongodb.MongoClient;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

/**
 * Created by TPa on 05.06.16.
 */
public abstract class MongoHelper {
	private static final Morphia morphia = new Morphia();
	private static Datastore datastore;
	private static String dbName;
	private static String host = "localhost";
	private static int port = 27017;

	public static void macClass(Class...  classes) {
		morphia.map(classes);
	}

	public static void mapPackage(String packageName) {
		morphia.mapPackage(packageName);
	}

	public static Datastore createDatastore(MongoClient mongoClient) {
		datastore = morphia.createDatastore(mongoClient, dbName);
		return datastore;
	}

	public static Datastore getDatastore() {
		if (datastore == null) {
			datastore = morphia.createDatastore(new MongoClient(host, port), dbName);
		}
		return datastore;
	}

	public static void setDbName(String dbName) {
		MongoHelper.dbName = dbName;
	}

	public static void setHost(String host) {
		MongoHelper.host = host;
	}

	public static void setPort(int port) {
		MongoHelper.port = port;
	}
}
