/**
 * adam cunningham csc460 prog3
 * creates some empty tables
 */
import java.io.*;
import java.sql.*;                 // For access to the SQL interaction methods

class MakeTables{
    public static void main(String[] args){
        Connection dbconn = null;
        if (args.length < 1){
            System.out.println("when executing use the option 'create' or 'drop'");
            System.exit(0);
        }else {
            dbconn = getConnection();
        }

        if (args[0].equals("create")){
            makeTable("twelve", dbconn);
            makeTable("fourteen", dbconn);
            makeTable("sixteen", dbconn);
            makeTable("eighteen", dbconn);
        }
        else if (args[0].equals("drop")){
            dropTable("twelve", dbconn);
            dropTable("fourteen", dbconn);
            dropTable("sixteen", dbconn);
            dropTable("eighteen", dbconn);
        } else{
            System.out.println("when executing use the option 'create' or 'drop'");
            System.exit(0);
        }


        try {
            dbconn.close();
        } catch (SQLException e) {
            System.out.println("could not close db connection");
        }
    }

    
    /**makeTable
     * 
     * drops a previous table laser.year
     * and creates a new empty table named laser.year
     * where year is replaced with the String argument
     * @param year, and dbconn, a String representing the table title extension
     * (MUST BE NON-DECIMAL) ie "twelve", dbconn is a connection to the oracle database
     */
    public static void makeTable(String year, Connection dbconn){

        String table =       // our test query
        //
            "CREATE TABLE laser." + year +" (" +
                "state              VARCHAR2(255), " +
                "jurisdiction_type  VARCHAR2(255), " +
                "county_name        VARCHAR2(255), " +
                "municipality       VARCHAR2(255), " +
                "precinct_name      VARCHAR2(255), " +
                "precinct_id        VARCHAR2(255), " +    
                "name               VARCHAR2(255), " +
                "address            VARCHAR2(255)"  +
            ")";

        Statement stmt = null;

        try {

            stmt = dbconn.createStatement();
            stmt.executeUpdate(table);

            // Shut down the table creation command
            stmt.close();  

            stmt = dbconn.createStatement();
            stmt.execute("GRANT SELECT on laser."+year+" to PUBLIC");
            stmt.close();

        } catch (SQLException e) {

                System.err.println("*** SQLException:  "
                    + "Could not create table");
                System.err.println("\tMessage:   " + e.getMessage());
                System.err.println("\tSQLState:  " + e.getSQLState());
                System.err.println("\tErrorCode: " + e.getErrorCode());
                System.exit(-1);

        }
    }

    public static void dropTable(String year, Connection dbconn){
        String drop  = "DROP TABLE laser." + year;
        Statement stmt = null;
        try {

            stmt = dbconn.createStatement();
            stmt.executeUpdate(drop);

            // Shut down the table creation command
            stmt.close();  

        } catch (SQLException e) {

                System.err.println("*** SQLException:  "
                    + "drop table");
                System.err.println("\tMessage:   " + e.getMessage());
                System.err.println("\tSQLState:  " + e.getSQLState());
                System.err.println("\tErrorCode: " + e.getErrorCode());
                System.exit(-1);

        }
    }

    /**getConnection
     * 
     * establish a connection with the oracle database
     * code taken from the provided JDBC.java
     * @return Connection, a database connection to the oracle database
     * @throws ClassNotFoundException probably means you need to add 
     * the Oracle JDBC driver to your CLASSPATH environment variable:
     * export CLASSPATH=/opt/oracle/product/10.2.0/client/jdbc/lib/ojdbc14.jar:${CLASSPATH}
     * 
     * also throws SQLExeption, probably because URL, password, or username is wrong
     */
    public static Connection getConnection(){

        String oracleURL =   // Magic lectura -> aloe access spell
        "jdbc:oracle:thin:@aloe.cs.arizona.edu:1521:oracle";

        String username = "laser",    // Oracle DBMS username
               password = "a2423";    // Oracle DBMS password

        // load the (Oracle) JDBC driver by initializing its base
        // class, 'oracle.jdbc.OracleDriver'.

        try {
            Class.forName("oracle.jdbc.OracleDriver");

        } catch (ClassNotFoundException e) {

            System.err.println("*** ClassNotFoundException:  "
                + "Error loading Oracle JDBC driver.  \n"
                + "\tPerhaps the driver is not on the Classpath?");
            System.exit(-1);
        }

        // make and return a database connection to the user's
        // Oracle database

        Connection dbconn = null;

        try {
                dbconn = DriverManager.getConnection
                                (oracleURL,username,password);

        } catch (SQLException e) {

                System.err.println("*** SQLException:  "
                    + "Could not open JDBC connection.");
                System.err.println("\tMessage:   " + e.getMessage());
                System.err.println("\tSQLState:  " + e.getSQLState());
                System.err.println("\tErrorCode: " + e.getErrorCode());
                System.exit(-1);

        }

        return dbconn;
    }
}