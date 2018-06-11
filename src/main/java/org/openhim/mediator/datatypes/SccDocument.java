package org.openhim.mediator.datatypes;

import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;

import java.util.Objects;

public class SccDocument {

    private boolean processed;
    private ProvideAndRegisterDocumentSetRequestType.Document document;

    public SccDocument(ProvideAndRegisterDocumentSetRequestType.Document document) {
        this.processed = false;
        this.document = document;
    }

    public ProvideAndRegisterDocumentSetRequestType.Document getDocument() {
        return document;
    }

    public void setDocument(ProvideAndRegisterDocumentSetRequestType.Document document) {
        this.document = document;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SccDocument that = (SccDocument) o;
        return processed == that.processed &&
                Objects.equals(document, that.document);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processed, document);
    }
}
