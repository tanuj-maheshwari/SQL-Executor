package org.cs305.assignment1;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.w3c.dom.*;

import javax.xml.parsers.*;
import java.io.*;
import java.sql.*;

import org.cs305.assignment1.interfaces.QueryPopulator;
import org.cs305.assignment1.interfaces.SqlRunner;
import org.cs305.assignment1.interfaces.XmlParser;

public class SqlExecuter implements SqlRunner, XmlParser, QueryPopulator {
    
    private final String pathToXMLFile;
    private final Connection dbConnection;

    /**
     * Default consructor
     * @param pathToXMLFile Absolute path to the XML file
     * @param dbConnection Database connection object (of type java.sql.Connection.class)
     */
    public SqlExecuter(String pathToXMLFile, Connection dbConnection) {
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
     * @throws Exception
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
    * Converts an object of primitive type array to an object array
    * @param queryParam Primitive type array
    * @return object array
    */
    private static <P> Object[] getObjectArrayFromPrimitiveArrayObject(P queryParam) {
        if(queryParam.getClass().getComponentType() == int.class) {
            int[] queryParamArray = (int[]) queryParam;
            Object[] objectArray = new Object[queryParamArray.length];
            for(int i=0; i<queryParamArray.length; i++) {
                objectArray[i] = (Object) queryParamArray[i];
            }
            return objectArray;
        }
        else if(queryParam.getClass().getComponentType() == byte.class) {
            byte[] queryParamArray = (byte[]) queryParam;
            Object[] objectArray = new Object[queryParamArray.length];
            for(int i=0; i<queryParamArray.length; i++) {
                objectArray[i] = (Object) queryParamArray[i];
            }
            return objectArray;
        }
        else if(queryParam.getClass().getComponentType() == short.class) {
            short[] queryParamArray = (short[]) queryParam;
            Object[] objectArray = new Object[queryParamArray.length];
            for(int i=0; i<queryParamArray.length; i++) {
                objectArray[i] = (Object) queryParamArray[i];
            }
            return objectArray;
        }
        else if(queryParam.getClass().getComponentType() == long.class) {
            long[] queryParamArray = (long[]) queryParam;
            Object[] objectArray = new Object[queryParamArray.length];
            for(int i=0; i<queryParamArray.length; i++) {
                objectArray[i] = (Object) queryParamArray[i];
            }
            return objectArray;
        }
        else if(queryParam.getClass().getComponentType() == float.class) {
            float[] queryParamArray = (float[]) queryParam;
            Object[] objectArray = new Object[queryParamArray.length];
            for(int i=0; i<queryParamArray.length; i++) {
                objectArray[i] = (Object) queryParamArray[i];
            }
            return objectArray;
        }
        else if(queryParam.getClass().getComponentType() == double.class) {
            double[] queryParamArray = (double[]) queryParam;
            Object[] objectArray = new Object[queryParamArray.length];
            for(int i=0; i<queryParamArray.length; i++) {
                objectArray[i] = (Object) queryParamArray[i];
            }
            return objectArray;
        }
        else if(queryParam.getClass().getComponentType() == boolean.class) {
            boolean[] queryParamArray = (boolean[]) queryParam;
            Object[] objectArray = new Object[queryParamArray.length];
            for(int i=0; i<queryParamArray.length; i++) {
                objectArray[i] = (Object) queryParamArray[i];
            }
            return objectArray;
        }
        else {
            char[] queryParamArray = (char[]) queryParam;
            Object[] objectArray = new Object[queryParamArray.length];
            for(int i=0; i<queryParamArray.length; i++) {
                objectArray[i] = (Object) queryParamArray[i];
            }
            return objectArray;
        }
    }

    /**
    * Gets the value of the queryParam to be used in the query. 
    * Returns string to replace the respective ${prop} in query parsed from XML. 
    * Accepts primitive(& wrapper), date, string, and array of these types.
    * @param queryParam Parameter(s) to be used in the query.
    * @return The raw SQL query
     * @throws Exception
    */
    private static <P> String getValueForPropertyPlaceholders(P queryParam) {
        String toBeReplacedBy = new String("");
        if(isWrapperType(queryParam.getClass()) || queryParam.getClass() == Date.class) {
            //primitive type wrappers
            toBeReplacedBy = queryParam.toString().trim();
        }
        else if(queryParam.getClass() == String.class) {
            //string type
            toBeReplacedBy = new String("\'" + queryParam.toString().trim() + "\'");
        }
        else if(queryParam.getClass().isArray()) {
            //array
            Object[] queryParamObjects;
            //convert to object array
            if(queryParam.getClass().getComponentType().isPrimitive()) {
                queryParamObjects = getObjectArrayFromPrimitiveArrayObject(queryParam);
            }
            else {
                queryParamObjects = (Object[]) queryParam;
            }
            toBeReplacedBy = toBeReplacedBy + "(";
            for(int i=0; i<queryParamObjects.length; i++) {
                String eachValue = new String("");
                eachValue = queryParamObjects[i].toString();
                //add "" if object is of type string
                if(queryParamObjects.getClass().getComponentType() == String.class) {
                    eachValue = "\'" + eachValue + "\'";
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
     * @throws Exception
    */
    public <P> String populateRawQuery(String rawQuery, P queryParam) {
        if(queryParam == null) {
            //if no parameters for query, return the raw query itself
            return rawQuery;
        }
        String populatedQuery = new String("");
        Field[] paramFields = queryParam.getClass().getDeclaredFields();
        if(isWrapperType(queryParam.getClass()) //primitive wrappers
            || queryParam.getClass() == Date.class //date
            || queryParam.getClass() == String.class //string
            || queryParam.getClass().isArray()) { //array
            populatedQuery = rawQuery.replace("${value}", getValueForPropertyPlaceholders(queryParam));
        }
        else if(queryParam instanceof Collection) {
            //collection type
            //convert to object array and then use getValueForPropertyPlaceholders method
            Collection<?> queryParamCollection = (Collection<?>) queryParam; 
            Object[] queryParamObjects = queryParamCollection.toArray();
            populatedQuery = rawQuery.replace("${value}", getValueForPropertyPlaceholders(queryParamObjects));
        }
        else {
            //generic class type
            while (rawQuery.indexOf("${") != -1) {
                int start = rawQuery.indexOf("${");
                int end = rawQuery.indexOf("}");
                String propName = rawQuery.substring(start+2, end);
                String propValue = "";
                for(int i=0; i<paramFields.length; i++) {
                    if(propName.equals(paramFields[i].getName())) {
                        Object propObject = new Object();
                        try {
                            propObject = paramFields[i].get(queryParam);
                        } catch (Exception e) {}
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
        
        ResultSet resultSet = dbStatement.executeQuery(finalQuery);
        return resultSet;
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

            //Initialize return object and corresponding fields in it's class
            R sqlResult = resultType.getDeclaredConstructor().newInstance();
            Field[] resultTypeFields = sqlResult.getClass().getDeclaredFields();
            List<String> resultTypeFieldNames = new ArrayList<>();
            for(int a=0; a<resultTypeFields.length; a++) {
                resultTypeFieldNames.add(resultTypeFields[a].getName());
            }

            //populate POJO for all columns in select query
            for(int i=1; i<=rsMetaData.getColumnCount(); i++) {
                String columnName = rsMetaData.getColumnLabel(i);
                if(resultTypeFieldNames.contains(columnName)) {
                    //setter function for field "name" is -> setName
                    String setterMethodName = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
                    
                    //find the field to get its class type
                    for(int j=0; j<resultTypeFields.length; j++) {
                        if(resultTypeFields[j].getName().equals(columnName)) {
                            Method setterMethod = sqlResult.getClass().getDeclaredMethod(setterMethodName, resultTypeFields[j].getType());
                            setterMethod.invoke(sqlResult, resultSet.getObject(columnName));
                            //resultTypeFields[j].set(sqlResult, resultSet.getObject(columnName));
                        }
                    }
                }
                else {
                    throw new RuntimeException("Field " + rsMetaData.getColumnLabel(i) + " doesn't exist for class " + resultType.getClass().getName());
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
     * Same as {@link #selectOne(String, P, Class<R>)} except that this one returns multiple rows.
     * @param queryId
     * @param queryParam
     * @param resultItemType
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

            //Initialize return object item and corresponding fields in it's class
            R tempSqlResultItem = resultItemType.getDeclaredConstructor().newInstance();
            Field[] resultTypeFields = tempSqlResultItem.getClass().getDeclaredFields();
            List<String> resultTypeFieldNames = new ArrayList<>();
            for(int a=0; a<resultTypeFields.length; a++) {
                resultTypeFieldNames.add(resultTypeFields[a].getName());
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
                        for(int j=0; j<resultTypeFields.length; j++) {
                            if(resultTypeFields[j].getName().equals(columnName)) {
                                Method setterMethod = sqlResultItem.getClass().getMethod(setterMethodName, resultTypeFields[j].getType());
                                setterMethod.invoke(sqlResultItem, resultSet.getObject(columnName));
                                //resultTypeFields[j].set(sqlResultItem, resultSet.getObject(columnName));
                            }
                        }
                    }
                    else {
                        throw new RuntimeException("Field " + rsMetaData.getColumnLabel(i) + " doesn't exist for class " + resultItemType.getClass().getName());
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
     * @param queryId
     * @param queryParam
     * @return number of rows affected
     */
    public <P> int update(String queryId, P queryParam) {
        try {
            String rawQuery = this.getRawQueryFromXML(queryId, queryParam);
            String finalQuery = this.populateRawQuery(rawQuery, queryParam);

            Statement dbStatement = dbConnection.createStatement();
            
            int numRowsAffected = dbStatement.executeUpdate(finalQuery);
            return numRowsAffected;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
 
    /**
     * Execute an insert statement and return the number of rows affected.
     * @param queryId
     * @param queryParam
     * @return number of rows affected
     * @throws Exception
     */
    public <P> int insert(String queryId, P queryParam) {
        try {
            String rawQuery = this.getRawQueryFromXML(queryId, queryParam);
            String finalQuery = this.populateRawQuery(rawQuery, queryParam);

            Statement dbStatement = dbConnection.createStatement();
            
            int numRowsAffected = dbStatement.executeUpdate(finalQuery);
            return numRowsAffected;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
 
    /**
     * Execute a delete statement and return the number of rows affected.
     * @param queryId
     * @param queryParam
     * @return number of rows affected
     */
    public <P> int delete(String queryId, P queryParam) {
        try {
            String rawQuery = this.getRawQueryFromXML(queryId, queryParam);
            String finalQuery = this.populateRawQuery(rawQuery, queryParam);

            Statement dbStatement = dbConnection.createStatement();
            
            int numRowsAffected = dbStatement.executeUpdate(finalQuery);
            return numRowsAffected;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
