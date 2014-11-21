package gov.tna.discovery.taxonomy.service.impl;

import gov.tna.discovery.taxonomy.repository.domain.lucene.InformationAssetView;
import gov.tna.discovery.taxonomy.repository.domain.lucene.InformationAssetViewFields;
import gov.tna.discovery.taxonomy.repository.lucene.LuceneHelperTools;
import gov.tna.discovery.taxonomy.repository.mongo.CategoryRepository;
import gov.tna.discovery.taxonomy.repository.mongo.TrainingDocumentRepository;
import gov.tna.discovery.taxonomy.service.exception.TaxonomyErrorType;
import gov.tna.discovery.taxonomy.service.exception.TaxonomyException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * class dedicated to the categorisation of documents<br/>
 * use the More Like This feature of Lucene
 *
 */
@Service
public class Categoriser {

    private static final Logger logger = LoggerFactory.getLogger(Categoriser.class);

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    TrainingDocumentRepository trainingDocumentRepository;

    @Autowired
    TrainingSetService trainingSetService;

    @Autowired
    IndexReader iaViewIndexReader;

    @Autowired
    IndexReader trainingSetIndexReader;

    @Autowired
    private SearcherManager iaviewSearcherManager;

    @Autowired
    private SearcherManager trainingSetSearcherManager;

    @Value("${lucene.mlt.mimimumScore}")
    private float mimimumScore;
    @Value("${lucene.mlt.maximumSimilarElements}")
    private int maximumSimilarElements;

    @Value("${lucene.index.version}")
    private String luceneVersion;

    /**
     * categorise a document by running the MLT process against the training set
     * 
     * @param catdocref
     *            IAID
     * @throws IOException
     * @throws ParseException
     */
    public Map<String, Float> categoriseIAViewSolrDocument(String catdocref) {
	// TODO 4 CATDOCREF in schema.xml should be stored as string?
	// and not text_gen: do not need to be tokenized. makes search by
	// catdocref more complicated that it needs (look for a bunch of terms,
	// what if they are provided in the wrong order? have to check it also
	TopDocs results = searchIAViewIndexByFieldAndPhrase("CATDOCREF", catdocref, 1);

	Document doc;
	try {
	    doc = this.iaViewIndexReader.document(results.scoreDocs[0].doc);
	} catch (IOException e) {
	    throw new TaxonomyException(TaxonomyErrorType.LUCENE_IO_EXCEPTION, e);
	}

	Reader reader = new StringReader(doc.get(InformationAssetViewFields.DESCRIPTION.toString()));
	Map<String, Float> result = runMlt(reader);

	logger.debug("DOCUMENT");
	logger.debug("------------------------");
	logger.debug("TITLE: {}", doc.get("TITLE"));
	logger.debug("IAID: {}", doc.get("CATDOCREF"));
	logger.debug("DESCRIPTION: {}", doc.get("DESCRIPTION"));
	logger.debug("");
	for (Entry<String, Float> category : result.entrySet()) {
	    logger.debug("CATEGORY: {}, score: {}", category.getKey(), category.getValue());
	}
	logger.debug("------------------------");

	logger.debug("");

	return result;

    }

    private TopDocs searchIAViewIndexByFieldAndPhrase(String field, String value, int numHits) {
	IndexSearcher searcher = null;
	try {
	    searcher = iaviewSearcherManager.acquire();
	    QueryParser qp = new QueryParser(Version.valueOf(luceneVersion), field, new WhitespaceAnalyzer(
		    Version.valueOf(luceneVersion)));
	    return searcher.search(qp.parse(QueryParser.escape(value)), numHits);
	} catch (IOException ioException) {
	    throw new TaxonomyException(TaxonomyErrorType.LUCENE_IO_EXCEPTION, ioException);
	} catch (ParseException parseException) {
	    throw new TaxonomyException(TaxonomyErrorType.LUCENE_PARSE_EXCEPTION, parseException);

	} finally {
	    LuceneHelperTools.releaseSearcherManagerQuietly(iaviewSearcherManager, searcher);
	}
    }

