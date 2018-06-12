package org.openhim.mediator.datatypes;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 11.06.18.
 */
public class DocumentsHolder {
    private Map<String, String> documents = new HashMap<>();

    public Map<String, String> getDocuments() {
        return documents;
    }

    public void setDocuments(Map<String, String> documents) {
        this.documents = documents;
    }
}
