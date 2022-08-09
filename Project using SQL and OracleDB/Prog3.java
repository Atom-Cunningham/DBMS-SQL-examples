/**Prog3
 * 
 * Adam Cunningham, csc 460 Database design, Prog3, Dr. McCann & your name (TA), due Oct 5
 * 
 * 
 * This program prompts the user for a query option, a through d
 * It then displays the resulting query as a list of names or counts
 * of the type described by the menu
 * 
 * To accomplish this, the program uses a combination of SQL and java
 * to contact laser@oracle.aloe, and retrieve tables containing data
 * about precincts for the state of Montana
 * 
 * Using the tables returned from the database, this program makes liberal use
 * of hashMaps and hashSets to count various things about the precincts
 * and to ensure that when needed, uniqueness is preserved
 * 
 * this program makes no changes to the database itself
 * all Relation creation and population is handled by MakeTables.java
 * and by Populate.java
 * 
 * 
 * 
 * java 10 (8 compatible)
 * no args
 */
import java.sql.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner; 

class Prog3{
    public static void main(String[] args){
        Connection dbconn = getConnection();
        Scanner userInput = new Scanner(System.in);
        String year;
        while(true){
            printMenuOptions();
            System.out.print(":>>>");
            switch (userInput.next()){
                case "a":
                    queryA(dbconn);
                    break;
                case "b":
                    year = getYear(userInput);
                    queryB(year, dbconn);
                    break;
                case "c":
                    year = getYear(userInput);
                    queryC(year, dbconn);
                    break;
                case "d":
                    queryD(dbconn);
                    break;
                case "exit":
                    try {
                        dbconn.close();
                    } catch (SQLException e) {
        
                        System.err.println("*** SQLException:  "
                            + "Could not close JDBC connection.");
                        System.err.println("\tMessage:   " + e.getMessage());
                        System.err.println("\tSQLState:  " + e.getSQLState());
                        System.err.println("\tErrorCode: " + e.getErrorCode());
                        System.exit(-1);
        
                    }
                    System.exit(0);
                default:
                    System.out.println("\n-\t!!!\nnot a valid option\n-\t!!!");
                    break;
            }
        }
    }
    
    /**getYear
     * asks the user for a year
     * and returns the year they type if it is valid
     * @return a String
     */
    public static String getYear(Scanner userInput){
        String year = "";
        boolean validYear = false;
        while(!validYear){
            System.out.println("select a year:");
            System.out.println("type twelve, fourteen, sixteen, or eighteen");
            year = userInput.next();
            if (year.equals("twelve")
            || year.equals("fourteen")
            || year.equals("sixteen")
            || year.equals("eighteen")){
                validYear = true;
            }else{
                System.out.println(year + " is not a valid year option");
            }
        }
        return year;
    }

    /**printMenuOptions
     * 
     * a helper function for printing the menu Options
     * for the user interface
     */
    public static void printMenuOptions(){
        System.out.println( "************************************************************\n" +
                            "*               Montana precinct information               *\n" +
                            "*                  Years '12 '14 '16 '18                   *\n" +
                            "************************************************************\n" +
                            "|Type a letter corresponding to the query                   \n" +
                            "|that you would like to display, and press enter.           \n" +
                            "|\t--to exit, type exit                                     \n"+
                            "|options:                                                   \n" +
                            "|(a) Number of precincts in each county in the year 2012    \n" +
                            "|(b) Municipalities with the most precincts (top 5)         \n" +
                            "|(c) Number of times the words church, school,              \n" +
                            "|\thall, and college appear in the names of precincts       \n" +
                            "|(d) Lists of counties which have lost or gained            \n" +
                            "|\t precincts over the last eight years                     \n" +
                            "____________________________________________________________\n\n");
    }

