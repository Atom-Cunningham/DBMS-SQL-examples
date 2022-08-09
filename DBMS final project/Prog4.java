/**Prog4
 * 
 * Adam Cunningham, Moses Maugans, Christopher Crabtree, Julien Galons
 * csc 460 Database design, Prog4, Dr. McCann & Zheng, due Oct 8
 * 
 * 
 * This program prompts the user for a query option, 1 through 5
 * If necessary the program asks for follow up input to clarify the query
 * 
 * To accomplish this, the program uses a combination of SQL and java
 * to contact laser@oracle.aloe, and retrieve tables containing data
 * about a shopping mall, including:
 * its members, employees, sales, suppliers, and products
 * 
 * 
 * Using the tables returned from the database via sql requests, this program
 * displays information to the user, and allows the user to make changes to the db
 * 
 * functionalities include:
 * 1) 	Adding, updating or deleting a member, employee, product, supplier,
 * 		supply record, sale record, or subsale record.
 * 2)	Displaying a member's information by searching their phone number or their ID
 * 3)	Checking the which product is the most profitable.
 * 4)	Displaying a member's lifetime savings.
 * 
 * all Relation creation and population is handled by DatabaseCreator.java
 * and by DatabasePopulator.java
 * 
 * The data itself is taken from the test cases in the csv files in our repository
 * Member, Product, Employee, Sales, Supplier, Supply
 * 
 * compile with java 10
 * pre-condition, database must be populated using Populator.class
 * which can be found in the DataGeneration subdirectory
 * 
 * When running:
 * first argument is your oracledb username
 * second argument is your oracledb password
 * 
 * TODO: KNOWN ISSUES
 * not tested yet
 */
import java.sql.*;
import java.util.*;

//populates all tables in our database
class Prog4 {
	/**MAIN
	 * creates a connection to the database
	 * runs a loop which interfaces with the user
	 * and prompts them for queries. 
	 * @param args the array of strings following the command to run in the terminal
	 * args[0] should be the oracledb username, args[1] should be the password 
	 */
	public static void main(String args[]){
		Connection dbconn = getConnection(args);

		Scanner userin = new Scanner(System.in);
		printMenuOptions();
		while(true){
			System.out.println("type 'options' to see the options again");
            	System.out.print(":>>>");
            	switch (userin.nextLine()){
				case "1":
					//NESTED CASE SWITCH MOVED TO UI SECTION OF CODE
					//TODO ADD COMMENTS
					addUpdateRemove(userin, dbconn);
					break;
				case "2":
						//Add or update sale records and sub sale records
						addUpdate(userin, dbconn);
					break;
				case "3":
					//displays the member by searching the phone number or the ID
					getMember(userin, dbconn);	//requires additional I/O
					break;
				case "4":
					//Check the current the most profitable product over all 
					//the products in the database
					System.out.println(getMostProfitableProduct(dbconn));
					break;
				case "5":
		    			//Display the total savings for some member
		    			getMemberSavings(userin, dbconn);
		    			break;
	        	case "options":
		    			//redisplay the menu options
		    			printMenuOptions();
		    			break;
                case "exit":
                    	exitProgram(userin, dbconn);
                default:
		    			System.out.println("\n-\t!!!\nnot a valid option\n-\t!!!" +
		    				"\ntype 'options' to see the menu again");
                    	break;
            		} 
		}
	}

	/*---------------------------------------------------------------------
        |  Method addMember
        |
        |  Purpose:  Adds a new member row to the member table. The user will
	|	be prompted to enter info for the following attributes:
	|	firstname, lastname, dob, address, phoneNumber, and activate.
	|	The first name, lastname, and phone number may not be left
	|	blank.
        |
        |  Pre-condition:  The Connection must be open and the member table
	|	must exist.
        |
        |  Post-condition: A new row will be added to the member table.
        |
        |  Parameters:
        |      dbconn -- Connection object used to communicate with oracle
	|      userIn -- Scanner used to read in user input
        |
        |  Returns:  None.
        *-------------------------------------------------------------------*/
	public static void addMember(Connection dbconn, Scanner userIn){
		System.out.print("First Name: ");//get firstname attribute from user
		String fname = userIn.nextLine();
		while(fname.equals("")){//there must be something entered by user
			System.out.println("\nYou must provide a first name.");
			fname = userIn.nextLine();
		}

		System.out.print("Last Name: ");
		String lname = userIn.nextLine();
		while(lname.equals("")){//again this is not an optional feild
			System.out.println("\nYou must provide a last name.");
			lname = userIn.nextLine();
		}
		System.out.println();

		System.out.print("Date of Birth(MM/DD/YYYY): ");
		String dob = userIn.nextLine();
		System.out.println();

		System.out.print("Address: ");
		String address = userIn.nextLine();
		System.out.println();

		System.out.print("Phone Number: ");
		String phoneNumber = userIn.nextLine();
		while(phoneNumber.equals("")){//can't be blank
			System.out.print("\nYou must provide a phone number. ");
			fname = userIn.nextLine();
		}
		System.out.println();

		System.out.print("Would you like to activate your account(Y/N): ");
		String stractive = userIn.nextLine().toUpperCase();
		while(!stractive.equals("Y") && !stractive.equals("N")){
			System.out.println("\nYou must enter a valid option(Y/N).");
			stractive = userIn.nextLine();
		}
		int active = 0;//convert user answer to a boolean value
		if(stractive.equals("Y")){
			active = 1;
		}

		//the String query to send to the dbms
		String query = "INSERT INTO member(firstName, lastName, dob, address, phoneNumber, " + 
			"rewardPoints, active)" +
			"VALUES('" + fname + "', '" + lname + "', '" + dob + "', '" + address + "', '" + 
			phoneNumber + "', '0', '" + active + "')";
		try {
			Statement stmt = dbconn.createStatement();
			stmt.executeQuery(query);
			stmt.close();
		} catch (SQLException e) {
			System.err.println("*** SQLException:  "
                		+ "error adding new member into member table.");
			System.err.println("\tMessage:   " + e.getMessage());
            		System.err.println("\tSQLState:  " + e.getSQLState());
            		System.err.println("\tErrorCode: " + e.getErrorCode());
            		System.exit(-1);
		}
	}

