package org.cs305.assignment1.interfaces;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public interface XmlParser {
    /**
    * Parses the XML file to get the raw query.
    * @param queryId the id of the required query
    * @param queryParam Parameter(s) to be used in the query.
    * @return The raw SQL query
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws Exception
    */
    <P> String getRawQueryFromXML(String queryId, P queryParam);
}
