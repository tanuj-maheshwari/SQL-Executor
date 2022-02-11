# SQL Executor library

> Submitter name: Tanuj Maheshwari
>
> Roll No.: 2019CSB1125
>
> Course: CS305 (Software Engineering)

> GitHub repo - [link](https://github.com/tanuj-maheshwari/cs305_2022/tree/assignment_1)

Java based library for executing SQL queries for CRUD operations against an RDBMS.


## What does this program do?

This is a Java library that can be used for executing basic SQL queries, i.e. `SELECT`, `INSERT`, `DELETE` and `UPDATE` (CRUD operations) against an RDMS such as MySQL, PostgreSQL, etc.

The SQL queries to be processed are grabbed from an XML file with a specific format, an example of which can be found [here](./lib/src/test/resources/SQLTest.xml). The queries can be complete on their own, or could be dynamically populated from user defined parameters at run time. The results from the SQL queries are used to populate plain old java objects (POJOs).


## A description of how this program works (i.e. its logic)

### Flow of logic

#### Parsing the XML file

First, the XML file is loaded from the path provided. The desired SQL query is then parsed from the XML file using the unique `id` parameter of each `<sql>` tag. The subsequent query received is termed as "raw query".

#### Populating the raw query

Next, the raw query parsed from the XML file is populated (i.e. the `${...}` is replaced with the appropriate value(s)) from the parameter passed as an argument to the called function.

#### Executing the SQL command

The SQL command then created, referred to as "populated query" is then executed through the database connection object

#### Populating the POJO

The result of SQL query is then either returned to the user (in terms of number of rows affected in case of `INSERT`, `UPDATE` and `DELETE` commands), or used to populate a Java object (in case of `SELECT` command).

### Usage of library

The library implements 5 functions that can be used to achieve the desired results. These are :-

- `selectOne()` Picks the record received from the SELECT query and returns an object populated with it.
- `selectMany()` Returns a `List<>` of objects populated with all the records from SELECT query.
- `insert()` Runs an INSERT query and returns number of rows affected.
- `update()` Runs an UPDATE query and returns number of rows affected.
- `delete()` Runs an DELETE query and returns number of rows affected.

> NOTE - `selectOne()` returns `null` if the query selects 0 records, and throws an exception if the query selects more than one record.

> **IMPORTANT - The POJOs to be populated using `selectOne()` and `selectMany()` must have default getters and setters**

To use these functions, user needs to define an object of class `org.cs305.assignment1.SqlExecutor`, and call these functions accordingly.

The constructor for `SqlExecutor` takes two commands :-

1. `pathToXMLFile` is a `String` which contains the **absolute** path to the XML file where SQL commands are stored.
2. `dbConnection` is a `java.sql.Connection` object which contains the database connection object.

### Types supported

The following types are supported to be provided as `queryParam`/`paramType`, with their corresponding `${...}` values to be specified in XML:-

1. null - ~~${...}~~ (i.e. no ${} should be present in the query)
2. Primitive types & their Wrappers - ${value}
3. String - ${value} 
4. Arrays (of type 2, 3) - ${value}
5. Collections (of type 2, 3) - ${value}
6. Objects (with fields of type 2 to 5) - ${_field_name_}
7. Date - ${value}
8. Arrays/Collections of objects (with overridden toString() method) - ${value}
9. Generic objects (with fields of type 2 to 5, or with overridden toString() methods) - ${_field_name_}

For fields 7, 8 and 9, the object types must have overridden toString() methods, like

```
@Override
public String toString() {
    //return the string format for this object
    //which will replace ${prop} in the raw SQL query
}
```

### Exceptions raised by the program

The program can raise several exceptions, and all will be of the type `java.lang.RuntimeException`, some of which are :-

- Passing a `null` object when paramType is not specified as "null"
- paramType and `queryParam` class mismatch (note that paramType must contain the Fully Qualified Name for the class)
- No query id match found
- No field found in `queryParam` corresponding to "${prop}" name
- No field found in POJO corresponding to column label
- Failure to cast from SQL query result type to POJO field type
- SQL query corresponding to `selectOne()` returning more than one record
- Exception(s) raised in making database connection statement
- Exception(s) raised while executing SQL commands


## How to compile and run this program

### Prerequisites

The library is built in java, using gradle, and hence JDK and gradle must be installed on the system.

Also, the built-in tests run on the MySQL Sakila database, hence to run those, MySQL is needed and [Sakila database](https://dev.mysql.com/doc/sakila/en/sakila-installation.html) must be loaded.

### Clone the repository

Clone the repository from GitHub to get the code, and then change branch to assignment_1 by executing the following commands :-

```
git clone https://github.com/tanuj-maheshwari/cs305_2022.git
git checkout assignment_1
```

### To run unit tests

To run unit tests provided within the implementation, 

1. Open [SqlExecutorTest](./lib/src/test/java/org/cs305/assignment1/SqlExecutorTest.java)

    - Change line 31 as follows :-

          dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila","<root username>","<password>");

        > Change 3306 to MySQL's port number (default is 3306)

    - Remove line 32 if changes are to be committed in the database

    - Change line 33 as follows :-

          sqlExecutor = new SqlExecutor("<Absolute path to XML file>", dbConnection);

2. Run the following command from the project directory (i.e. the directory where the repository is cloned) :-

       ./gradlew test

    > NOTE - If running on Windows, replace ./gradlew with gradlew

### To generate code coverage report using JaCoCo

Detailed code coverage report can be generated using JaCoCo. To generate the report, run :-

    ./gradlew jacocoTestReport

> NOTE - If running on Windows, replace ./gradlew with gradlew

The test report generated is located at `lib/build/reports/jacoco/test/html/index.html`

### To build a JAR

To build a JAR so that the library could be used in any project, run :-

    ./gradlew build

> NOTE - If running on Windows, replace ./gradlew with gradlew

The JAR file built will be located at `lib/build/libs/lib.jar`

## Snapshot of a sample run

### Test report through gradle

![Gradle Report](./reports/Gradle_Report.png?raw=true "Gradle Report")

### Coverage reports

The unit tests provided cover 100% percent of the code. Below is a snapshot of the code coverage report generated by IntelliJ and JaCoCo.

#### IntelliJ report

![IntelliJ Snapshot](./reports/IntelliJ_Report.png?raw=true "IntelliJ Snapshot")

#### JaCoCo report

![JaCoCo Snapshot](./reports/JaCoCo_Report.png?raw=true "JaCoCo Snapshot")
