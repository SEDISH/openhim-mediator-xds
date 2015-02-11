/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.openhim.mediator.e2e;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.openhim.mediator.engine.RoutingTable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * ITI-41: Provide and Register Document Set.b (MTOM edition)
 *
 * path: /xdsrepository
 *
 * Should enrich the request with a ECIDs, ELIDs and EPIDs and forward the enriched result to the repository
 */
public class ProvideAndRegisterDocumentSetE2EMTOMTest extends ProvideAndRegisterDocumentSetE2EBase {
    @Test
    public void runPnRTest_MTOM() throws IOException, URISyntaxException {
        InputStream in = getClass().getClassLoader().getResourceAsStream("pnr_e2e_mtom.xml");
        String pnr = IOUtils.toString(in);

        E2EHTTPResponse response = executeHTTPRequest("POST", "/xdsrepository", pnr, Collections.singletonMap("Content-Type", "multipart/related"), null);
        assertEquals(new Integer(201), response.status);

        pixServer.verifyCalled(2);
        verify(
                postRequestedFor(urlEqualTo("/openmrs-standalone/ms/xdsrepository"))
                        .withHeader("Content-Type", equalTo("multipart/related"))
        );
        verify(2,
                postRequestedFor(urlEqualTo("/CSD/csr/jembi-ecgroup-testing/careServicesRequest"))
                        .withHeader("Content-Type", equalTo("application/xml"))
        );
    }
}
