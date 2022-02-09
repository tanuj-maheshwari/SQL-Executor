# SQL library

> Submitter name: Tanuj Maheshwari

> Roll No.: 2019CSB1125

> Course: CS305 (Software Engineering)

Java based library for executing SQL queries for CRUD operations against an RDBMS.


## What does this program do?

This is a Java library that can be used for executing basic SQL queries, i.e. `SELECT`, `INSERT`, `DELETE` and `UPDATE` (CRUD operations) against an RDMS such as MySQL, PostgreSQL, etc.

The SQL queries to be processed are grabbed from an XML file with a specific format. The queries can be complete on their own, or could be dynamically populated from user defined parameters at run time. The results from the SQL queries are used to populate plain old java objects (POJOs).


## A description of how this program works (i.e. its logic)

### Flow of logic

#### Parsing the XML file

First, the XML file is loaded from the path provided. The desired SQL query is then parsed from the XML file using the unique `id` parameter of each `<sql>` tag. The subsequent query recieved is termed as "raw query".

#### Populating the raw query

Next, the raw query parsed from the XML file is populated (i.e. the `${...}` is replaced with the appropriate value(s)) from the parameter passed as an argument to the called function.

#### Executing the SQL command

The SQL command then created, refered to as "populated query" is then executed through the database connection object

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

To use these functions, user needs to define an object of class `org.cs305.assignment1.SqlExecuter`, and call these functions accordingly.

The constructor for `SqlExecuter` takes two commands :-

1. `pathToXMLFile` is a `String` which contains the **absolute** path to the XML file where SQL commands are stored.
2. `dbConnection` is a `java.sql.Connection` object which contains the database connection object.

### Types supported

The following types are supported to be provided as `queryParam`/`paramType` :-

1. null
2. Primitive types & their Wrappers
3. String
4. Arrays (of type 2, 3)
5. Collections (of type 2, 3)
6. Objects (with fields of type 2 to 5)
7. Arrays/Collections of objects (with overridden toString() method)
8. Generic objects (with fields of type 2 to 5, or with overridden toString() methods)

For fields 7 and 8, the object types must have overridden toString() methods, like

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

The library is built in java, using gradle, and hence JDK (or atleast JRE) and gradle must be installed on the system.

Also, the built in tests run on the MySQL Sakila database, hence to run those, MySQL is needed and Sakila database must be loaded.

### Clone the repository

Clone the repository from GitHub to get the code, and then change branch to assignment_1 by executing the following commands :-

```
git clone https://github.com/tanuj-maheshwari/cs305_2022.git
git checkout assignment_1
```

### To run unit tests

To run unit tests provided within the implementation, run the fllowing command from the project directory (i.e. the directory where the repository is cloned) :-

```
./gradlew test
```

> NOTE - If running on Windows, replace ./gradlew with gradlew

### To generate code coverage report

Code coverage report generation is implemented using JaCoCo. To generate the report, run :-

```
./gradlew jacocoTestReport
```

The test report generated is located at lib/build/reports/jacoco/test/html/index.html

### To build a JAR

To build a JAR so that the library could be used in any project, run :-

```
./grdlew build
```

The JAR file built will be located at lib/build/libs/lib.jar

## Snapshot of a sample run

The unit tests provided cover 99% percent of the code. Below is a sanpshot of the code coverage report generated by jacoco. The complete coverage report generated by JaCoCo can be found [here](./report/jacoco/test/html/index.html).

![JaCoCo Snapshot](./report/Coverage.png?raw=true "JaCoCo Snapshot")
