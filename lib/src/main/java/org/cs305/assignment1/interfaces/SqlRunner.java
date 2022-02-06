package org.cs305.assignment1.interfaces;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
* A class implementing this interface will execute arbitrary SQL statements
* (INSERT, UPDATE, SELECT, DELETE). The SQL queries will be specified in an
* XML file named queries.xml by default and placed at a well-known location.
* The structure of the XML file will be as follows:
* <queries>
*     <sql id="findMovies" paramType="org.foo.Bar" mapRowTo="org.foo.Movie">
*      <![CDATA[
*      SELECT a, b, c FROM my_table WHERE x=${propX} AND y=${propY};
*      ]]>
*     </sql>
*     <sql id="addMovie" paramType="org.foo.Bar">
*        <![CDATA[
*        INSERT INTO my_table(x, y, x) VALUES(${propX}, ${propY}, ${propZ});
*        ]]>
*     </sql>
* </queries>
*
* In the above XML, the "mapRowTo" attribute of "sql" element will be used to
* specify the FQN of the class whose instance will be created for each row.
* The value of columns selected by the query will be populated into the matching
* property of the class. Matching will be done in strict mode by default, which
* means that if the class does not have a matching property for a selected
* column, then the method will throw an exception.
*/
public interface SqlRunner {
   /**
    * Executes a select query that returns a single or no record.
    * @param queryId Unique ID of the query in the queries.xml file.
    * @param queryParam Parameter(s) to be used in the query.
    * @param resultType Type of the object that will be returned after populating it with the data returned by the SQL.
    * @return The object populated with the SQL results.
    */
    <R, P> R selectOne(String queryId, P queryParam, Class<R> resultType) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException;

    /**
     * Same as {@link #selectOne(String, P, Class<R>)} except that this one returns multiple rows.
     * @param queryId
     * @param queryParam
     * @param resultItemType
     * @return The object populated with the SQL results.
     */
    <R, P> List<R> selectMany(String queryId, P queryParam, Class<R> resultItemType) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException;

    /**
     * Execute an update statement and return the number of rows affected.
     * @param queryId
     * @param queryParam
     * @return number of rows affected
     */
    <P> int update(String queryId, P queryParam);

    /**
     * Execute an insert statement and return the number of rows affected.
     * @param queryId
     * @param queryParam
     * @return number of rows affected
     */
    <P> int insert(String queryId, P queryParam);

    /**
     * Execute a delete statement and return the number of rows affected.
     * @param queryId
     * @param queryParam
     * @return number of rows affected
     */
    <P> int delete(String queryId, P queryParam);
}
