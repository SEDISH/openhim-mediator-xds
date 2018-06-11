package org.openhim.mediator.parser;

import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
import org.apache.commons.lang3.StringUtils;

public class XopIncludeElementParser {

    private static final int CONTENT_ID = 1;
    private static final String CONTENT_ID_ATTRIBUTE = "href";

    public static String getContentId(Object element) {
        String contentId = null;
        if (element instanceof ElementNSImpl) {
            ElementNSImpl xopInclude = (ElementNSImpl)element;
            String header = xopInclude.getAttribute(CONTENT_ID_ATTRIBUTE);
            if (StringUtils.contains(header, ":")) {
                contentId = xopInclude.getAttribute(CONTENT_ID_ATTRIBUTE).split(":")[CONTENT_ID].trim();
            }
        }
        return contentId;
    }

}
