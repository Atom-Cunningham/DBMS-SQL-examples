/**Adam Cunningham
 * csc460 program 4, Dr. McCann, due 12/8/20
 * This program must be run before Prog4.class is run
 * 
 * populates member, product, sale, subsale, supplier, supply
 * tables.
 * 
 * pre-condition. tables must already be created
 * 
 * uses a list of csv file directories containing data
 * for a shopping mall to populate tables in JDBC
 * 
 * To run this, first arg should be the username
 * for the oracle.aloe database
 * 
 * the second arg should be the password
 * 
 */
import java.sql.*;
import java.io.File;
import java.util.Scanner;
import java.io.IOException;
import java.util.NoSuchElementException;

public class Populator {
    public static void main(String args[]){

        Connection dbconn = getConnection(args);
        String[] fileNames = {"Sale.csv", "Subsale.csv", "Employee.csv",
                             "Member.csv","Product.csv",
                              "Supplier.csv","Supply.csv"};

        try {
            //insert data from csv files into oracle database
            for(String fileName : fileNames){
                populateTable(fileName, dbconn);
            }

        } catch (SQLException e) {
            System.out.println("an error occured inserting into database");
            System.err.println("\tMessage:   " + e.getMessage());
                	System.err.println("\tSQLState:  " + e.getSQLState());
                	System.err.println("\tErrorCode: " + e.getErrorCode());
                	System.exit(-1);
        }
    }

    /**populateTable
     * iterates through a file, creating an sql statement for each line
     * then inserts a row into the database using the sql based on the line
     * @param fileName the name of the file to be opened, must be spelled the same as the table's name
     * @param dbconn a connection the oracle database
     * @throws SQLException throws if the insert statement cannot be executed
     */
    public static void populateTable(String fileName, Connection dbconn) throws SQLException{
        File file = getFile(fileName);
        Scanner scanner = getScanner(file);

        //advance past first token
        try{ scanner.nextLine(); } catch(NoSuchElementException e) {
            System.out.println("file is empty");}
            
        String[] line;
        String tableName = fileName.toLowerCase().replace(".csv", "");
        String insertStatement;
        //for lines of fields in file:
        while(scanner.hasNext()){
            line = scanner.nextLine().split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"); //get fields
            /*
            if (tableName.equals("subsale")){                                   //special case, update sale
                updateSaleTable(line, dbconn);
            }*/
            insertStatement = createSQLStatement(tableName, line);              //convert to sql
            Statement stmt = dbconn.createStatement();                          //insert into db
            stmt.execute(insertStatement);
            stmt.close();
            System.out.println(insertStatement);
        }
    }

    /**updateSaleTable
     * takes an array of strings representing subsale fields
     * creates a new sale row in the database if no sale matches the sale id
     * associated with the subsale
     * updates the sale to reflect the new subsales saleprice being added to the sale's totalprice
     * @param line a line from the file, split on commas
     * @param dbconn a connection to the oracle database
     */
    public static void updateSaleTable(String[] line, Connection dbconn){
       
        String saleID = line[1];    //get subsale's saleID
        String salePrice = line[3]; //get the price of the sale
        String amount = line[4];    //get the amount

        //TODO randomize payment methods & dates if we care

        //sql
        //insert if does not exist SQL statement
        String insertIfDNE = "IF NOT EXISTS(SELECT * FROM sale WHERE id = " + saleID +")" +
                             "begin"+
                             "INSERT INTO sale "+
                             "(saleID, saleDate, paymentMethod, totalPrice) " +
                             "VALUES ('" + saleID + "','11/24/2020', 'Cash', '0.0') " +
                             "end";

        //update the totalPrice with salePrice * amount
        try {
            //insert if does not exist
            Statement stmt = dbconn.createStatement();
            stmt.execute(insertIfDNE);
            stmt.close();

            //calculate new total price
            stmt = dbconn.createStatement();
            ResultSet totalPrice = stmt.executeQuery("SELECT totalPrice FROM sale "+
                                                     "WHERE id = " + saleID);
            totalPrice.next();
            Double currTotalPrice = Double.parseDouble(totalPrice.getString("totalPrice"));
            Double newTotalPrice = currTotalPrice + Double.parseDouble(salePrice) * Integer.parseInt(amount);
            stmt.close();

            //update with new total price
            stmt = dbconn.createStatement();
            stmt.execute("UPDATE sale " +
                         "SET totalPrice = " + newTotalPrice +
                         "WHERE id = " +saleID);
            stmt.close();

        } catch (SQLException e) {
            System.out.println("could not properly update sale table");
            System.err.println("\tMessage:   " + e.getMessage());
                	System.err.println("\tSQLState:  " + e.getSQLState());
                	System.err.println("\tErrorCode: " + e.getErrorCode());
                	System.exit(-1);
        }
        
    }
    
    /**createSQLstatement
     * creates an sql statement based on the list of strings
     * @param tableName the name of the table to insert into
     * @param line  a list of Strings as fields
     * @return the insert statement, constructed from the list of strings
     */
    public static String createSQLStatement(String tableName, String[] line){
        String insertStatement = "INSERT INTO " + tableName + " VALUES(";
        //add each field except the last
        for(int i = 0; i < line.length-1; i++){
	    String value = line[i].trim();
	    if(value.isEmpty()){
		value = " ";
	    }
            insertStatement += "'" + value +"',";
        }
        //add the last field and the ending
        insertStatement += "'" + line[line.length-1] + "')";
        return insertStatement;
    }

    /**getConnection
     * gets a connection to the oracle db
     * @param args a string of arguments passed from the terminal to main
     * should be username and password
     * @return a connection to the oracle db
     */
    public static Connection getConnection(String args[]){
        Connection dbconn = null;
        String username = null;
        String password = null;
        try {
            username = args[0];
            password = args[1];
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Could not run program."+
            "\nFirst argument should be your username, second should be your password");
            System.exit(-1);
        }
        
		String oracleURL = "jdbc:oracle:thin:@aloe.cs.arizona."+
        "edu:1521:oracle";

        try{
            Class.forName("oracle.jdbc.OracleDriver");
        }catch(ClassNotFoundException e){
            System.out.println("Couln't find the oracle driver");
            System.exit(-1);
        }

        try{
            dbconn = DriverManager.getConnection(oracleURL,
                username, password);
        }catch(SQLException e){
            System.out.println("Could not connect to the db.");
                        System.err.println("\tMessage:   " + e.getMessage());
                    System.err.println("\tSQLState:  " + e.getSQLState());
                    System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);
        }
        return dbconn;
    }

    /**getFile
	 * opens a file at a given filePath, checking to ensure that the file was found
	 * @param filePath the path to the file to be opened
	 * @return a valid file, that should never be null
	 */
	public static File getFile(String filePath){
		//Open file
		File file = null;
		try {
			file = new File(filePath); 
		} catch (NullPointerException e) {
			System.out.println("could not find file at: " + filePath);
		}
		return file;
    }
    
    /**getScanner
     * gets a scanner to read a file
     * @param file a valid, non-null File object
     * @return a scanner to read the file
     */
    public static Scanner getScanner(File file){
        Scanner reader = null;
		try {	
			reader = new Scanner(file);
		} catch (IOException e) {
			System.out.println("ERROR: Could not read file" + file.getName());
        }
        return reader;
    }
}
