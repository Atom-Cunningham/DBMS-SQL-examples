
/**
 * adam cunningham csc460 prog3
 * populates tables with precinct data
 */
import java.io.*;
import java.util.Scanner;
import java.sql.*;                 // For access to the SQL interaction methods

class Populate {
    public static void main(String[] args){
        Connection dbconn = getConnection();
        populateTable("12", "twelve", dbconn);
        populateTable("14", "fourteen", dbconn);
        populateTable("16", "sixteen", dbconn);
        populateTable("18", "eighteen", dbconn);

        try {
            dbconn.close();
        } catch (SQLException e) {
            System.out.println("could not close db connection");
        }
    }


    /**populateTable
     * opens a csv file at a path in the public directory for csc460 fall20
     * parses the file, and prints the first four lines
     * 
     * @param year string, as two decimals denoting the last two numbers
     * for the year 20xx where xx is replaced by the passed string
     * @param label the name of the table into which the data will be inserted
     * @param dbconn a Connection object in communication with the server
     * @return void
     */
    public static void populateTable(String year, String label, Connection dbconn){

        //open the file and get scanner
        Scanner scanner = getScanner(year);

        Statement stmt = null;
        System.out.println("\n\nresults for "+year);
        System.out.println("___________________________________");
        String line = scanner.nextLine();

        while (scanner.hasNext()){
            line = scanner.nextLine();
            
            //construct sql insert statment
            String sql = "INSERT INTO laser."+label+" VALUES (";
            //split on commas iff that comma is followed by an even number of quotes
            line = line.toUpperCase();
            line = line.replaceAll("'", "''");
            String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            //construct value list
            sql += "'" + fields[0] + "'";
            for (int idx = 1; idx < fields.length; idx++){
                sql += "," + "'" + fields[idx] + "'";
            }
            sql += ")";

            try {
                stmt = dbconn.createStatement();
                stmt.execute(sql);
                // Shut down the statement creation command
                stmt.close();  
    
            } catch (SQLException e) {
                    System.err.println("*** SQLException:  "
                        + "Could not enter relation into table " + label);
                    System.err.println("for line:\n" + line);
                    System.err.println("\tMessage:   " + e.getMessage());
                    System.err.println("\tSQLState:  " + e.getSQLState());
                    System.err.println("\tErrorCode: " + e.getErrorCode());
                    System.exit(-1);
    
            }
        }

        scanner.close();
    }

    /**getScanner
     * opens a file at a path for the monatana polling places csv file for a given year
     * creates a scanner object, and returns that object
     * 
     * @param year the last two digits of a year as a string
     * @return a scanner object reading the file for the polling places in montana for that year
     * @throws FileNotFoundException

     */
    public static Scanner getScanner(String year){
        String path = "/home/cs460/fall20/mtpollingplaces20"+year+".csv";
        Scanner scanner = null;
        try {
            File file = new File(path);
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.out.println("could not find file for the year 20" + year);
        }
        return scanner;
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