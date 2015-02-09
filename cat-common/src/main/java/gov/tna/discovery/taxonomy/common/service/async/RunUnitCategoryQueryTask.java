package gov.tna.discovery.taxonomy.common.service.async;

import gov.tna.discovery.taxonomy.common.aop.annotation.Loggable;
import gov.tna.discovery.taxonomy.common.repository.domain.mongo.Category;
import gov.tna.discovery.taxonomy.common.repository.lucene.IAViewRepository;
import gov.tna.discovery.taxonomy.common.service.TaxonomyHelperTools;
import gov.tna.discovery.taxonomy.common.service.domain.CategorisationResult;
import gov.tna.discovery.taxonomy.common.service.exception.TaxonomyException;

import java.util.concurrent.Callable;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.TopDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task to run a category query against the index, using a filter
 * 
 * @author jcharlet
 *
 */
public class RunUnitCategoryQueryTask implements Callable<CategorisationResult> {

    private Filter filter;
    private Category category;
    private IAViewRepository iaViewRepository;
    private Logger logger = LoggerFactory.getLogger(RunUnitCategoryQueryTask.class);

    public RunUnitCategoryQueryTask(Filter filter, Category category, IAViewRepository iaViewRepository) {
	super();
	this.filter = filter;
	this.category = category;
	this.iaViewRepository = iaViewRepository;
    }

    @Loggable
    @Override
    public CategorisationResult call() throws Exception {
	long start_time = TaxonomyHelperTools.startTimer();
	logger.debug(".call: start for category: {}", category.getTtl());
	try {

	    TopDocs topDocs = iaViewRepository.performSearchWithoutAnyPostProcessing(category.getQry(), filter,
		    category.getSc(), 1, 0);
	    if (topDocs.totalHits != 0 && topDocs.scoreDocs[0].score > category.getSc()) {
		return new CategorisationResult(category.getTtl(), topDocs.scoreDocs[0].score);
	    }
	} catch (TaxonomyException e) {
	    logger.debug(".call: an exception occured while parsing category query for category: {}, exception: {}",
		    category.getTtl(), e.getMessage());
	}
	long timerDifference = TaxonomyHelperTools.getTimerDifference(start_time);
	return null;
    }

}