	/*---------------------------------------------------------------------
        |  Method updateMember
        |
        |  Purpose:  Allows a user to update the value of any column except
	|	for the id. 
        |
        |  Pre-condition:  The Connection must be open and the member table
	|	must exist.
        |
        |  Post-condition: The value of one of the columns of the rows in
	|	the member table will be updated.
        |
        |  Parameters:
        |      dbconn -- Connection object used to communicate with oracle
	|      userIn -- Scanner used to read in user input
        |
        |  Returns:  None.
        *-------------------------------------------------------------------*/
	public static void updateMember(Connection dbconn, Scanner userIn){
		System.out.print("What is the id of the member you wish to update? ");
		String id = userIn.nextLine();
		System.out.println();

		System.out.print("What field would you like to update?(firstName, " +
			"lastName, dob, address, phoneNumber, rewardPoints, or active) ");
		String field = userIn.nextLine().toLowerCase();

		System.out.print("What should the new value of " + field + " be? ");
		String value = userIn.nextLine();
		System.out.println();
		
		updateField(dbconn, "member", id, field, value);
	}


	/*---------------------------------------------------------------------
        |  Method addEmployee
        |
        |  Purpose:  Adds a new employee row to the employee table. The user will
	|	be prompted to enter info for the following attributes:
	|	firstname, lastname, gender, address, phoneNumber, and groupType.
	|	Only the address line may be left blank
        |
        |  Pre-condition:  The Connection must be open and the employee table
	|	must exist.
        |
        |  Post-condition: A new row will be added to the employee table.
        |
        |  Parameters:
        |      dbconn -- Connection object used to communicate with oracle
	|      userIn -- Scanner used to read in user input
        |
        |  Returns:  None.
        *-------------------------------------------------------------------*/
	public static void addEmployee(Connection dbconn, Scanner userIn){
		System.out.print("First Name: ");//get firstname attribute from user
		String fname = userIn.nextLine();
		while(fname.equals("")){//there must be something entered by user
			System.out.print("\nYou must provide a first name.");
			fname = userIn.nextLine();
		}

		System.out.print("Last Name: ");
		String lname = userIn.nextLine();
		while(lname.equals("")){//again this is not an optional feild
			System.out.print("\nYou must provide a last name.");
			lname = userIn.nextLine();
		}

		System.out.print("Gender: ");
		String gender = userIn.nextLine();
		while(lname.equals("")){
			System.out.print("\nYou must enter something for this field.");
			gender = userIn.nextLine();
		}
		System.out.println();

		System.out.print("Address: ");
		String address = userIn.nextLine();
		System.out.println();	

		System.out.print("Phone Number: ");
		String phoneNumber = userIn.nextLine();
		while(phoneNumber.equals("")){//can't be blank
			System.out.print("\nYou must provide a phone number. ");
			fname = userIn.nextLine();
		}
		System.out.println();

		System.out.print("Group Type(stock, sales, manager): ");
		String groupType = userIn.nextLine().toLowerCase();
		while(!groupType.equals("stock") && !groupType.equals("sales") &&
			!groupType.equals("manager")){
			System.out.print("\nGroup type must be 'stock', 'sales', or 'manager'");
			groupType = userIn.nextLine();
		}
		System.out.println();

		int salary = 0;
		if(groupType.equals("stock")){
			salary = 22000;
		}else if(groupType.equals("sales")){
			salary = 34000;
		}else if(groupType.equals("manager")){
			salary = 43000;
		}

		String query = "INSERT INTO employee(firstName, lastName, gender, address, " +
			"phoneNumber, groupType, salary) " + 
			"VALUES('" + fname + "', '" + lname + "', '" + gender + "', '" + 
			address + "', '" + phoneNumber + "', '" + groupType + "', '" + salary + "')";
		try {
			Statement stmt = dbconn.createStatement();
			stmt.executeQuery(query);
			stmt.close();
		} catch (SQLException e) {
			System.err.println("*** SQLException:  "
                		+ "error adding new employee into employee table.");
			System.err.println("\tMessage:   " + e.getMessage());
            		System.err.println("\tSQLState:  " + e.getSQLState());
            		System.err.println("\tErrorCode: " + e.getErrorCode());
            		System.exit(-1);
		}

		System.out.println("Successfully added to employee table.");
	}

	/*---------------------------------------------------------------------
        |  Method updateEmployee
        |
        |  Purpose:  Allows a user to update the value of any column except
	|	for the id. 
        |
        |  Pre-condition:  The Connection must be open and the employee table
	|	must exist.
        |
        |  Post-condition: The value of one of the columns of the rows in
	|	the employee table will be updated.
        |
        |  Parameters:
        |      dbconn -- Connection object used to communicate with oracle
	|      userIn -- Scanner used to read in user input
        |
        |  Returns:  None.
        *-------------------------------------------------------------------*/
	public static void updateEmployee(Connection dbconn, Scanner userIn){
		System.out.print("What is the id of the employee you wish to update? ");
		String id = userIn.nextLine();
		System.out.println();

		System.out.print("What field would you like to update?(firstName, " +
			"lastName, gender, address, phoneNumber, groupType, or salary) ");
		String field = userIn.nextLine().toLowerCase();

		System.out.print("What should the new value of " + field + " be? ");
		String value = userIn.nextLine();
		System.out.println();

		updateField(dbconn, "employee", id, field, value);
	}

	/*---------------------------------------------------------------------
        |  Method addProduct
        |
        |  Purpose:  Adds a new product row to the product table. The user will
	|	be prompted to enter info for the following attributes: name, 
	|	retailPrice, category, membershipDiscount, and quantity.
	|	All lines except for the name line may be left blank. If a product
	|	is added with type attribute that is not already in the category
	|	table then that type will also be added to the category table.
        |
        |  Pre-condition:  The Connection must be open and the product table
	|	must exist.
        |
        |  Post-condition: A new row will be added to the product table.
        |
        |  Parameters:
        |      dbconn -- Connection object used to communicate with oracle
	|      userIn -- Scanner used to read in user input
        |
        |  Returns:  None.
        *-------------------------------------------------------------------*/
	public static void addProduct(Connection dbconn, Scanner userIn){
		System.out.print("Product Name: ");//get product name attribute from user
		String name = userIn.nextLine();
		while(name.equals("")){//there must be something entered by user
			System.out.print("\nYou must provide a product name.");
			name = userIn.nextLine();
		}

		System.out.print("Retail Price: ");
		double price = Double.parseDouble(userIn.nextLine());
		System.out.println();

		System.out.print("Category: ");
		String category = userIn.nextLine();
		System.out.println();

		System.out.print("Membership Discount(in dollars): ");
		double discount = Double.parseDouble(userIn.nextLine());
		System.out.println();

		System.out.print("Quantity: ");
		int quantity = Integer.parseInt(userIn.nextLine());
		System.out.println();
		
		int id = generateUniqueId(dbconn, "product");
		String query = "INSERT INTO product(id, name, retailPrice, category, membershipDiscount, quantity)" +
			"VALUES('" + id + "', '" + name + "', '" + price + "', '" + category + "', '" + discount + 
			"', '" + quantity + "')";
		try {
			Statement stmt = dbconn.createStatement();
			stmt.executeQuery(query);
			stmt.close();
		} catch (SQLException e) {
			System.err.println("*** SQLException:  "
                		+ "error adding new product into product table.");
			System.err.println("\tMessage:   " + e.getMessage());
            		System.err.println("\tSQLState:  " + e.getSQLState());
            		System.err.println("\tErrorCode: " + e.getErrorCode());
            		System.exit(-1);
		}
	}