    /**
     * Categorise the whole IA collection
     * 
     * @throws IOException
     */
    public void categoriseIAViewsFromSolr() throws IOException {

	// TODO 1 insert results in a new database/index

	for (int i = 0; i < this.iaViewIndexReader.maxDoc(); i++) {
	    // TODO 2 Add concurrency: categorize several documents at the same
	    // time
	    if (this.iaViewIndexReader.hasDeletions()) {
		System.out
			.println("[ERROR].categoriseDocument: the reader provides deleted elements though it should not");
	    }

	    Document doc = this.iaViewIndexReader.document(i);

	    Reader reader = new StringReader(doc.get(InformationAssetViewFields.DESCRIPTION.toString()));
	    Map<String, Float> result = runMlt(reader);

	    logger.debug("DOCUMENT");
	    logger.debug("------------------------");
	    logger.debug("TITLE: {}", doc.get("TITLE"));
	    logger.debug("IAID: {}", doc.get("CATDOCREF"));
	    logger.info("DESCRIPTION: {}", doc.get("DESCRIPTION"));
	    logger.debug("");
	    for (Entry<String, Float> category : result.entrySet()) {
		logger.info("Document {} has CATEGORY: {}, score: {}", doc.get("CATDOCREF"), category.getKey(),
			category.getValue());
	    }
	    logger.debug("------------------------");

	    logger.debug("");

	}

	logger.debug("Categorisation finished");

    }

    /**
     * run More Like This process on a document by comparing its description to
     * the description of all items of the training set<br/>
     * currently we get a fixed number of the top results
     * 
     * @param modelPath
     *            path of the training set index
     * @param reader
     *            reader of the document being tested
     * @param maxResults
     *            max number of results to return
     * @return
     * @throws IOException
     */
    // TODO 1 check and update fields that are being retrieved to create
    // training set, used for MLT (run MLT on title, context desc and desc at
    // least. returns results by score not from a fixed number)
    public Map<String, Float> runMlt(Reader reader) {

	Map<String, Float> result = null;
	IndexSearcher searcher = null;
	try {
	    searcher = trainingSetSearcherManager.acquire();

	    Analyzer analyzer = new EnglishAnalyzer(Version.valueOf(luceneVersion));

	    MoreLikeThis moreLikeThis = new MoreLikeThis(this.trainingSetIndexReader);
	    moreLikeThis.setAnalyzer(analyzer);
	    moreLikeThis.setFieldNames(new String[] { InformationAssetViewFields.DESCRIPTION.toString() });

	    Query query = moreLikeThis.like(reader, InformationAssetViewFields.DESCRIPTION.toString());

	    TopDocs topDocs = searcher.search(query, this.maximumSimilarElements);

	    result = new LinkedHashMap<String, Float>();

	    int size = 0;
	    if (topDocs.totalHits <= this.maximumSimilarElements) {
		size = topDocs.totalHits - 1;
	    } else {
		size = this.maximumSimilarElements - 1;
	    }

	    for (int i = 0; i < size; i++) {
		ScoreDoc scoreDoc = topDocs.scoreDocs[i];
		Float score = scoreDoc.score;
		logger.debug("score for document found: {}", score);

		if (score < this.mimimumScore) {
		    break;
		}

		Document hitDoc = searcher.doc(scoreDoc.doc);
		String category = hitDoc.get(InformationAssetViewFields.CATEGORY.toString());

		Float existingCategoryScore = result.get(category);
		if (existingCategoryScore == null || existingCategoryScore < score) {
		    result.put(category, scoreDoc.score);
		}

	    }

	} catch (IOException e) {
	    throw new TaxonomyException(TaxonomyErrorType.LUCENE_IO_EXCEPTION, e);
	} finally {
	    LuceneHelperTools.releaseSearcherManagerQuietly(trainingSetSearcherManager, searcher);
	}

	return result;
    }

    public Map<String, Float> testCategoriseSingle(InformationAssetView iaView) {
	Reader reader = new StringReader(iaView.getDESCRIPTION());
	return runMlt(reader);
    }

}
