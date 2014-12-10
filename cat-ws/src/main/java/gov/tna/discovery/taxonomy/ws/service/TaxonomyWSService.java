package gov.tna.discovery.taxonomy.ws.service;

import gov.tna.discovery.taxonomy.common.repository.domain.lucene.InformationAssetView;
import gov.tna.discovery.taxonomy.common.service.domain.CategorisationResult;
import gov.tna.discovery.taxonomy.ws.domain.TestCategoriseSingleRequest;

import java.util.List;

/**
 * Service that handles all requests from Taxonomy WS Controller
 * 
 * @author jcharlet
 *
 */
public interface TaxonomyWSService {

    public void publishUpdateOnCategory(String ciaid);

    public List<InformationAssetView> performSearch(String categoryQuery, Double score, Integer limit, Integer offset);

    public List<CategorisationResult> testCategoriseSingle(TestCategoriseSingleRequest testCategoriseSingleRequest);

}