	/*---------------------------------------------------------------------
        |  Method generateUniqueId
        |
        |  Purpose:  Generates a new id for a record being added to the product,
	|	sale, or subsale tables. The id is generated by finding the record
	|	with the largest id in the tables and incrementing it ensuring that
	| 	there are no repeat record id's in the tables.
        |
        |  Pre-condition:  The Connection must be open and the product, sale, and
	|	subsale tables must exist.
        |
        |  Post-condition: A unique product id will be created.
        |
        |  Parameters:
        |      dbconn -- Connection object used to communicate with oracle
        |
        |  Returns:  The int value of the id we generated.
        *-------------------------------------------------------------------*/
	public static int generateUniqueId(Connection dbconn, String table){
		String query = "SELECT id FROM " + table + " ORDER BY id DESC";

		int largestId = 0;
		try {
			Statement stmt = dbconn.createStatement();
			ResultSet results = stmt.executeQuery(query);
			results.next();
			largestId = results.getInt(1);
			stmt.close();
		} catch (SQLException e) {
			System.err.println("*** SQLException:  "
                		+ "error selecting contents of category table.");
			System.err.println("\tMessage:   " + e.getMessage());
            		System.err.println("\tSQLState:  " + e.getSQLState());
            		System.err.println("\tErrorCode: " + e.getErrorCode());
            		System.exit(-1);
		}
		largestId++;
		return largestId;	
	}

	/*---------------------------------------------------------------------
        |  Method updateProduct
        |
        |  Purpose:  Collects information from the user that will be used to update
	|	an attribute of a single row in the product table.
        |
        |  Pre-condition:  The Connection must be open and the product table
	|	must exist.
        |
        |  Post-condition: The value of one of the columns of the rows in
	|	the product table will be updated.
        |
        |  Parameters:
        |      dbconn -- Connection object used to communicate with oracle
	|      userIn -- Scanner used to read in user input
        |
        |  Returns:  None.
        *-------------------------------------------------------------------*/
	public static void updateProduct(Connection dbconn, Scanner userIn){
		System.out.print("What is the id of the product you wish to update? ");
		String id = userIn.nextLine();
		System.out.println();

		System.out.print("What field would you like to update?(name, retailPrice, " +
			"category, membershipDiscount, quantity) ");
		String field = userIn.nextLine().toLowerCase();

		System.out.print("What should the new value of " + field + " be? ");
		String value = userIn.nextLine();
		System.out.println();

		//the row will now be updated
		updateField(dbconn, "product", id, field, value);
	}

	/*---------------------------------------------------------------------
        |  Method addSupplier
        |
        |  Purpose:  Adds a new supplier row to the supplier table. The user will
	|	be prompted to enter info for the following attributes: name, 
	|	address, and contact name.
	|	Both the name and contact name attribute must have something entered
	|	for their value.
        |
        |  Pre-condition:  The Connection must be open and the supplier table
	|	must exist.
        |
        |  Post-condition: A new row will be added to the supplier table.
        |
        |  Parameters:
        |      dbconn -- Connection object used to communicate with oracle
	|      userIn -- Scanner used to read in user input
        |
        |  Returns:  None.
        *-------------------------------------------------------------------*/
	public static void addSupplier(Connection dbconn, Scanner userIn){
		System.out.print("Supplier Name: ");//get supplier name attribute from user
		String name = userIn.nextLine();
		while(name.equals("")){//there must be something entered by user
			System.out.print("\nYou must provide a supplier name.");
			name = userIn.nextLine();
		}

		System.out.print("Address: ");
		String address = userIn.nextLine();
		System.out.println();

		System.out.print("Contact Name: ");
		String contactName = userIn.nextLine();
		while(name.equals("")){//there must be something entered by user
			System.out.print("\nYou must provide a contact name.");
			name = userIn.nextLine();
		}

		String query = "INSERT INTO supplier(name, address, contactName)"+
			"VALUES('" + name + "', '" + address + "', '" + contactName + "')";
		try {
			Statement stmt = dbconn.createStatement();
			stmt.executeQuery(query);
			stmt.close();
		} catch (SQLException e) {
			System.err.println("*** SQLException:  "
                		+ "error adding new supplier into supplier table.");
			System.err.println("\tMessage:   " + e.getMessage());
            		System.err.println("\tSQLState:  " + e.getSQLState());
            		System.err.println("\tErrorCode: " + e.getErrorCode());
            		System.exit(-1);
		}
	}

	/*---------------------------------------------------------------------
        |  Method updateSupplier
        |
        |  Purpose:  Collects information from the user that will be used to update
	|	an attribute of a single row in the supplier table.
        |
        |  Pre-condition:  The Connection must be open and the supplier table
	|	must exist.
        |
        |  Post-condition: The value of one of the columns of the rows in
	|	the supplier table will be updated.
        |
        |  Parameters:
        |      dbconn -- Connection object used to communicate with oracle
	|      userIn -- Scanner used to read in user input
        |
        |  Returns:  None.
        *-------------------------------------------------------------------*/
	public static void updateSupplier(Connection dbconn, Scanner userIn){
		System.out.print("What is the id of the supplier you wish to update? ");
		String id = userIn.nextLine();
		System.out.println();

		System.out.print("What field would you like to update?(name, address, contactName)");
		String field = userIn.nextLine().toLowerCase();

		System.out.print("What should the new value of " + field + " be? ");
		String value = userIn.nextLine();
		System.out.println();

		//the row will now be updated
		updateField(dbconn, "supplier", id, field, value);
	}

