package taxonomy;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.reverse.ReverseStringFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.TokenFilter;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class Indexer {

	public Indexer() {
	}

	
	
	public IndexWriter getIndexWriter(boolean create, String indexDirectory)
			throws IOException {

		IndexWriter indexWriter = null;

		if (indexWriter == null) {
			Analyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_44);
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_44,
					analyzer);
			File file = new File(indexDirectory);
			SimpleFSDirectory index = new SimpleFSDirectory(file);
			indexWriter = new IndexWriter(index, config);
		}
		return indexWriter;
	} 

	public void buildTrainingIndex() throws IOException {

		MongoClient mongoClient = new MongoClient("localhost", 27017);
		DB db = mongoClient.getDB("trainingset");
		DBCollection collection = db.getCollection("categories");
		DBCursor cursor = collection.find();
		try {
			while (cursor.hasNext()) {
				BasicDBObject dbObject = (BasicDBObject) cursor.next();
				TrainingDocument trainingDocument = new TrainingDocument();
				trainingDocument.set_id(dbObject.getString("_id"));
				trainingDocument.setCategory(dbObject.getString("CATEGORY"));
				trainingDocument.setDescription(dbObject.getString(
						"DESCRIPTION").replaceAll("\\<.*?>", ""));
				trainingDocument.setTitle(dbObject.getString("TITLE")
						.replaceAll("\\<.*?>", ""));
				indexTrainingSet(trainingDocument);
			}
		} finally {
			cursor.close();
		}

	}

	@SuppressWarnings("deprecation")
	public void indexTrainingSet(TrainingDocument trainingDocument)
			throws IOException {

		IndexWriter writer = getIndexWriter(false, "C:/TrainingIndex");

		Document doc = new Document();
		doc.add(new Field("_id", trainingDocument.get_id(), Field.Store.YES,
				Field.Index.NOT_ANALYZED));
		doc.add(new Field("category", trainingDocument.getCategory(),
				Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
		doc.add(new Field("title", trainingDocument.getTitle(),
				Field.Store.YES, Field.Index.ANALYZED,
				Field.TermVector.WITH_POSITIONS_OFFSETS));
		doc.add(new Field("description", trainingDocument.getDescription(),
				Field.Store.YES, Field.Index.ANALYZED,
				Field.TermVector.WITH_POSITIONS_OFFSETS));
		writer.addDocument(doc);

		writer.close();
	}

	public void buildIndex() throws IOException {

		String connectionString = "mongodb://***REMOVED***.***REMOVED***:27017";
		MongoClientURI uri = new MongoClientURI(connectionString);
		MongoClient mongoClient = new MongoClient(uri);
		DB db = mongoClient.getDB("iadata120125m1015");
		DBCollection collection = db.getCollection("InformationAsset");
		DBCursor cursor = collection.find();
		try {
			while (cursor.hasNext()) {
				BasicDBObject dbObject = (BasicDBObject) cursor.next();
				String _id = dbObject.getString("_id");
				String catdocref = dbObject.getString("IAID");
				String title = dbObject.getString("Title").replaceAll(
						"\\<.*?>", "");
				DBObject scopecontent = (BasicDBObject) dbObject
						.get("ScopeContent");
				String description = (String) scopecontent.get("Description")
						.toString().replaceAll("\\<.*?>", "");
				System.out.println(description);
				String urlparams = dbObject.getString("IAID");
				InformationAssetViewFull informationAssetView = new InformationAssetViewFull();
				informationAssetView.set_id(_id);
				informationAssetView.setCATDOCREF(catdocref);
				informationAssetView.setTITLE(title);
				informationAssetView.setDESCRIPTION(description);
				informationAssetView.setURLPARAMS(urlparams);
				indexAsset(informationAssetView);
				System.out.println("IA=" + catdocref + " added to index");
			}
		} finally {
			cursor.close();
		}

	}

	public void indexAsset(InformationAssetViewFull asset) throws IOException {
		IndexWriter writer = getIndexWriter(false, "C:/IAIndex");
		Document doc = new Document();
		doc.add(new TextField("_id", asset.get_id(), Field.Store.YES));
		doc.add(new TextField("catdocref", asset.getCATDOCREF(),
				Field.Store.YES));
		doc.add(new TextField("title", asset.getTITLE(), Field.Store.YES));
		doc.add(new TextField("description", asset.getDESCRIPTION(),
				Field.Store.YES));
		doc.add(new TextField("urlparams", asset.getURLPARAMS(),
				Field.Store.YES));
		writer.addDocument(doc);
		writer.close();
	}
}
