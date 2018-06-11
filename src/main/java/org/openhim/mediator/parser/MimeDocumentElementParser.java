package org.openhim.mediator.parser;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MimeDocumentElementParser {

    private static final String REGEX = ".*[\\*:<]([a-zA-Z0-9_-]*)[@>].*";

    public static String getBodyPartContentId(BodyPart part) throws MessagingException {
        String contentId = null;
        String[] contentIds = part.getHeader("Content-Id");
        if (contentIds.length > 0) {
            contentId = contentIds[0];

            Pattern pattern = Pattern.compile(REGEX);
            Matcher matcher = pattern.matcher(contentId);
            if (matcher.find()) {
                contentId = matcher.group(1);
            }
        }
        return contentId;
    }
}