	/*---------------------------------------------------------------------
        |  Method deleteRow
        |
        |  Purpose:  From a table specified by the user, deletes an entire row
	|	with the same id as the one given by the user. This method can
	|	only delete from the member, employee, product, and supplier tables.
        |
        |  Pre-condition:  The Connection must be open and the member, employee, product,
	|	and supplier tables must exist.
        |
        |  Post-condition: A row will be delete from either the member, employee, product,
	|	or supplier tables.
        |
        |  Parameters:
        |      dbconn -- Connection object used to communicate with oracle
	|      userIn -- Scanner used to read in user input
        |
        |  Returns:  None.
        *-------------------------------------------------------------------*/
	public static void deleteRow(Connection dbconn, Scanner userIn){
		System.out.print("Which table would you like to delete from(member, employee, " +
			"product, or supplier): ");
		String table = userIn.nextLine().toLowerCase();
		while(!table.equals("member") && !table.equals("employee") && !table.equals("product")
			&& !table.equals("supplier")){
			System.out.print("Can only delete from the tables: member, employee, product, " +
				"or supplier.)");
			table = userIn.nextLine();
		}

		System.out.print("What is the id of the row you want to drop? ");
		int id = Integer.parseInt(userIn.nextLine());

		String query = "DELETE FROM " + table + " WHERE id='" + id + "'";
		try {
			Statement stmt = dbconn.createStatement();
			stmt.executeQuery(query);
			stmt.close();
		} catch (SQLException e) {
			System.err.println("*** SQLException:  "
                		+ "error adding new member into member table.");
			System.err.println("\tMessage:   " + e.getMessage());
            		System.err.println("\tSQLState:  " + e.getSQLState());
            		System.err.println("\tErrorCode: " + e.getErrorCode());
            		System.exit(-1);
		}
	}

	/*---------------------------------------------------------------------
        |  Method updateField
        |
        |  Purpose:  For a given row in the member, employee, product, or supplier
	|	tables a single field is updated with the given value.
        |
        |  Pre-condition:  The Connection must be open and the member, employee, 
	|	product, and supplier tables must exist.
        |
        |  Post-condition: A single attribute in one of the tables will be updated.
        |
        |  Parameters:
        |      dbconn -- Connection object used to communicate with oracle
	|      table  -- the table we wish to update a record in
	|      id     -- the id of the row we wish to update
	|      field  -- the name of the column we want to update
	|      value  -- the new value to put in the column
        |
        |  Returns:  None.
        *-------------------------------------------------------------------*/
	public static void updateField(Connection dbconn, String table, String id, String field, String value){
		String query = "UPDATE " + table + " SET " + field + "='" + value + "' WHERE id='" + id + "'";

		try {
			Statement stmt = dbconn.createStatement();
			stmt.executeQuery(query);
			stmt.close();
		} catch (SQLException e) {
			System.err.println("*** SQLException:  "
                		+ "error updating member row.");
			System.err.println("\tMessage:   " + e.getMessage());
            		System.err.println("\tSQLState:  " + e.getSQLState());
            		System.err.println("\tErrorCode: " + e.getErrorCode());
            		System.exit(-1);
		}
	}

	/*---------------------------------------------------------------------
        |  Method addSale
        |
        |  Purpose:  Adds a new sale row to the sale table. The user will
	|	be prompted to enter info for the following attributes: date, 
	|	paymentMethod, and memberId. The total price will be initialized
	|	to 0.00.
        |
        |  Pre-condition:  The Connection must be open and the sale table
	|	must exist.
        |
        |  Post-condition: A new row will be added to the sale table.
        |
        |  Parameters:
        |      dbconn -- Connection object used to communicate with oracle
	|      userIn -- Scanner used to read in user input
        |
        |  Returns:  None.
        *-------------------------------------------------------------------*/
	public static void addSale(Connection dbconn, Scanner userIn){
		int saleId = generateUniqueId(dbconn, "sale");

		System.out.print("Date: ");
		String date = userIn.nextLine();
		System.out.println();

		System.out.print("Payment Method: ");
		String method = userIn.nextLine();
		System.out.println();

		System.out.print("Member ID: ");
		String memId = userIn.nextLine();
		System.out.println();

		String query = "INSERT INTO sale(id, saleDate, paymentMethod, totalPrice, memberId)"+
			"VALUES('" + saleId + "', '" + date + "', '" + method + "', '0', '" + memId + "')";

		try {
			Statement stmt = dbconn.createStatement();
			stmt.executeQuery(query);
			stmt.close();
		} catch (SQLException e) {
			System.err.println("*** SQLException:  "
                		+ "error adding new sale into sale table.");
			System.err.println("\tMessage:   " + e.getMessage());
            		System.err.println("\tSQLState:  " + e.getSQLState());
            		System.err.println("\tErrorCode: " + e.getErrorCode());
            		System.exit(-1);
		}
	}

	/*---------------------------------------------------------------------
        |  Method addSubsale
        |
        |  Purpose:  Adds a new sub-sale row to the sub-sale table. The user will
	|	be prompted to enter info for the following attributes: saleId, 
	|	productId, salePrice, and amount. Once a new sub-sale is created
	|	the sale record it is tied to will also be update to reflect the
	|	contents of the sub-sale.
        |
        |  Pre-condition:  The Connection must be open and the sub-sale table
	|	must exist.
        |
        |  Post-condition: A new row will be added to the sub-sale table and
	|	a the corresponding sale record will be updated.
        |
        |  Parameters:
        |      dbconn -- Connection object used to communicate with oracle
	|      userIn -- Scanner used to read in user input
        |
        |  Returns:  None.
        *-------------------------------------------------------------------*/
	public static void addSubsale(Connection dbconn, Scanner userIn){
		int subId = generateUniqueId(dbconn, "subsale");

		System.out.print("Sale ID: ");
		String saleId = userIn.nextLine();
		System.out.println();

		String query = "SELECT memberId, totalPrice FROM sale WHERE id='" + saleId + "'";
		int memId = 0;
		double oldTotal = 0.0;
		try {
			Statement stmt = dbconn.createStatement();
			ResultSet results = stmt.executeQuery(query);
			results.next();
			memId = results.getInt(1);
			oldTotal = results.getDouble(2);
			stmt.close();
		} catch (SQLException e) {
			System.err.println("*** SQLException:  "
                		+ "error selecting sale record associated with sub-sale.");
			System.err.println("\tMessage:   " + e.getMessage());
            		System.err.println("\tSQLState:  " + e.getSQLState());
            		System.err.println("\tErrorCode: " + e.getErrorCode());
            		System.exit(-1);
		}

		query = "SELECT active FROM member WHERE id='" + memId + "'";
		int active = 0;
		try {
			Statement stmt = dbconn.createStatement();
			ResultSet results = stmt.executeQuery(query);
			results.next();
			memId = results.getInt(1);
			stmt.close();
		} catch (SQLException e) {
			System.err.println("*** SQLException:  "
                		+ "error selecting active status from member record.");
			System.err.println("\tMessage:   " + e.getMessage());
            		System.err.println("\tSQLState:  " + e.getSQLState());
            		System.err.println("\tErrorCode: " + e.getErrorCode());
            		System.exit(-1);
		}

		System.out.print("Product ID: ");
		String prodId = userIn.nextLine();
		System.out.println();

		query = "SELECT retailPrice, membershipDiscount FROM product WHERE id='" + prodId + "'";
		double retail = 0.0;
		double discount = 0.0;
		try {
			Statement stmt = dbconn.createStatement();
			ResultSet results = stmt.executeQuery(query);
			results.next();
			retail = results.getDouble(1);
			discount = results.getDouble(2);
			stmt.close();
		} catch (SQLException e) {
			System.err.println("*** SQLException:  "
                		+ "error selecting retail price and membership discount from product.");
			System.err.println("\tMessage:   " + e.getMessage());
            		System.err.println("\tSQLState:  " + e.getSQLState());
            		System.err.println("\tErrorCode: " + e.getErrorCode());
            		System.exit(-1);
		}

		System.out.print("Amount: ");
		int amount = Integer.parseInt(userIn.nextLine());
		System.out.println();

		double sale = 0.0;
		if(active == 1){
			sale = retail - discount;
		}else{
			sale = retail;
		}

		query = "INSERT INTO subsale(id, saleId, productId, salePrice, amount)" + 
			"VALUES('" + subId + "', '" + saleId + "', '" + prodId + "', '" + sale + "', '" + amount + "')";
		try {
			Statement stmt = dbconn.createStatement();
			stmt.executeQuery(query);
			stmt.close();
		} catch (SQLException e) {
			System.err.println("*** SQLException:  "
                		+ "error inserting new sub-sale record.");
			System.err.println("\tMessage:   " + e.getMessage());
            		System.err.println("\tSQLState:  " + e.getSQLState());
            		System.err.println("\tErrorCode: " + e.getErrorCode());
            		System.exit(-1);
		}
		
		oldTotal = oldTotal + (sale*amount);
		String newTotal = String.valueOf(oldTotal);
		updateField(dbconn, "sale", saleId, "totalPrice", newTotal);
	}
	
