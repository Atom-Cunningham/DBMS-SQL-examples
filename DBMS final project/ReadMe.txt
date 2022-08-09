Running the JDBC:
All files concerning creating and populating the database are located in the DataGeneration directory. If you need to
recreate the datase login to oracle.lectura while you are in the DataGeneration directory and first run dropTables.sql
and then createTables.sql. At this point all necessary tables exist but are empty. To populate tables just run Populator.java
with your oracle username and password as command line arguments. Now that the database is populated and you can run 
Prog4.java in the root directory which takes your oracle username and password as command line arguments. Once you run 
Prog4.java all your queries will be handled through a text-based interface.

Chris & Moses: database generation and implementation of record changes and queries and general debugging
Julien: FD design, normalization, and E-R diagram
Adam: FD design, normalization, data generation, implementation of queries, and general debugging
