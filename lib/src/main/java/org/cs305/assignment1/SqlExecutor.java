package org.cs305.assignment1;

import java.lang.reflect.Field;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.*;

import javax.xml.parsers.*;
import java.io.*;
import java.sql.*;

import org.cs305.assignment1.interfaces.QueryPopulator;
import org.cs305.assignment1.interfaces.SqlRunner;
import org.cs305.assignment1.interfaces.XmlParser;

public class SqlExecutor implements SqlRunner, XmlParser, QueryPopulator {
    
    private final String pathToXMLFile;
    private final Connection dbConnection;

    /**
     * Default constructor
     * @param pathToXMLFile Absolute path to the XML file
     * @param dbConnection Database connection object (of type java.sql.Connection.class)
     */
    public SqlExecutor(String pathToXMLFile, Connection dbConnection) {
        this.pathToXMLFile = pathToXMLFile;
        this.dbConnection = dbConnection;
    }

    /**
    * Checks if a class is a wrapper for primitive types
    * @param clazz Class of object
    * @return true if class is a wrapper
    */
    private static boolean isWrapperType(Class<?> clazz) {
        return clazz.equals(Boolean.class) || 
            clazz.equals(Integer.class) ||
            clazz.equals(Character.class) ||
            clazz.equals(Byte.class) ||
            clazz.equals(Short.class) ||
            clazz.equals(Double.class) ||
            clazz.equals(Long.class) ||
            clazz.equals(Float.class);
    }

    /**
    * Parses the XML file to get the raw query.
    * @param queryId the id of the required query
    * @param queryParam Parameter(s) to be used in the query.
    * @return The raw SQL query
    */
    public <P> String getRawQueryFromXML(String queryId, P queryParam) {
        //get XML file as a document
        try{
            File xmlFile = new File(pathToXMLFile);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();
            Document doc = dbBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            //get the Query with queryID
            String rawQuery = "";
            NodeList nList = doc.getElementsByTagName("sql");
            for(int i=0; i<nList.getLength(); i++) {
                Element sqlElement = (Element) nList.item(i);
                if(queryId.equals(sqlElement.getAttribute("id"))) {
                    //paramType in XML should match with object type passed
                    //if queryParam is null, paramType must also be null
                    if(queryParam == null) {
                        if (sqlElement.getAttribute("paramType").equals("null")) {
                            rawQuery = sqlElement.getTextContent().trim();
                            break;
                        }
                        else {
                            throw new RuntimeException("Null argument");
                        }
                    }
                    //if queryParam is not null, the classes must match
                    else if(sqlElement.getAttribute("paramType").equals(queryParam.getClass().getName())) {
                        rawQuery = sqlElement.getTextContent().trim();
                        break;
                    } else {
                        throw new RuntimeException("Parameter object type mismatch, classes " + sqlElement.getAttribute("paramType") + " and " + queryParam.getClass().getName());
                    }
                }
            }
            //query shouldn't be empty
            if(rawQuery.equals("")) {
                throw new RuntimeException("No query with id = " + queryId + " found");
            }
            return rawQuery;
        } catch(Exception e) {
        throw new RuntimeException(e);
        }
    }

    /**
    * Gets the value of the queryParam to be used in the query. 
    * Returns string to replace the respective ${prop} in query parsed from XML. 
    * Accepts primitive(& wrapper), string, and array of these types.
    * @param queryParam Parameter(s) to be used in the query.
    * @return The raw SQL query
    */
    private static <P> String getValueForPropertyPlaceholders(P queryParam) {
        String toBeReplacedBy = "";
        if(isWrapperType(queryParam.getClass())) {
            //primitive type wrappers
            toBeReplacedBy = queryParam.toString().trim();
        }
        else if(queryParam.getClass() == String.class) {
            //string type
            toBeReplacedBy = "\"" + queryParam.toString().trim() + "\"";
        }
        else if(queryParam.getClass().isArray()) {
            //array
            //convert to object array
            int arrayLength = Array.getLength(queryParam);
            Object[] queryParamObjects = new Object[arrayLength];
            for(int a = 0; a<arrayLength; a++) {
                queryParamObjects[a] = Array.get(queryParam, a);
            }
            
            toBeReplacedBy = toBeReplacedBy + "(";
            for(int i=0; i<queryParamObjects.length; i++) {
                String eachValue;
                eachValue = queryParamObjects[i].toString();
                //add "" if object is of type string
                //if(queryParamObjects.getClass().getComponentType() == String.class) {
                if(queryParam.getClass().getComponentType() == String.class) {
                    eachValue = "\"" + eachValue + "\"";
                }
                toBeReplacedBy = toBeReplacedBy + eachValue;
                //add , in between if not the last object
                if(i < queryParamObjects.length-1) {
                    toBeReplacedBy = toBeReplacedBy + ", ";
                } 
            }
            toBeReplacedBy = toBeReplacedBy + ")";
        }
        else {
            //throw new RuntimeException("Parameter fields must be primitive(wrapper), string, array/collection type only.");
            toBeReplacedBy = queryParam.toString();
        }
        return toBeReplacedBy;
    }