	/*---------------------------------------------------------------------
        |  Method updateSubSale
        |
        |  Purpose:  Collects information from the user that will be used to update
	|	an attribute of a single row in the sub sale table.
        |
        |  Pre-condition:  The Connection must be open and the supplier table
	|	must exist.
        |
        |  Post-condition: The value of one of the columns of the rows in
	|	the supplier table will be updated.
        |
        |  Parameters:
        |      dbconn -- Connection object used to communicate with oracle
	|      userIn -- Scanner used to read in user input
        |
        |  Returns:  None.
        *-------------------------------------------------------------------*/
	public static void updateSubSale(Scanner userIn, Connection dbconn){
		System.out.print("What is the id of the sub sale you wish to update? ");
		String id = userIn.nextLine();
		System.out.println();
	
		System.out.print("What field would you like to update?(productID, saleID, salePrice, amount)");
		String field = userIn.nextLine().toLowerCase();
	
		System.out.print("What should the new value of " + field + " be? ");
		String value = userIn.nextLine();
		System.out.println();
	
		int saleId = 0;
		int productId = 0;
		double salePrice = 0.0;
		int amount = 0;
		double totalPrice = 0.0;
		int memberId = 0;
		try {
			Statement stmt = dbconn.createStatement();
			String getSubsaleFields = "SELECT saleId, productId, salePrice, amount " + 
				"FROM subsale WHERE id='" + id + "'";
			ResultSet subsaleResult = stmt.executeQuery(getSubsaleFields);
			subsaleResult.next();
			saleId = subsaleResult.getInt(1);
			productId = subsaleResult.getInt(2);
			salePrice = subsaleResult.getDouble(3);
			amount = subsaleResult.getInt(4);
			
			String getSaleFields = "SELECT totalPrice, memberId FROM sale WHERE id='" + saleId + "'";
			ResultSet saleResult = stmt.executeQuery(getSaleFields);
			saleResult.next();
			totalPrice = saleResult.getDouble(1);
			memberId = saleResult.getInt(2);
			stmt.close();
		} catch (SQLException e) {
			System.err.println("*** SQLException:  "
                		+ "error fetching sale and subsale columns.");
			System.err.println("\tMessage:   " + e.getMessage());
            		System.err.println("\tSQLState:  " + e.getSQLState());
            		System.err.println("\tErrorCode: " + e.getErrorCode());
            		System.exit(-1);
		}

		if(field.equals("productid")){
			double retailPrice = 0.0;
			double membershipDiscount = 0.0;
			int active = 0;
			try {
				Statement stmt = dbconn.createStatement();
				String query = "SELECT retailPrice, membershipDiscount FROM product " +
					"WHERE id='" + value + "'";
				ResultSet prodResult = stmt.executeQuery(query);
				prodResult.next();
				retailPrice = prodResult.getDouble(1);
				membershipDiscount = prodResult.getDouble(2);

				query = "SELECT active FROM member " +
					"WHERE id='" + memberId + "'";
				ResultSet memResult = stmt.executeQuery(query);
				memResult.next();
				active = memResult.getInt(1);
				stmt.close();
			} catch (SQLException e) {
				System.err.println("*** SQLException:  "
                			+ "error selecting fields from product and member.");
				System.err.println("\tMessage:   " + e.getMessage());
            			System.err.println("\tSQLState:  " + e.getSQLState());
            			System.err.println("\tErrorCode: " + e.getErrorCode());
            			System.exit(-1);
			}
			if(active == 1){
				totalPrice = amount*(retailPrice-membershipDiscount);
				salePrice = retailPrice-membershipDiscount;
			}else{
				totalPrice = amount*retailPrice;
				salePrice = retailPrice;
			}
			System.out.println("TotalPrice: " + totalPrice);
			updateField(dbconn, "sale", String.valueOf(saleId), "totalPrice", String.valueOf(totalPrice));
			updateField(dbconn, "subsale", id, "salePrice", String.valueOf(salePrice));
		}else if(field.equals("saleprice")){
			double newPrice = Double.parseDouble(value);
			if(newPrice > salePrice){
				totalPrice = totalPrice + (amount*(newPrice-salePrice));
			}else{
				totalPrice = totalPrice - (amount*(salePrice-newPrice));
			}
			updateField(dbconn, "sale", String.valueOf(saleId), "totalPrice", String.valueOf(totalPrice));
		}else if(field.equals("amount")){
			int newAmount = Integer.parseInt(value);
			if(newAmount > amount){
				totalPrice = totalPrice + (salePrice*(newAmount-amount));
			}else{
				totalPrice = totalPrice - (salePrice*(amount-newAmount));	
			}
			updateField(dbconn, "sale", String.valueOf(saleId), "totalPrice", String.valueOf(totalPrice));
		}

		//the row will now be updated
		updateField(dbconn, "subsale", id, field, value);
	}


