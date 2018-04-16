package org.openhim.mediator.messages;

import org.openhim.mediator.engine.messages.MediatorRequestMessage;
import org.openhim.mediator.engine.messages.MediatorResponseMessage;

public class ResolveSccResponse extends MediatorResponseMessage {

    private final Boolean success;
    private final String response;

    public ResolveSccResponse(MediatorRequestMessage originalRequest, Boolean success, String response) {
        super(originalRequest);
        this.success = success;
        this.response = response;
    }

    public Boolean getSuccess() {
        return success;
    }

    public String getResponse() {
        return response;
    }
}
