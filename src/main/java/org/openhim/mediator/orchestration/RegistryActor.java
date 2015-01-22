/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.openhim.mediator.orchestration;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.apache.http.HttpStatus;
import org.openhim.mediator.datatypes.AssigningAuthority;
import org.openhim.mediator.datatypes.Identifier;
import org.openhim.mediator.denormalization.PIXRequestActor;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.FinishRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPResponse;
import org.openhim.mediator.engine.messages.SimpleMediatorRequest;
import org.openhim.mediator.messages.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RegistryActor extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private MediatorConfig config;

    protected ActorRef resolvePatientIDActor;

    private ActorRef requestHandler;
    private String xForwardedFor;
    private String messageBuffer;
    private Identifier patientIdBuffer;


    public RegistryActor(MediatorConfig config) {
        this.config = config;

        resolvePatientIDActor = getContext().actorOf(Props.create(PIXRequestActor.class, config), "pix-denormalization");
    }


    private void parseMessage(MediatorHTTPRequest request) {
        requestHandler = request.getRequestHandler();
        xForwardedFor = request.getHeaders().get("X-Forwarded-For");

        //get request body
        messageBuffer = request.getBody();

        //parse message...
        ActorSelection parseActor = getContext().actorSelection(config.userPathFor("parse-registry-stored-query"));
        parseActor.tell(new SimpleMediatorRequest<>(request.getRequestHandler(), getSelf(), messageBuffer), getSelf());
    }

    private void lookupEnterpriseIdentifier() {
        String enterpriseIdentifierAuthority = config.getProperty("pix.requestedAssigningAuthority");
        String enterpriseIdentifierAuthorityId = config.getProperty("pix.requestedAssigningAuthorityId");
        AssigningAuthority authority = new AssigningAuthority(enterpriseIdentifierAuthority, enterpriseIdentifierAuthorityId);
        ResolvePatientIdentifier msg = new ResolvePatientIdentifier(requestHandler, getSelf(), patientIdBuffer, authority);
        resolvePatientIDActor.tell(msg, getSelf());
    }

    private void enrichEnterpriseIdentifier(ResolvePatientIdentifierResponse msg) {
        patientIdBuffer = msg.getIdentifier();

        if (patientIdBuffer !=null) {
            log.info("Resolved patient enterprise identifier. Enriching message...");
            ActorSelection enrichActor = getContext().actorSelection(config.userPathFor("enrich-registry-stored-query"));
            EnrichRegistryStoredQuery enrichMsg = new EnrichRegistryStoredQuery(requestHandler, getSelf(), messageBuffer, patientIdBuffer);
            enrichActor.tell(enrichMsg, getSelf());
        } else {
            log.info("Could not resolve patient identifier");
            FinishRequest response = new FinishRequest("Unknown patient identifier", "text/plain", HttpStatus.SC_NOT_FOUND);
            requestHandler.tell(response, getSelf());
        }
    }

    private void forwardEnrichedMessage(EnrichRegistryStoredQueryResponse msg) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/soap+xml");

        messageBuffer = msg.getEnrichedMessage();

        String scheme;
        Integer port;
        if (config.getProperty("xds.registry.secure").equals("true")) {
            scheme = "https";
            port = Integer.parseInt(config.getProperty("xds.registry.securePort"));
        } else {
            scheme = "http";
            port = Integer.parseInt(config.getProperty("xds.registry.port"));
        }

        MediatorHTTPRequest request = new MediatorHTTPRequest(
                requestHandler, getSelf(), "XDS.b Registry", "POST", scheme,
                config.getProperty("xds.registry.host"), port, config.getProperty("xds.registry.path"),
                messageBuffer, headers, Collections.<String, String>emptyMap()
        );

        ActorSelection httpConnector = getContext().actorSelection(config.userPathFor("http-connector"));
        httpConnector.tell(request, getSelf());
    }

    private void finalizeResponse(MediatorHTTPResponse response) {
        requestHandler.tell(response.toFinishRequest(), getSelf());
    }

    private void sendAuditMessage(ATNAAudit.TYPE type) {
        try {
            ATNAAudit audit = new ATNAAudit(type);
            audit.setMessage(messageBuffer);

            audit.setParticipantIdentifiers(Collections.singletonList(patientIdBuffer));
            audit.setUniqueId("NotParsed");
            //TODO failed outcome
            audit.setOutcome(true);
            audit.setSourceIP(xForwardedFor);

            getContext().actorSelection(config.userPathFor("atna-auditing")).tell(audit, getSelf());
        } catch (Exception ex) {
            //quiet you!
        }
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof MediatorHTTPRequest) { //parse request
            log.info("Parsing registry stored query request...");
            parseMessage((MediatorHTTPRequest) msg);

        } else if (msg instanceof ParsedRegistryStoredQuery) { //resolve patient id
            log.info("Parsed contents. Resolving patient enterprise identifier...");
            patientIdBuffer = ((ParsedRegistryStoredQuery) msg).getPatientId();
            lookupEnterpriseIdentifier();

            sendAuditMessage(ATNAAudit.TYPE.REGISTRY_QUERY_RECEIVED); //audit

        } else if (msg instanceof ResolvePatientIdentifierResponse) { //enrich message
            enrichEnterpriseIdentifier((ResolvePatientIdentifierResponse) msg);

        } else if (msg instanceof EnrichRegistryStoredQueryResponse) { //forward to registry
            log.info("Sending enriched request to XDS.b Registry");
            forwardEnrichedMessage((EnrichRegistryStoredQueryResponse) msg);

        } else if (msg instanceof MediatorHTTPResponse) { //respond
            log.info("Received response from XDS.b Registry");
            finalizeResponse((MediatorHTTPResponse) msg);
            sendAuditMessage(ATNAAudit.TYPE.REGISTRY_QUERY_ENRICHED); //audit

        } else {
            unhandled(msg);
        }
    }
}
