package org.openhim.mediator.dsub.pull;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.ObjectFactory;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.bw_2.UnableToGetMessagesFault;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;
import org.openhim.mediator.dsub.MongoSupport;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class MongoPullPoint extends MongoSupport implements PullPoint {

    private final static String DOC_ID = "documentId";
    private final static String LOCATION_ID = "locationId";
    private final static String METADATA = "metadata";

    private final String locationId;

    MongoPullPoint(MongoDatabase mongoDb, String locationId) {
        super(mongoDb, "pull_point_documents");
        this.locationId = locationId;
    }

    @Override
    public List<String> getDocumentIds() {
        List<String> ids = new ArrayList<>();
        for (org.bson.Document doc : getCollection().find(Filters.eq(LOCATION_ID, locationId))) {
            ids.add(doc.getString(DOC_ID));
        }
        return ids;
    }

    @Override
    public void registerDocument(String documentId) {
        getCollection().insertOne(
                new org.bson.Document("_id", UUID.randomUUID()).append(DOC_ID, documentId).append(LOCATION_ID, locationId));
    }

    @Override
    public void registerDocument(String documentId, String metadata) {
        getCollection().insertOne(
                new org.bson.Document("_id", UUID.randomUUID()).append(DOC_ID, documentId).append(LOCATION_ID, locationId).append(METADATA, metadata));
    }
    
    @Override
    public List<String> getDocumentIds(Integer maxDocumentIds) {
        List<String> ids = new ArrayList<>();
        for (org.bson.Document doc : getCollection().find(Filters.eq(LOCATION_ID, locationId)).sort(new BasicDBObject("$natural", -1)).limit(maxDocumentIds)) {
            ids.add(doc.getString(DOC_ID));
        }
        return ids;
    }

	@Override
	public List<NotificationMessageHolderType> getMessages(Integer max)
	        throws UnableToGetMessagesFault, ResourceUnknownFault, ParserConfigurationException, DOMException, UnsupportedEncodingException {
		
		if (max <= 0) {
			max = 10;
		}
		
		if (max > 1000) {
			max = 1000;
		}
		
		List<NotificationMessageHolderType> holders = new  ArrayList<NotificationMessageHolderType>();
		for (org.bson.Document doc : getCollection().find(Filters.eq(LOCATION_ID, locationId)).sort(new BasicDBObject("$natural", -1))
		        .limit(max)) {
			NotificationMessageHolderType holder = new NotificationMessageHolderType();
			holder.setMessage(new ObjectFactory().createNotificationMessageHolderTypeMessage());
			if (locationId != null) {
				TopicExpressionType topicExp = new TopicExpressionType();
				topicExp.getContent().add(locationId);
				holder.setTopic(topicExp);
			}
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder = dbf.newDocumentBuilder();
	        Document document = builder.newDocument();
	        Element element = document.createElement("root");
	        element.setTextContent(Base64.encodeBase64String(doc.getString(METADATA).getBytes("UTF-8")));
	        element.setAttribute("documentId", doc.getString(DOC_ID));
	        document.appendChild(element);
	        
			holder.getMessage().setAny(element);
			holders.add(holder);
		}
		return holders;
	}

}