	/*---------------------------------------------------------------------
    	|  Method updateSale
    	|
    	|  Purpose:  Collects information from the user that will be used to update
	|	an attribute of a single row in the sub sale table.
    	|
    	|  Pre-condition:  The Connection must be open and the supplier table
	|	must exist.
        |
        |  Post-condition: The value of one of the columns of the rows in
	|	the supplier table will be updated.
        |
        |  Parameters:
        |      dbconn -- Connection object used to communicate with oracle
	|      userIn -- Scanner used to read in user input
        |
        |  Returns:  None.
        *-------------------------------------------------------------------*/
	public static void updateSale(Scanner userIn, Connection dbconn){
		System.out.print("What is the id of the sale you wish to update? ");
		String id = userIn.nextLine();
		System.out.println();
	
		System.out.print("What field would you like to update?(saleDate, paymentMethod, memberID)");
		String field = userIn.nextLine().toLowerCase();
	
		System.out.print("What should the new value of " + field + " be? ");
		String value = userIn.nextLine();
		System.out.println();
	
		//the row will now be updated
		updateField(dbconn, "sale", id, field, value);
	}

	/**searchMembers
	 * Write a query that displays the member by searching the phone number or the ID.
	 * The results should display the member name, date of birth, and reward points
	 * 
	 * @param identifier a phone number of the form xxx-xxx-xxxx or a memberID
	 * @param identifierType a string which should refer to what the user is searching by,
	 * either "phoneNumber" or "id"
	 * @param dbconn a connection to the oracle database
	 * @return a string representation of the member name, date of birth, and reward points
	 */
	public static String searchMembers(String identifier, String identifierType, Connection dbconn){
		Statement stmt = null;
		ResultSet answer = null;
		String msg = "";

		//Create query string
		String query = "SELECT * from member" + 
					   " WHERE " + identifierType + "='" + identifier + "'";
		
		try {
			stmt = dbconn.createStatement();
			answer = stmt.executeQuery(query);

			while(answer.next()){
				//display the member name, date of birth, and reward points
				msg += 
					"\n\nName: " +
					answer.getString("firstName") + " " +
					answer.getString("lastName") + "\n" +
					"Birth Date: " +
					answer.getString("dob") + "\n" +
					"Points: " +
					Integer.toString(answer.getInt("rewardPoints"));
			}

			stmt.close();

		} catch (SQLException e) {
            		System.err.println("*** SQLException:  "
                		+ "Could not fetch member phone/id query results.");
			//double check that the column name passed to getAttribute is valid
			System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
		}

		//if no users found, return relevant string
		//else return display of user data
		if(msg.equals("")){
			return "NO MEMBER MATCHES FOUND";
		}
		return msg;
	}


	/**getMostProfitableProduct
	 * checks each products total profit (after including the cost from buying from the supplier)
	 * and returns a string with the most profitable item
	 * 
	 * calls getProductProfit, which may throw an SQL exception
	 * @param dbconn a connection to the oracle database
	 * @return a string representation of the most profitable item in the database
	 */
	public static String getMostProfitableProduct(Connection dbconn){

		String itemName = "NO ITEM FOUND";
		Double profit = 0.0;

		try {
			Statement stmt = null;
			ResultSet answer = null;
			
			stmt = dbconn.createStatement();
			answer = stmt.executeQuery("SELECT * FROM product");
			//the first item is by default the most profitable (makes sure highest profit can be negative)
			if(answer.next()){
				itemName = answer.getString("name");
				profit = getProductProfit(answer.getInt("id"), dbconn);
			}
			//get the total profit for each item in the database
			//if its higher, save it
			while(answer.next()){
				Double currProfit = getProductProfit(answer.getInt("id"), dbconn);
				if(currProfit > profit){
					itemName = answer.getString("name");
					profit = currProfit;
				}
			}

			stmt.close();

		} catch (SQLException e) {
			System.err.println("*** SQLException:  "
                + "Could not fetch item profit query results.");
			//double check that the column name passed to getAttribute is valid
			System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
		}

		return "Most profitable product: "+ itemName + "\n" +
			   "Total profit made from "+itemName+": "+ Double.toString(profit);
	}

	/**getProductProfit
	 * gets the amount that the store made in profit for an item
	 * total sales = summa(all subsales of item, (amount sold in a subsale) * (price the item in that subsale))
	 * profit = total sales - sum(amount attribute) * cost of purchase from warehouse
	 * this function is called by getMostProfitableProduct
	 * 
	 * @param id an integer identifying the product type
	 * @param dbconn a connection to the oracle databse
	 * @return the profit made on that item
	 * @throws SQLexeption if there is some trouble opening or closing a statement with the Connection dbconn
	 */
	public static Double getProductProfit(int id, Connection dbconn) throws SQLException{
		Statement stmt = null;
		ResultSet answer = null;

		stmt = dbconn.createStatement();
		answer = stmt.executeQuery("SELECT * FROM subsale WHERE productId = " + id);
		
		int totalAmount = 0;
		int totalSales = 0;
		while(answer.next()){
			int amount = answer.getInt("amount");
			Double price = answer.getDouble("salePrice");

			totalAmount+= amount;
			totalSales+= amount * price;
		}
		stmt.close();	//close subsales

		//get the cost of buying from the supplier
		stmt = dbconn.createStatement();
		answer = stmt.executeQuery("SELECT purchasePrice FROM supply WHERE productId = " + id);
		
		Double profit = 0.0;	//the total profit after cost of buying from supplier
		if(answer.next()){		//put cursor on first row
			Double purchasePrice = answer.getDouble("purchasePrice");	//get what the retailer payed for it
			profit = totalSales - (purchasePrice * totalAmount);
		}else{
			System.out.println("could not find product id " + id );
		}
		stmt.close();	//close supply

		return profit;
	}

