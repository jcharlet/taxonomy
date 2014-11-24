package gov.tna.discovery.taxonomy.service.impl;

import gov.tna.discovery.taxonomy.repository.domain.TrainingDocument;
import gov.tna.discovery.taxonomy.repository.domain.lucene.InformationAssetView;
import gov.tna.discovery.taxonomy.repository.domain.lucene.InformationAssetViewFields;
import gov.tna.discovery.taxonomy.repository.domain.mongo.Category;
import gov.tna.discovery.taxonomy.repository.lucene.IAViewRepository;
import gov.tna.discovery.taxonomy.repository.lucene.LuceneHelperTools;
import gov.tna.discovery.taxonomy.repository.mongo.CategoryRepository;
import gov.tna.discovery.taxonomy.repository.mongo.TrainingDocumentRepository;
import gov.tna.discovery.taxonomy.service.TrainingSetService;
import gov.tna.discovery.taxonomy.service.exception.TaxonomyErrorType;
import gov.tna.discovery.taxonomy.service.exception.TaxonomyException;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

//TODO create Interface for service layer
@Service
public class TrainingSetServiceImpl implements TrainingSetService {

    private static final Logger logger = LoggerFactory.getLogger(TrainingSetServiceImpl.class);

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    TrainingDocumentRepository trainingDocumentRepository;

    @Autowired
    IAViewRepository iaViewRepository;

    @Autowired
    private Directory trainingSetDirectory;

    @Value("${lucene.index.trainingSetCollectionPath}")
    private String trainingSetCollectionPath;

    @Value("${lucene.index.version}")
    private String luceneVersion;

