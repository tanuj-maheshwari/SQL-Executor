package org.cs305.assignment1.interfaces;

public interface XmlParser {
    /**
    * Parses the XML file to get the raw query.
    * @param queryId the id of the required query
    * @param queryParam Parameter(s) to be used in the query.
    * @return The raw SQL query
    */
    <P> String getRawQueryFromXML(String queryId, P queryParam);
}
