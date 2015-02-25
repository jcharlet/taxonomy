package uk.gov.nationalarchives.discovery.taxonomy.common.mapper;

import java.util.Date;

import uk.gov.nationalarchives.discovery.taxonomy.common.domain.repository.lucene.InformationAssetView;
import uk.gov.nationalarchives.discovery.taxonomy.common.domain.repository.mongo.IAViewUpdate;
import uk.gov.nationalarchives.discovery.taxonomy.common.domain.repository.mongo.MongoInformationAssetView;
import uk.gov.nationalarchives.discovery.taxonomy.common.domain.repository.mongo.TestDocument;

public class TaxonomyMapper {
    public static TestDocument getTestDocumentFromIAView(InformationAssetView iaView) {
	TestDocument testDocument = new TestDocument();
	testDocument.setDescription(iaView.getDESCRIPTION());
	testDocument.setContextDescription(iaView.getCONTEXTDESCRIPTION());
	testDocument.setTitle(iaView.getTITLE());
	testDocument.setDocReference(iaView.getDOCREFERENCE());
	testDocument.setCatDocRef(iaView.getCATDOCREF());
	testDocument.setCorpBodys(iaView.getCORPBODYS());
	testDocument.setPersonFullName(iaView.getPERSON_FULLNAME());
	testDocument.setPlaceName(iaView.getPLACE_NAME());
	testDocument.setSubjects(iaView.getSUBJECTS());
	return testDocument;
    }

    public static InformationAssetView getIAViewFromTestDocument(TestDocument testDocument) {
	InformationAssetView iaView = new InformationAssetView();
	iaView.setDESCRIPTION(testDocument.getDescription());
	iaView.setCONTEXTDESCRIPTION(testDocument.getContextDescription());
	iaView.setTITLE(testDocument.getTitle());
	iaView.setDOCREFERENCE(testDocument.getDocReference());
	iaView.setCATDOCREF(testDocument.getCatDocRef());
	iaView.setCORPBODYS(testDocument.getCorpBodys());
	iaView.setPERSON_FULLNAME(testDocument.getPersonFullName());
	iaView.setPLACE_NAME(testDocument.getPlaceName());
	iaView.setSUBJECTS(testDocument.getSubjects());
	return iaView;
    }

    public static MongoInformationAssetView getMongoIAViewFromLuceneIAView(
	    InformationAssetView iaViewFromLuceneDocument, Date creationDate) {
	MongoInformationAssetView mongoIaView = new MongoInformationAssetView(creationDate);
	mongoIaView.setDocReference(iaViewFromLuceneDocument.getDOCREFERENCE());
	mongoIaView.setCatDocRef(iaViewFromLuceneDocument.getCATDOCREF());
	mongoIaView.setSeries(iaViewFromLuceneDocument.getSERIES());
	return mongoIaView;
    }

    public static IAViewUpdate getIAViewUpdateFromLuceneIAView(InformationAssetView iaViewFromLuceneDocument,
	    Date creationDate) {
	IAViewUpdate iaViewUpdate = new IAViewUpdate();
	iaViewUpdate.setDocReference(iaViewFromLuceneDocument.getDOCREFERENCE());
	iaViewUpdate.setCatDocRef(iaViewFromLuceneDocument.getCATDOCREF());
	iaViewUpdate.setCreationDate(creationDate);
	return iaViewUpdate;
    }

}
