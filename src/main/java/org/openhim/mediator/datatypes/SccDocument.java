package org.openhim.mediator.datatypes;

import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;

import java.util.Objects;

public class SccDocument {

    boolean successful = false;
    private ProvideAndRegisterDocumentSetRequestType.Document document;

    public SccDocument(ProvideAndRegisterDocumentSetRequestType.Document document) {
        this.successful = successful;
        this.document = document;
    }

    public ProvideAndRegisterDocumentSetRequestType.Document getDocument() {
        return document;
    }

    public void setDocument(ProvideAndRegisterDocumentSetRequestType.Document document) {
        this.document = document;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SccDocument that = (SccDocument) o;
        return successful == that.successful &&
                Objects.equals(document, that.document);
    }

    @Override
    public int hashCode() {
        return Objects.hash(successful, document);
    }
}