	/**getSavings
	 * Iterates through all sale records associated with member and finds all subsale
	 * records associated with the sale. From there the member's total savings are
	 * calculated. The members name and total savings are printed to the screen.
	 * 
	 * @param memberID a string referring to a member ID
	 * @param dbconn is the Connection object we use to communicate with the database
	 * @return None
	 *
	 */
	public static void getSavings(String memberId, Connection dbconn){
		ArrayList<Integer> sales = new ArrayList<Integer>();//all sales associated with member
		String firstName = "";
		String lastName = "";
		int active = 0;//no savings if the member isn't active
		try {//get all the sale records associated with the account
			Statement stmt = dbconn.createStatement();
			String query = "SELECT id FROM sale WHERE memberId='" + memberId + "'";
			ResultSet saleResult = stmt.executeQuery(query);
			while(saleResult.next()){//put all sale record id's in a list
				sales.add(saleResult.getInt(1));
			}

			query = "SELECT firstName, lastName, active FROM member WHERE id='" + memberId + "'";
			ResultSet memResult = stmt.executeQuery(query);//get info on the member
			memResult.next();
			firstName = memResult.getString(1);
			lastName = memResult.getString(2);
			active = memResult.getInt(3);
			stmt.close();
		} catch (SQLException e) {
			System.err.println("*** SQLException:  "
                		+ "error selecting sale records associated with member.");
			System.err.println("\tMessage:   " + e.getMessage());
            		System.err.println("\tSQLState:  " + e.getSQLState());
            		System.err.println("\tErrorCode: " + e.getErrorCode());
            		System.exit(-1);
		}

		if(active == 0){//can't have savings if you aren't a member
			System.out.println("The user " + firstName + " " + 
				lastName + " has saved $0.00 :(");
			return;
		}

		//will hold all subsale record data we need associated with the member
		ArrayList<String> subsales = new ArrayList<String>();
		for(int saleId : sales){//get all subsale records associated with each sale record
			try{
				Statement stmt = dbconn.createStatement();
				String query = "SELECT productId, amount FROM subsale WHERE saleId='" + saleId + "'";
				ResultSet subResult = stmt.executeQuery(query);//get info to look up products
				subResult.next();
				int prodId = subResult.getInt(1);
				int amount = subResult.getInt(2);
				subsales.add(prodId + " " + amount);
				stmt.close();
			}catch (SQLException e) {
				System.err.println("*** SQLException:  "
                			+ "error selecting productId and amount from subsale.");
				System.err.println("\tMessage:   " + e.getMessage());
            			System.err.println("\tSQLState:  " + e.getSQLState());
            			System.err.println("\tErrorCode: " + e.getErrorCode());
            			System.exit(-1);
			}	
		}

		double savings = 0.0;//the total savings over time
		for(String sub : subsales){//for every subsale associated with the member, calc savings
			String subarr[] = sub.split(" ");
			String productId = subarr[0];
			int amount = Integer.parseInt(subarr[1]);
			double memberDiscount = 0.0;
			try{
				Statement stmt = dbconn.createStatement();
				String query = "SELECT membershipDiscount FROM product WHERE id='" + productId + "'";
				ResultSet prodResult = stmt.executeQuery(query);//get discount per item in dollars
				prodResult.next();
				memberDiscount = prodResult.getDouble(1);
				stmt.close();
			}catch (SQLException e) {
				System.err.println("*** SQLException:  "
                			+ "error selecting productId and amount from subsale.");
				System.err.println("\tMessage:   " + e.getMessage());
            			System.err.println("\tSQLState:  " + e.getSQLState());
            			System.err.println("\tErrorCode: " + e.getErrorCode());
            			System.exit(-1);
			}
			savings += amount*memberDiscount;//get total savings per subsale
		}
		
		System.out.println("\nThe user " + firstName + " " + lastName + " has saved $" + savings + "!\n");
	}





	/**************************************************
	 * 					INTERFACE					  *
	 **************************************************/

	public static void printMenuOptions(){
        System.out.println( "************************************************************\n" +
							"*                SHOPPING MALL INFORMATION                 *\n" +
							"*                                                          *\n" +
                            "************************************************************\n" +
                            "|Type a number corresponding to the query                   \n" +
                            "|that you would like to execute, and press enter.           \n" +
                            "|\t--to exit, type 'exit'                                   \n" +
                            "|options:                                                   \n" +
							"|(1) Add, update or delete a member, employee, 	         \n" +
							"|\tproduct, and supplier							         \n" +
                            "|(2) Add or update sale records and sub sale records	     \n" +
							"|(3) displays the member by searching the 					 \n" +
							"|\tphone number or the ID								     \n" +
							"|(4) Check the current the most profitable product over	 \n" +
							"|\tall the products in the database	            		 \n" +
							"|(5) Check the lifetime savings of a member	             \n" +
							"|to see these options again, type 'options'	            \n" +							
							"____________________________________________________________\n\n");
	}	

	/*---------------------------------------------------------------------
        |  Method addUpdateRemove
        |
        |  Purpose:  Handles the text based interface for choosing to add,
	|	update, and delete operations on the member, employee, product,
	|	and supplier tables.
        |
        |  Pre-condition:  The Connection must be open, the Scanner to get user
	|	input must be open, and the member, employee, product, and supplier 
	|	tables must exist.
        |
        |  Post-condition: The user will be directed to the operation they want.
        |
        |  Parameters:
	|      userIn -- the Scanner object used to fetch info from the user
        |      dbconn -- Connection object used to communicate with oracle
        |
        |  Returns:  None.
        *-------------------------------------------------------------------*/
	public static void addUpdateRemove(Scanner userIn,Connection dbconn){
		System.out.println("\nOPTIONS:\n" +
			"\t (1) Add a Record\n" +
			"\t (2) Update a Record\n" +
			"\t (3) Delete");
		System.out.print(":>>>");
		switch(userIn.nextLine()){
			case "1":
				System.out.println("\nOPTIONS:\n" +
					"\t (1) Add Member\n" +
					"\t (2) Add Employee\n" +
					"\t (3) Add Product\n" +
					"\t (4) Add Supplier");
				System.out.print(":>>>");
				switch(userIn.nextLine()){
					case "1":
						addMember(dbconn, userIn);
						break;
					case "2":
						addEmployee(dbconn, userIn);
						break;
					case "3":
						addProduct(dbconn, userIn);
						break;
					case "4":
						addSupplier(dbconn, userIn);
						break;
					default:
						System.out.println("Not a valid option.");
						break;
				}
				break;
			case "2":
				System.out.println("\nOPTIONS:\n" +
					"\t (1) Update Member\n" +
					"\t (2) Update Employee\n" +
					"\t (3) Update Product\n" +
					"\t (4) Update Supplier");
				System.out.print(":>>>");
				switch(userIn.nextLine()){
					case "1":
						updateMember(dbconn, userIn);
						break;
					case "2":
						updateEmployee(dbconn, userIn);
						break;
					case "3":
						updateProduct(dbconn, userIn);
						break;
					case "4":
						updateSupplier(dbconn, userIn);
						break;
					default:
						System.out.println("Not a valid option.");
						break;
				}
				break;
			case "3":
				deleteRow(dbconn, userIn);
				break;
			default:
				System.out.println("Not a valid option.");
				break;
		}
	}
	