    /*
     * (non-Javadoc)
     * 
     * @see gov.tna.discovery.taxonomy.service.impl.TrainingSetService#
     * updateTrainingSetForCategory
     * (gov.tna.discovery.taxonomy.repository.domain.mongo.Category,
     * java.lang.Float)
     */
    @Override
    public void updateTrainingSetForCategory(Category category, Float fixedLimitScore) {
	List<InformationAssetView> IAViewResults;
	try {
	    // FIXME JCT Iterate instead of taking only 100 elements
	    IAViewResults = iaViewRepository.performSearch(category.getQry(),
		    (fixedLimitScore != null ? fixedLimitScore : category.getSc()), 1000, 0);
	    logger.debug(".updateTrainingSetForCategory: Category=" + category.getTtl() + ", found "
		    + IAViewResults.size() + " result(s)");
	    if (IAViewResults.size() > 0) {

		for (InformationAssetView iaView : IAViewResults) {
		    TrainingDocument trainingDocument = new TrainingDocument();
		    trainingDocument.setCategory(category.getTtl());
		    trainingDocument.setDescription(iaView.getDESCRIPTION());
		    trainingDocument.setTitle(iaView.getTITLE());
		    trainingDocumentRepository.save(trainingDocument);
		    logger.debug(trainingDocument.getCategory() + ":" + iaView.getCATDOCREF() + " - "
			    + trainingDocument.getTitle().replaceAll("\\<.*?>", ""));
		}
	    }
	} catch (TaxonomyException e) {
	    // TODO 1 several errors occur while creating the training set,
	    // to investigate
	    // some queries are not valid: paul takes care of them.
	    // Some queries have wildcards and lucene doesnt accept them: to
	    // enable.
	    logger.error(".updateTrainingSetForCategory< An error occured for category: " + category.toString());
	    logger.error(".updateTrainingSetForCategory< Error message: " + e.getMessage());
	    throw e;
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.tna.discovery.taxonomy.service.impl.TrainingSetService#
     * indexTrainingSetDocument
     * (gov.tna.discovery.taxonomy.repository.domain.TrainingDocument,
     * org.apache.lucene.index.IndexWriter)
     */
    @Override
    @SuppressWarnings("deprecation")
    public void indexTrainingSetDocument(TrainingDocument trainingDocument, IndexWriter writer) throws IOException {
	// TODO 4 handle exceptions, do not stop the process unless several
	// errors occur
	// TODO 1 bulk insert, this is far too slow to do it unitary!
	// TODO 4 Field is deprecated, use appropriate fields.
	// FIXME why to remove punctuation before indexing? analyser duty
	trainingDocument.setDescription(trainingDocument.getDescription().replaceAll("\\<.*?>", ""));
	trainingDocument.setTitle(trainingDocument.getTitle().replaceAll("\\<.*?>", ""));

	Document doc = new Document();
	doc.add(new Field(InformationAssetViewFields._id.toString(), trainingDocument.get_id(), Field.Store.YES,
		Field.Index.NOT_ANALYZED));
	doc.add(new Field(InformationAssetViewFields.CATEGORY.toString(), trainingDocument.getCategory(),
		Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
	doc.add(new Field(InformationAssetViewFields.TITLE.toString(), trainingDocument.getTitle(), Field.Store.YES,
		Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
	doc.add(new Field(InformationAssetViewFields.DESCRIPTION.toString(), trainingDocument.getDescription(),
		Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
	writer.addDocument(doc);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * gov.tna.discovery.taxonomy.service.impl.TrainingSetService#createTrainingSet
     * (java.lang.Float)
     */
    @Override
    public void createTrainingSet(Float fixedLimitScore) throws IOException, ParseException {
	logger.debug(".createTrainingSet : START");

	Iterator<Category> categoryIterator = categoryRepository.findAll().iterator();

	// empty collection
	trainingDocumentRepository.deleteAll();

	while (categoryIterator.hasNext()) {
	    Category category = categoryIterator.next();
	    try {
		updateTrainingSetForCategory(category, fixedLimitScore);
	    } catch (TaxonomyException e) {
		continue;
	    }

	}
	logger.debug(".createTrainingSet : END");
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.tna.discovery.taxonomy.service.impl.TrainingSetService#
     * deleteAndUpdateTraingSetIndexForCategory
     * (gov.tna.discovery.taxonomy.repository.domain.mongo.Category)
     */
    @Override
    public void deleteAndUpdateTraingSetIndexForCategory(Category category) {
	IndexWriter writer = null;
	try {
	    writer = new IndexWriter(trainingSetDirectory, new IndexWriterConfig(getLuceneVersion(),
		    new EnglishAnalyzer(getLuceneVersion())));
	    writer.deleteDocuments(new Term(InformationAssetViewFields.CATEGORY.toString(), category.getTtl()));

	    for (TrainingDocument trainingDocument : trainingDocumentRepository.findByCategory(category.getTtl())) {
		indexTrainingSetDocument(trainingDocument, writer);
	    }
	} catch (IOException e) {
	    throw new TaxonomyException(TaxonomyErrorType.LUCENE_IO_EXCEPTION, e);
	} finally {
	    LuceneHelperTools.closeIndexWriterQuietly(writer);
	}
    }

    private Version getLuceneVersion() {
	return Version.valueOf(luceneVersion);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * gov.tna.discovery.taxonomy.service.impl.TrainingSetService#indexTrainingSet
     * ()
     */
    @Override
    public void indexTrainingSet() {
	IndexWriter writer = null;
	try {
	    writer = new IndexWriter(trainingSetDirectory, new IndexWriterConfig(getLuceneVersion(),
		    new EnglishAnalyzer(getLuceneVersion())));

	    writer.deleteAll();

	    Iterator<TrainingDocument> trainingDocumentIterator = trainingDocumentRepository.findAll().iterator();

	    while (trainingDocumentIterator.hasNext()) {
		TrainingDocument trainingDocument = trainingDocumentIterator.next();
		indexTrainingSetDocument(trainingDocument, writer);

	    }
	} catch (IOException e) {
	    throw new TaxonomyException(TaxonomyErrorType.LUCENE_IO_EXCEPTION, e);
	} finally {
	    LuceneHelperTools.closeIndexWriterQuietly(writer);
	}

    }

}
