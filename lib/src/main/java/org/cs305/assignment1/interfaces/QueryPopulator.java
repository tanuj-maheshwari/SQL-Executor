package org.cs305.assignment1.interfaces;

public interface QueryPopulator {
    /**
    * Populates a raw SQL query with the fields of parameter received
    * @param rawQuery raw SQL query
    * @param queryParam Parameter(s) to be used in the query.
    * @return The populated SQL query
    */
    <P> String populateRawQuery(String rawQuery, P queryParam);
}
