package gov.tna.discovery.taxonomy;

import org.apache.lucene.util.Version;

public final class CatConstants {
	public static final Version LUCENE_VERSION = Version.LUCENE_47;
	public static String MONGO_TAXONOMY_DB = "taxonomy";
	public static String IAVIEW_INDEX = "/opt/solr/solr/collection1/data/index";
	public static String TRAINING_INDEX = "/opt/solr/solr/collection2/data/index";
	public static int MONGO_PORT = 27017;
	public static String MONGO_HOST = "localhost";
//	public static String MONGO_HOST = "***REMOVED***.***REMOVED***";
	

//	public static int MONGO_PORT = 27017;
//	public static String MONGO_HOST = "localhost";
//	public static final String IA_INDEX = "C:/IAIndex";
//	public static final String TRAINING_INDEX = "C:/TrainingIndex";
}