    /**
    * Populates a raw SQL query with the fields of parameter received
    * @param rawQuery raw SQL query
    * @param queryParam Parameter(s) to be used in the query.
    * @return The populated SQL query
    */
    public <P> String populateRawQuery(String rawQuery, P queryParam) {
        if(queryParam == null) {
            //if no parameters for query, return the raw query itself
            return rawQuery;
        }
        String populatedQuery;
        Field[] paramFields = queryParam.getClass().getDeclaredFields();
        if(isWrapperType(queryParam.getClass()) //primitive wrappers
            || queryParam.getClass() == String.class //string
            || queryParam.getClass().isArray()) { //array
            populatedQuery = rawQuery.replace("${value}", getValueForPropertyPlaceholders(queryParam));
        }
        else if(queryParam instanceof Collection<?> queryParamCollection) {
            //collection type
            //convert to object array and then use getValueForPropertyPlaceholders method
            Object[] queryParamObjects = queryParamCollection.toArray();
            populatedQuery = rawQuery.replace("${value}", getValueForPropertyPlaceholders(queryParamObjects));
        }
        else {
            //generic class type
            while (rawQuery.contains("${")) {
                int start = rawQuery.indexOf("${");
                int end = rawQuery.indexOf("}");
                String propName = rawQuery.substring(start+2, end);
                String propValue = "";
                for (Field paramField : paramFields) {
                    if (propName.equals(paramField.getName())) {
                        Object propObject = new Object();
                        try {
                            propObject = paramField.get(queryParam);
                        } catch (Exception ignored) {
                        }
                        propValue = getValueForPropertyPlaceholders(propObject);
                        break;
                    }
                }
                //if such property doesn't exist
                if(propValue.equals("")) {
                    throw new RuntimeException("No field " + propName + " for class " + queryParam.getClass().getName() + " found");
                }
                rawQuery = rawQuery.substring(0, start) + propValue + rawQuery.substring(end+1);
            }
            populatedQuery = rawQuery;
        }
        return populatedQuery;
    }

