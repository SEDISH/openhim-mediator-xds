package org.openhim.mediator.messages;

import org.openhim.mediator.datatypes.Acknowledgement;
import org.openhim.mediator.engine.messages.MediatorRequestMessage;
import org.openhim.mediator.engine.messages.MediatorResponseMessage;

public class ResolveSccResponse extends MediatorResponseMessage {

    private final Acknowledgement acknowledgement;

    public ResolveSccResponse(MediatorRequestMessage originalRequest, Acknowledgement acknowledgement) {
        super(originalRequest);
        this.acknowledgement = acknowledgement;
    }

    public Boolean getSuccess() {
        return acknowledgement.isSuccess();
    }

    public String getErroMessge() {
        String error = acknowledgement.getErrorCode();
        if (acknowledgement.getErrorDiagnosticsInformation() != null) {
            error += ":" + acknowledgement.getErrorDiagnosticsInformation();
        }
        return error;
    }
}
