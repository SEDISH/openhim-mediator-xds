package org.openhim.mediator.dsub.subscription;

import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.messages.ITI53NotifyMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SoapSubscriptionNotifier implements SubscriptionNotifier {

    private MediatorConfig config;

    private static final Logger LOGGER = LoggerFactory.getLogger(SoapSubscriptionNotifier.class);

    public SoapSubscriptionNotifier(MediatorConfig config) {
        this.config = config;
    }

    @Override
    public void notifySubscription(Subscription subscription, String documentId) {
        String hostAdress = config.getProperty("core.host");
        ITI53NotifyMessage message = new ITI53NotifyMessage(subscription.getUrl(), hostAdress, documentId);

        byte[] postData = message.generateMessage().getBytes(StandardCharsets.UTF_8);

        sendMessage(subscription.getUrl(), postData);
    }

    private void sendMessage(String url, byte[] body) {
        HttpURLConnection con = null;
        try {

            URL myurl = new URL(url);

            con = (HttpURLConnection) myurl.openConnection();

            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/soap+xml");

            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.write(body);
            }

            StringBuilder content;

            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {

                String line;
                content = new StringBuilder();

                while ((line = in.readLine()) != null) {
                    content.append(line);
                    content.append(System.lineSeparator());
                }
            }

            System.out.println(content.toString());
        } catch (IOException exception) {
            LOGGER.error(exception.getMessage());
        } finally {
            con.disconnect();
        }
    }
}
