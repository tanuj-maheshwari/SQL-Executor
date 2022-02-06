package org.cs305.assignment1;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
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

    public SqlExecuter(String pathToXMLFile, Connection dbConnection) {
        this.pathToXMLFile = pathToXMLFile;
        this.dbConnection = dbConnection;
    }

    /**
    * Parses the XML file to get the raw query.
    * @param queryId the id of the required query
    * @param queryParam Parameter(s) to be used in the query.
    * @return The raw SQL query
    */
    public <P> String getRawQueryFromXML(String queryId, P queryParam) {
        try {
            //get XML file as a document
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
                    if(sqlElement.getAttribute("paramType").equals(queryParam.getClass().getName())) {
                        rawQuery = sqlElement.getTextContent().trim();
                        break;
                    }
                    else {
                        throw new ClassCastException("Parameter object type mismatch");
                    }
                }
            }
            //query shouldn't be empty
            if(rawQuery.equals("")) {
                throw new Exception("No query with id = " + queryId + " found");
            }

            return rawQuery;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return null; //to remove comiler errors
        }
    }

    /**
    * Populates a raw SQL query with the fields of parameter received
    * @param rawQuery raw SQL query
    * @param queryParam Parameter(s) to be used in the query.
    * @return The populated SQL query
    */
    public <P> String populateRawQuery(String rawQuery, P queryParam) {
        try {
            String populatedQuery = "";

            Field[] paramFields = queryParam.getClass().getDeclaredFields();

            if(paramFields.length == 0) {
                //--------NOT GETTING CATCHED--------
                //primitive type
                //only handling non-array values for now
                populatedQuery = rawQuery.replace("${value}", queryParam.toString().trim());
                //--------NOT GETTING CATCHED--------
            }
            else {
                //class type
                while (rawQuery.indexOf("${") != -1) {
                    int start = rawQuery.indexOf("${");
                    int end = rawQuery.indexOf("}");
                    String propName = rawQuery.substring(start+2, end);
                    String propValue = "";
                    for(int i=0; i<paramFields.length; i++) {
                        if(propName.equals(paramFields[i].getName())) {
                            propValue = paramFields[i].get(queryParam).toString().trim();
                            //if the field is of type string
                            if(paramFields[i].getType() == "String".getClass()) {
                                propValue = "\"" + propValue + "\"";
                            }
                            break;
                        }
                    }
                    //if such property doesn't exist
                    if(propValue.equals("")) {
                        throw new NoSuchFieldException("No field " + propName + " for class " + queryParam.getClass().getName() + " found");
                    }
                    rawQuery = rawQuery.substring(0, start) + propValue + rawQuery.substring(end+1);
                }
            }
            populatedQuery = rawQuery;
            return populatedQuery;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return null; //to remove comiler errors
        }
    }

    /**
     * 
     * -------------BELOW CONTAINS THE IMPLEMENTATION OF 
     * {@link #getRawQueryFromXML(String, P)} and 
     * {@link #populateRawQuery(String, P)}
     * REFACTORED AS ONE METHOD-------------
     * 
     * Gets the query from the XML file
     * @param queryId Unique ID of the query in the queries.xml file.
     * @param queryParam Parameter(s) to be used in the query.
     * @return Query
     * 
    private <P> String getQueryFromXML(String queryId, P queryParam) {
        try {
            //get XML file as a document
            File xmlFile = new File(pathToXMLFile);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();
            Document doc = dbBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            //get the Query with queryID
            String query = "";
            NodeList nList = doc.getElementsByTagName("sql");
            for(int i=0; i<nList.getLength(); i++) {
                Element sqlElement = (Element) nList.item(i);
                if(queryId.equals(sqlElement.getAttribute("id"))) {
                    //paramType in XML should match with object type passed
                    if(sqlElement.getAttribute("paramType").equals(queryParam.getClass().getName())) {
                        query = sqlElement.getTextContent().trim();
                        break;
                    }
                    else {
                        throw new ClassCastException("Parameter object type mismatch");
                    }
                }
            }
            //query shouldn't be empty
            if(query.equals("")) {
                throw new Exception("No query with id = " + queryId + " found");
            }

            //populate the query with values
            Field[] paramFields = queryParam.getClass().getDeclaredFields();
            
            if(paramFields.length == 0) {
                //primitive type
                //only handling non-array values for now
                query = query.replace("${value}", queryParam.toString().trim());
            }
            else {
                //class type
                while (query.indexOf("${") != -1) {
                    int start = query.indexOf("${");
                    int end = query.indexOf("}");
                    String propName = query.substring(start+2, end);
                    String propValue = "";
                    for(int i=0; i<paramFields.length; i++) {
                        if(propName.equals(paramFields[i].getName())) {
                            propValue = paramFields[i].get(queryParam).toString().trim();
                            break;
                        }
                    }
                    //if such property doesn't exist
                    if(propName.equals("")) {
                        throw new NoSuchFieldException("No field " + propName + " for class " + queryParam.getClass().getName() + " found");
                    }
                    query = query.substring(0, start) + propValue + query.substring(end+1);
                }
            }

            return query;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }   
    }
    */

    private <P> ResultSet getResultSet(String queryId, P queryParam) {
        try {
            String rawQuery = this.getRawQueryFromXML(queryId, queryParam);
            String finalQuery = this.populateRawQuery(rawQuery, queryParam);

            Statement dbStatement = dbConnection.createStatement();
            
            ResultSet resultSet = dbStatement.executeQuery(finalQuery);
            return resultSet;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return null;
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
                String columnName = rsMetaData.getColumnName(i);
                if(resultTypeFieldNames.contains(columnName)) {
                    //setter function for field "name" is -> setName
                    String setterMethodName = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
                    Method setterMethod = sqlResult.getClass().getMethod(setterMethodName);
                    
                    //find the field to get its class type
                    for(int j=0; j<resultTypeFields.length; j++) {
                        if(resultTypeFields[j].getName().equals(columnName)) {
                            //cast the result into the datatype of object's field
                            String fieldClassName = resultTypeFields[j].getClass().getName();
                            setterMethod.invoke(sqlResult, Class.forName(fieldClassName).cast(resultSet.getObject(columnName)));
                        }
                    }
                }
                else {
                    throw new NoSuchFieldException("Field " + rsMetaData.getColumnName(i) + " doesn't exist for class " + resultType.getClass().getName());
                }
            }

            return sqlResult;
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(0);
            return null;
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
            //Get resultSet and the corresponding meta data
            ResultSet resultSet = this.getResultSet(queryId, queryParam);
            if(!resultSet.next()) {
                return null;
            }
            ResultSetMetaData rsMetaData = resultSet.getMetaData();

            //Initialize return object item and corresponding fields in it's class
            R tempSqlResultItem = resultItemType.getDeclaredConstructor().newInstance();
            Field[] resultTypeFields = tempSqlResultItem.getClass().getDeclaredFields();
            List<String> resultTypeFieldNames = new ArrayList<>();
            for(int a=0; a<resultTypeFields.length; a++) {
                resultTypeFieldNames.add(resultTypeFields[a].getName());
            }

            //final result list
            List<R> sqlResultList = new ArrayList<>();

            //repeat for all rows in query result
            do {
                R sqlResultItem = resultItemType.getDeclaredConstructor().newInstance();
                //populate POJO for all columns in select query
                for(int i=1; i<=rsMetaData.getColumnCount(); i++) {
                    String columnName = rsMetaData.getColumnName(i);
                    if(resultTypeFieldNames.contains(columnName)) {
                        //setter function for field "name" is -> setName
                        String setterMethodName = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
                        Method setterMethod = sqlResultItem.getClass().getMethod(setterMethodName);
                        
                        //find the field to get its class type
                        for(int j=0; j<resultTypeFields.length; j++) {
                            if(resultTypeFields[j].getName().equals(columnName)) {
                                //cast the result into the datatype of object's field
                                String fieldClassName = resultTypeFields[j].getClass().getName();
                                setterMethod.invoke(sqlResultItem, Class.forName(fieldClassName).cast(resultSet.getObject(columnName)));
                            }
                        }
                    }
                    else {
                        throw new NoSuchFieldException("Field " + rsMetaData.getColumnName(i) + " doesn't exist for class " + resultItemType.getClass().getName());
                    }
                }

                //add the item in the final result list
                sqlResultList.add(sqlResultItem);
            } while (resultSet.next());

            return sqlResultList;
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(0);
            return null;
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
            e.printStackTrace();
            System.exit(0);
            return 0;
        }
    }
 
    /**
     * Execute an insert statement and return the number of rows affected.
     * @param queryId
     * @param queryParam
     * @return number of rows affected
     */
    public <P> int insert(String queryId, P queryParam) {
        try {
            String rawQuery = this.getRawQueryFromXML(queryId, queryParam);
            String finalQuery = this.populateRawQuery(rawQuery, queryParam);

            Statement dbStatement = dbConnection.createStatement();
            
            int numRowsAffected = dbStatement.executeUpdate(finalQuery);
            return numRowsAffected;
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(0);
            return 0;
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
            e.printStackTrace();
            System.exit(0);
            return 0;
        }
    }
}
