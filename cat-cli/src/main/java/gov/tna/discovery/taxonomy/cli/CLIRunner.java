package gov.tna.discovery.taxonomy.cli;

import java.io.IOException;

import gov.tna.discovery.taxonomy.CLIApplication;
import gov.tna.discovery.taxonomy.service.impl.Categoriser;
import gov.tna.discovery.taxonomy.service.impl.TrainingSetService;

import org.apache.lucene.queryparser.classic.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// TODO 1 do not use local index and mongo db, but use dev platform
// TODO 3 handle lack of results: many NPE
// TODO 3 empty the collections and index before repopulating them
// TODO 3 handle concurrency issues: while creating the training set, if
// collection is browsed from the solr admin GUI, it crashes
@Component
public class CLIRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CLIApplication.class);

    @Autowired
    Categoriser categoriser;

    @Autowired
    TrainingSetService trainingSetService;

    public void run(String... args) throws IOException, ParseException {

	logger.info("Start cat CLI Runner.");

	trainingSetService.createTrainingSet(0.1f);

	// categoriser.indexTrainingSet();

	// categoriser.categoriseIAViewSolrDocument("CO 273/632/2");

	// categoriser.categoriseIAViewsFromSolr();

	logger.info("Stop cat CLI Runner.");
    }
}