    /**queryA
     * this answers the query
     * (a) For each county name in the 2012 relation,
     *  how many precincts were in that county? 
     * (There is no user input for this query.)
     * 
     * by getting the column of county names from the database,
     * counting the instances of each name by updating their counts in a hashmap
     * and adding unencountered items to a list
     * this then sorts the list into alphabetical order
     * and prints the count of each name for the user
     * 
     * @param dbconn a connection to the oracle database
     * @return void
     */
    public static void queryA(Connection dbconn){
        HashMap<String,Integer> count = new HashMap<>();
        ArrayList<String> counties = new ArrayList();

        try {
            count = getCountMap("twelve", "county_name", dbconn);
            counties.addAll(count.keySet());

            Collections.sort(counties);
            for(String county : counties){
                System.out.println("county: " + county + "\t\tprecincts: " + count.get(county));
            }
        } catch (SQLException e) {

            System.err.println("*** SQLException:  "
                + "Could not fetch query A results.");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);

        }
    }

    /**queryB
     * answers the query:
     * For a given year (that is, a year provided by the user of your program),
     * what are the top five municipalities by numbers of precincts?
     * Display the names of the municipalities and their quantities
     * of precincts in descending order by quantity.
     * Thus, the first one listed will have the most precincts
     * 
     * by creating a map of municipality names to periodicity
     * then creates a collection of the counts, converts it to a list,
     * sorts that list, finds a municipality with the 
     * @param year a string representing the year of data to check
     * @param dbconn a connection to an oracle database
     */
    public static void queryB(String year, Connection dbconn){

        HashMap<String,Integer> count = new HashMap<>();

        try {
            count = getCountMap(year, "municipality", dbconn);

            Collection<Integer> countCollection = null;
            //edge case, if empty, let user know there were no municipalities
            if(count.isEmpty()){
                System.out.println("There were no municipalities with precincts,"+
                " or municipalities were unlabeled");
            }else {
                //get list of counts
                countCollection = count.values();
                ArrayList<Integer> countList = new ArrayList<>();
                for (Integer n : countCollection){
                    countList.add(n);
                }
                Collections.sort(countList);    //sort low to high
                Collections.reverse(countList); //reverse

                //print them all if countList size is less than 5
                //else print highest 5
                int end = Integer.min(countList.size(), 5);
                Integer currMax = 0;    //the index of the current highest number in the sorted countList
                while (currMax < end){
                    for (String key : count.keySet()){
                        //find a key with a value equal to the current highest number on the list
                        //print it, and the number, and remove that key from the map
                        //then move to the next highest number
                        //and break out of the for loop, into the outer while loop
                        if (count.get(key).equals(countList.get(currMax))){
                            System.out.println("Municipality: "+ key + 
                                               "\tprecinct count: " + countList.get(currMax));
                            count.remove(key);
                            break;
                        }
                    }
                    currMax++;
                }
            }

        } catch (SQLException e) {
            System.err.println("*** SQLException:  "
                + "Could not fetch query B results.");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);
        }
    }

    /**queryC
     * answers the query:
     * (c) For a given year, how many of the precinct names 
     * contain the exact words CHURCH, HALL,
     * SCHOOL, or COLLEGE? For each of those words, 
     * in alphabetical order, list the word and the
     * quantity of precinct names that contain it. 
     * If a name contains more than one of those words, 
     * the name is to counted in the totals for all of the words.
     *  For example, if there is a precinct named
     * “BOB’S CHURCH HALL SCHOOL AND COLLEGE, 
     * it would be included in all four totals.
     * 
     * it answers this by using a map of those words, and for each precinct name,
     * increments the count of that word if it is contained in the precinct name
     * it then prints out the counts in alphabetical order
     * @param year the .extension label of the database
     * @param dbconn a Connection to the oracle database containing precinct data
     */
    public static void queryC(String year, Connection dbconn){
        Statement stmt = null;
        ResultSet answer = null;

        String query = "SELECT precinct_name from laser."+year;

        //create sorted list of target words
        HashMap<String,Integer> count = new HashMap<>();
        ArrayList<String> keyWords = new ArrayList();
        keyWords.add("CHURCH");
        keyWords.add("HALL");
        keyWords.add("SCHOOL");
        keyWords.add("COLLEGE");
        Collections.sort(keyWords);

        try {
            stmt = dbconn.createStatement();
            answer = stmt.executeQuery(query);

            while(answer.next()){
                String name = answer.getString("precinct_name");
                if (name == null){
                    continue;
                }

                //if the name contains a keyword, increase the count
                for (String keyWord : keyWords){
                    if (name.contains(keyWord)){
                        //first time? add to hashMap and list
                        if (!count.containsKey(keyWord)){
                            count.put(keyWord, 1);
                        }else{
                            //increment curr count
                            Integer curr = count.get(keyWord);
                            count.replace(keyWord, curr, curr+1);
                        }
                    }
                }
            }

            System.out.println("number of precinct names contining church,"+
                               " hall, school, or college:");
            for(String keyWord : keyWords){
                Integer currCount = count.get(keyWord);
                if (currCount == null){
                    currCount = 0;
                }
                System.out.println(keyWord + "\tcount: " + currCount);
            }

            stmt.close();

        } catch (SQLException e) {

            System.err.println("*** SQLException:  "
                + "Could not fetch query C results.");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);

        }
    }

    /**queryD
     * answers the query:
     * there are three pairs of subsequent years: 2012 and 2014, 2014 and
     * 2016, and 2016 and 2018. Which counties did not lose precincts in
     * all three pairs of subsequent years, and which counties did not gain 
     * precincts in all three pairs of subsequent years? For example,
     * imagine that county Armagh had 14 precincts in all four years. 
     * We would include Armagh in both lists. Another example: Imagine county 
     * Tyrone had 6 precincts in 2012, 5 in 2014, 5 in 2016, and
     * 4 in 2018. Tyrone would be in the list of counties that did not gain precincts
     *  in all three pairs of subsequent years. 
     * Note that it’s possible for a county to be in neither list.
     * 
     * it accomplishes this by using metatdata from the ResultSet object
     * returned by the answer to a query execution
     */
    public static void queryD(Connection dbconn){
        try {
            String fieldName = "county_name";
            HashMap<String,Integer> twelve = getCountMap("twelve", fieldName, dbconn);
            HashMap<String,Integer> fourteen = getCountMap("fourteen", fieldName, dbconn);
            HashMap<String,Integer> sixteen = getCountMap("sixteen", fieldName, dbconn);
            HashMap<String,Integer> eighteen = getCountMap("eighteen", fieldName, dbconn);

            HashMap<String,Integer> twelveFourteen = getDifferenceMap(twelve, fourteen);
            HashMap<String,Integer> fourteenSixteen = getDifferenceMap(fourteen, sixteen);
            HashMap<String,Integer> sixteenEighteen = getDifferenceMap(fourteen, eighteen);

            //creates two sets of municipalities, initially containing
            //all municipalities listed in all years
            //municipalities will be REMOVED if for example, a municipality
            //in didNotLose was found to have lost precincts
            HashSet<String> didNotLose = new HashSet<>();
            didNotLose.addAll(twelveFourteen.keySet());
            didNotLose.addAll(fourteenSixteen.keySet());
            didNotLose.addAll(sixteenEighteen.keySet());
            HashSet<String> didNotGain = new HashSet<>();
            didNotGain.addAll(twelveFourteen.keySet());
            didNotGain.addAll(fourteenSixteen.keySet());
            didNotGain.addAll(sixteenEighteen.keySet());

            //remove Strings from the set of municipalites that shrunk or grew
            updateLossGain(twelveFourteen,  didNotGain, didNotLose);
            updateLossGain(fourteenSixteen, didNotGain, didNotLose);
            updateLossGain(sixteenEighteen, didNotGain, didNotLose);

            //display results for the user
            System.out.println("\nMunicipalities which did not lose precincts\n"
                              +"___________________________________________");
            for (String municipality : didNotLose){
                System.out.println(municipality);
            }
            System.out.println("\n\nMunicipalities which did not gain precincts\n"
                              +"___________________________________________");
            for (String municipality : didNotGain){
                System.out.println(municipality);
            }

        } catch (SQLException e) {
            System.err.println("*** SQLException:  "
                + "Could not fetch query D results.");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);
        }
    }

    /**updateLossGain
     * 
     * a helper function, called by queryD
     * updates the sets of municipalites that did not gain precincts
     * and set of municipalities that did not lose precincts
     * 
     * @param difference a map from String to Integer of the difference in municipalities
     * between two given years
     * @param didNotGain a set of Strings, if a key from differences maps to
     * a number > 0, that key is removed from didNotGain
     * @param didNotLose a set of Strings, if a key from differences maps to
     * a number < 0, that key is removed from did not lose
     */
    static void updateLossGain(HashMap<String, Integer> difference,
                               HashSet<String> didNotGain,
                               HashSet<String> didNotLose){
        for (String municipality : difference.keySet()){
            
            //remove municipalites that do not belong in the sets
            if (difference.get(municipality) > 0){
                didNotGain.remove(municipality);
            }if(difference.get(municipality) < 0){
                didNotLose.remove(municipality);
            }
        }
    }

    /**getDifferenceMap
     * 
     * creates a map of the difference between the instance count of a municipality name
     * from a precincts list from one year, and a different year
     * 
     * @param oldPrecinctCount a map of municipalites to the number of times they appeared
     * in a list of precincts (list should be of a year < the year of newPrecinctCount)
     * @param newPrecinctCount a map of municipalites to the number of times they appeared
     * in a list of precincts (list should be of a year > the year of oldPrecinctCount)
     * @return a Map of strings to Integers, names of municipalities mapped to the difference
     * in the number of precincts between two years (new-old)
     */
    public static HashMap<String, Integer> getDifferenceMap(HashMap<String,Integer> oldPrecinctCount,
                                                            HashMap<String,Integer> newPrecinctCount){
        //get key sets for maps a and b
        //union the sets
        HashSet<String> municipalities = new HashSet<>();
        municipalities.addAll(oldPrecinctCount.keySet());
        municipalities.addAll(newPrecinctCount.keySet());

        //iterate over the merged set
        //create a map of differences
        HashMap<String, Integer> difference = new HashMap<>();
        for (String municipality : municipalities){
            Integer oldCount = oldPrecinctCount.get(municipality); 
            Integer newCount = newPrecinctCount.get(municipality);

            //if a municipality is in one set but not both
            //change its null count value to 0;
            oldCount = oldCount == null ? 0 : oldCount;
            newCount = newCount == null ? 0 : newCount;

            difference.put(municipality, newCount-oldCount);
        }

        return difference;
    }


    /**getCountMap
     * 
     * counts the number of duplicates of String values in a column
     * 
     * @param year the .extension to the database for a given year
     * ie: username.twelve
     * @param fieldName the title of the column in which to tally periodicity of a field entry
     * @param dbconn a Connection to an oracle database containing the records of precincts
     * @return a HashMap, with counts of how many times each string in a column repeats
     * @throws SQLException
     */
    public static HashMap<String,Integer> getCountMap(String year, String fieldName,
        Connection dbconn) throws SQLException {
        
        Statement stmt = null;
        ResultSet answer = null;

        String query = "SELECT "+fieldName+" from laser."+year;

        HashMap<String,Integer> count = new HashMap<>();

        stmt = dbconn.createStatement();
        answer = stmt.executeQuery(query);

        while(answer.next()){
            String field = answer.getString(fieldName);
            if (field == null){
                continue;
            }

            //remove leading and trailing whitespace
            field =field.trim();

            //first time? add to hashMap and list
            if (!count.containsKey(field)){
                count.put(field, 1);
            }else{
                //increment curr count
                Integer curr = count.get(field);
                count.replace(field, curr, curr+1);
            }
        }

        stmt.close();
        return count;
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