    private <P> ResultSet getResultSet(String queryId, P queryParam) {
        try {
            String rawQuery = this.getRawQueryFromXML(queryId, queryParam);
            String finalQuery = this.populateRawQuery(rawQuery, queryParam);
            Statement dbStatement = dbConnection.createStatement();

            return dbStatement.executeQuery(finalQuery);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes a select query that returns a single or no record.
     * @param queryId Unique ID of the query in the queries.xml file.
     * @param queryParam Parameter(s) to be used in the query.
     * @param resultType Type of the object that will be returned after populating it with the data returned by the SQL.
     * @return The object populated with the SQL results.
     */
    public <R, P> R selectOne(String queryId, P queryParam, Class<R> resultType) {  
        try {
            //Get resultSet and the corresponding meta data
            ResultSet resultSet = this.getResultSet(queryId, queryParam);
            if(!resultSet.next()) {
                return null;
            }
            ResultSetMetaData rsMetaData = resultSet.getMetaData();

            //Initialize return object and corresponding fields in its class
            R sqlResult = resultType.getDeclaredConstructor().newInstance();
            Field[] resultTypeFields = sqlResult.getClass().getDeclaredFields();
            List<String> resultTypeFieldNames = new ArrayList<>();
            for (Field typeField : resultTypeFields) {
                resultTypeFieldNames.add(typeField.getName());
            }

            //populate POJO for all columns in select query
            for(int i=1; i<=rsMetaData.getColumnCount(); i++) {
                String columnName = rsMetaData.getColumnLabel(i);
                if(resultTypeFieldNames.contains(columnName)) {
                    //setter function for field "name" is -> setName
                    String setterMethodName = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
                    
                    //find the field to get its class type
                    for (Field resultTypeField : resultTypeFields) {
                        if (resultTypeField.getName().equals(columnName)) {
                            Method setterMethod = sqlResult.getClass().getDeclaredMethod(setterMethodName, resultTypeField.getType());
                            setterMethod.invoke(sqlResult, resultSet.getObject(columnName));
                            //resultTypeFields[j].set(sqlResult, resultSet.getObject(columnName));
                        }
                    }
                }
                else {
                    throw new RuntimeException("Field " + rsMetaData.getColumnLabel(i) + " doesn't exist for class " + resultType.getName());
                }
            }
            
            //if more than one record is present
            if(resultSet.next()) {
                throw new RuntimeException("More than one rows in SELECT query");
            }

            return sqlResult;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
 
    /**
     * Same as {@link #selectOne(String, P, Class)} except that this one returns multiple rows.
     * @param queryId Unique ID of the query in the queries.xml file.
     * @param queryParam Parameter(s) to be used in the query.
     * @param resultItemType Type of the List item that will be returned after populating it with the data returned by the SQL.
     * @return The object populated with the SQL results.
     */
    public <R, P> List<R> selectMany(String queryId, P queryParam, Class<R> resultItemType) {
        try {
            //final result list
            List<R> sqlResultList = new ArrayList<>();

            //Get resultSet and the corresponding meta data
            ResultSet resultSet = this.getResultSet(queryId, queryParam);
            if(!resultSet.next()) {
                return sqlResultList;
            }
            ResultSetMetaData rsMetaData = resultSet.getMetaData();

            //Initialize return object item and corresponding fields in its class
            R tempSqlResultItem = resultItemType.getDeclaredConstructor().newInstance();
            Field[] resultTypeFields = tempSqlResultItem.getClass().getDeclaredFields();
            List<String> resultTypeFieldNames = new ArrayList<>();
            for (Field resultTypeField : resultTypeFields) {
                resultTypeFieldNames.add(resultTypeField.getName());
            }

            //repeat for all rows in query result
            do {
                R sqlResultItem = resultItemType.getDeclaredConstructor().newInstance();
                //populate POJO for all columns in select query
                for(int i=1; i<=rsMetaData.getColumnCount(); i++) {
                    String columnName = rsMetaData.getColumnLabel(i);
                    if(resultTypeFieldNames.contains(columnName)) {
                        //setter function for field "name" is -> setName
                        String setterMethodName = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
                        
                        //find the field to get its class type
                        for (Field resultTypeField : resultTypeFields) {
                            if (resultTypeField.getName().equals(columnName)) {
                                Method setterMethod = sqlResultItem.getClass().getMethod(setterMethodName, resultTypeField.getType());
                                setterMethod.invoke(sqlResultItem, resultSet.getObject(columnName));
                                //resultTypeFields[j].set(sqlResultItem, resultSet.getObject(columnName));
                            }
                        }
                    }
                    else {
                        throw new RuntimeException("Field " + rsMetaData.getColumnLabel(i) + " doesn't exist for class " + resultItemType.getName());
                    }
                }

                //add the item in the final result list
                sqlResultList.add(sqlResultItem);
            } while (resultSet.next());

            return sqlResultList;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
 
    /**
     * Execute an update statement and return the number of rows affected.
     * @param queryId Unique ID of the query in the queries.xml file.
     * @param queryParam Parameter(s) to be used in the query.
     * @return number of rows affected
     */
    public <P> int update(String queryId, P queryParam) {
        try {
            String rawQuery = this.getRawQueryFromXML(queryId, queryParam);
            String finalQuery = this.populateRawQuery(rawQuery, queryParam);

            Statement dbStatement = dbConnection.createStatement();

            return dbStatement.executeUpdate(finalQuery);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
 
    /**
     * Execute an insert statement and return the number of rows affected.
     * @param queryId Unique ID of the query in the queries.xml file.
     * @param queryParam Parameter(s) to be used in the query.
     * @return number of rows affected
     */
    public <P> int insert(String queryId, P queryParam) {
        try {
            String rawQuery = this.getRawQueryFromXML(queryId, queryParam);
            String finalQuery = this.populateRawQuery(rawQuery, queryParam);

            Statement dbStatement = dbConnection.createStatement();

            return dbStatement.executeUpdate(finalQuery);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
 
    /**
     * Execute a delete statement and return the number of rows affected.
     * @param queryId Unique ID of the query in the queries.xml file.
     * @param queryParam Parameter(s) to be used in the query.
     * @return number of rows affected
     */
    public <P> int delete(String queryId, P queryParam) {
        try {
            String rawQuery = this.getRawQueryFromXML(queryId, queryParam);
            String finalQuery = this.populateRawQuery(rawQuery, queryParam);

            Statement dbStatement = dbConnection.createStatement();

            return dbStatement.executeUpdate(finalQuery);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