	/*---------------------------------------------------------------------
    	|  Method addUpdate
    	|
    	|  Purpose:  Handles the text based interface for choosing to add,
	|	and update sale and subsale tables
    	|
    	|  Pre-condition:  The Connection must be open, the Scanner to get user
	|	input must be open, and the member, employee, product, and supplier 
	|	tables must exist.
        |
        |  Post-condition: The user will be directed to the operation they want.
        |
        |  Parameters:
	|      userIn -- the Scanner object used to fetch info from the user
        |      dbconn -- Connection object used to communicate with oracle
        |
        |  Returns:  None.
        *-------------------------------------------------------------------*/
	public static void addUpdate(Scanner userIn,Connection dbconn){
		System.out.println("\nOPTIONS:\n" +
			"\t (1) Add a Sale or Sub-Sale\n" +
			"\t (2) Update a Sale or Sub-Sale\n");
		System.out.print(":>>>");
		switch(userIn.nextLine()){
			case "1":
				System.out.println("\nOPTIONS:\n" +
					"\t (1) Add Sale\n" +
					"\t (2) Add Sub-Sale");
				System.out.print(":>>>");
				switch(userIn.nextLine()){
					case "1":
						addSale(dbconn, userIn);
						break;
					case "2":
						addSubsale(dbconn, userIn);
						break;
					default:
						System.out.println("Not a valid option.");
						break;
				}
				break;
			case "2":
				System.out.println("\nOPTIONS:\n" +
					"\t (1) Update Sub-Sale\n" +
					"\t (2) Update Sale");
				System.out.print(":>>>");
				switch(userIn.nextLine()){
					case "1":
						updateSubSale(userIn, dbconn);
						break;
					case "2":
						updateSale(userIn, dbconn);
						break;
					default:
						System.out.println("Not a valid option.");
						break;
				}
				break;
		}
	}

	/**addUpdateSale
	 * this extends the UI to give the user the option to add
	 * or update a sale or subsale based on their further input
	 * 
	 * @param dbconn a connection to the database
	 * @param userIn a scanner from System.in
	 */
	public static void addUpdateSale(Connection dbconn, Scanner userIn){
		System.out.println("OPTIONS: \n" +
			"\t (1) Add a Sale or Sub-Sale\n" +
			"\t (2) Update a Sale or Sub-Sale");
		System.out.print(":>>>");
		switch(userIn.nextLine()){
			case "1":
				System.out.println("OPTIONS: \n" + 
					"\t (1) Add Sale\n" +
					"\t (2) Add Subsale");
				System.out.print(":>>>");
				switch(userIn.nextLine()){
					case "1":
						addSale(dbconn, userIn);
						break;
					case "2":
						break;
					default:
						System.out.println("Not a valid option.");
						break;
				}
				break;
			case "2":
				System.out.println("OPTIONS: \n" + 
					"\t (1) Update Sale\n" +
					"\t (2) Update Subsale");
				System.out.print(":>>>");
				switch(userIn.nextLine()){
					case "1":
						break;
					case "2":
						break;
					default:
						System.out.println("Not a valid option.");
						break;
				}
				break;
			default:
				System.out.println("Not a valid option.");
				break;
		}
	}

	/**getMember
	 * gives the user an option to search by id or phone number
	 * performs an sql query to find members with the matching attribute type
	 * or prints an error message and calls this function again
	 * @param userin a scanner from System.in
	 * @param dbconn a connection the the oracle database
	 */
	public static void getMember(Scanner userin, Connection dbconn){
		System.out.println("\n Would you like to search by:"+
						   "\n(1) id?"+
						   "\n(2) phone number?");

		String identifier = "";
		String msg = "";
		
		switch(userin.nextLine()){
			case "1":
				System.out.println("input user ID: ");
				identifier += userin.nextLine();
				msg = searchMembers(identifier, "id", dbconn);
				break;
			case "2":
				System.out.println("input user phone number: ");
				identifier += userin.nextLine();
				msg = searchMembers(identifier, "phoneNumber", dbconn);
				break;
			default:
				System.out.println("not an option, type a number matching an option\n");
				getMember(userin, dbconn);
				break;
		}
    
    if(!msg.equals("")){
		    System.out.println(msg + "\n");
    }
  }

	/**getMemberSavings
	 * Asks the user what member they would like to get savings data on. Checks
	 * if the member exists in the database and them calls getSavings() if the
	 * member does exist which will get and print the total savings over time.
	 *
	 * @param userin is the Scanner we use to collect input from the user
	 * @param dbconn is the Connection object we use to communicate with the database
	 * @return None
	 *
	 */
	public static void getMemberSavings(Scanner userin, Connection dbconn){
		//ask for member id
		System.out.println("What is the ID of the member you wish to check the savings of?");
		String memberID = userin.nextLine();

		int count = 0;//if count is == 1 then the member exists in the database
		try {//check that member exists
			Statement memberStmt = dbconn.createStatement();
			String query = "SELECT COUNT(id) FROM member WHERE id='" + memberID + "'";
			ResultSet member = memberStmt.executeQuery(query);//get number of members with given id
			member.next();
			count = member.getInt(1);
			memberStmt.close();
		} catch (SQLException e) {
			System.err.println("*** SQLException:  "
				+ "Could not fetch member savings query results. " +
				  "possible error in statements getting member, sale, subsale, or product");
			System.err.println("\tMessage:   " + e.getMessage());
            		System.err.println("\tSQLState:  " + e.getSQLState());
            		System.err.println("\tErrorCode: " + e.getErrorCode());
            		System.exit(-1);
		}
		if(count == 1){//member exists find savings
			getSavings(memberID, dbconn);
		}else{
			System.out.println("MEMBER ID NOT FOUND");
		}
	}


	/**************************************************
	 * connection creation and program exit functions *
	 **************************************************/

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
    public static Connection getConnection(String[] args){

        String oracleURL =   // Magic lectura -> aloe access spell
        "jdbc:oracle:thin:@aloe.cs.arizona.edu:1521:oracle";
		String username = null;
		String password = null;
		try {
			username = args[0];    // Oracle DBMS username
        	password = args[1];    // Oracle DBMS password
		} catch (IndexOutOfBoundsException e) {
			System.out.println("first arg: oracle username, second: password");
			System.exit(1);
		}
        

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
	
	/**exitProgram
	 * cleans up and exits the program
	 * for use when the user requests that the program ends
	 * @param Connection dbconn a conection to the oracle sql database
	 * userin is a Scanner stream from sysIn
	 * @return void
	 */
	public static void exitProgram(Scanner userin, Connection dbconn){
		userin.close();			//close the scanner
		System.out.println("Closing connection to the database...");
		try{
			dbconn.close();		//close the dbconnection
		}catch(SQLException e){
			System.err.println("*** SQLException:  "
                    + "Could not close JDBC connection.");
                System.err.println("\tMessage:   " + e.getMessage());
                System.err.println("\tSQLState:  " + e.getSQLState());
                System.err.println("\tErrorCode: " + e.getErrorCode());
                System.exit(-1);
		}
		System.out.println("Exiting...\n");
		System.exit(0);
	}
}