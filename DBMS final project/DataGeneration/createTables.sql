CREATE SEQUENCE supply_sequence start with 1;
CREATE SEQUENCE employee_sequence start with 1;
CREATE SEQUENCE member_sequence start with 1;
CREATE SEQUENCE supplier_sequence start with 1;


CREATE TABLE member(id int NOT NULL, firstName varchar(50) NOT NULL,
	lastName varchar(50) NOT NULL, dob char(15), address varchar(70), 
	phoneNumber varchar(50) NOT NULL, rewardPoints int NOT NULL, 
	active int NOT NULL, primary key(id));

CREATE TABLE employee(id int NOT NULL, firstName varchar(50) NOT NULL, 
	lastName varchar(50) NOT NULL, gender varchar(10) NOT NULL,
	address varchar(70), phoneNumber varchar(50), 
	groupType varchar(10) NOT NULL, salary int, primary key(id));


CREATE TABLE sale(id int NOT NULL, saleDate char(10) NOT NULL, 
	paymentMethod varchar(20) NOT NULL, totalPrice number NOT NULL,
	memberId int, primary key(id));

CREATE TABLE subsale(id int NOT NULL, saleID int NOT NULL,
	productId int NOT NULL, salePrice number NOT NULL, amount int NOT NULL,
	primary key(id));

CREATE TABLE supplier(id int NOT NULL, name varchar(50) NOT NULL, 
	address varchar(50), contactName varchar(50) NOT NULL, primary key(id));

CREATE TABLE supply(id int NOT NULL, supplierId int NOT NULL,
	productId int NOT NULL, incomingDate char(15) NOT NULL, 
	purchasePrice number NOT NULL, amount int NOT NULL, primary key(id));

CREATE TABLE product(id int NOT NULL, name varchar(50) NOT NULL,
	retailPrice number NOT NULL, category varchar(20) NOT NULL, 
	membershipDiscount number, quantity int NOT NULL, primary key(id));
  

CREATE OR REPLACE TRIGGER member_on_insert
  BEFORE INSERT ON member
  FOR EACH ROW
BEGIN
  SELECT member_sequence.nextval
  INTO :new.id
  FROM dual;
END;
/

CREATE OR REPLACE TRIGGER employee_on_insert
  BEFORE INSERT ON employee
  FOR EACH ROW
BEGIN
  SELECT employee_sequence.nextval
  INTO :new.id
  FROM dual;
END;
/

CREATE OR REPLACE TRIGGER supply_on_insert
  BEFORE INSERT ON supply
  FOR EACH ROW
BEGIN
  SELECT supply_sequence.nextval
  INTO :new.id
  FROM dual;
END;
/

CREATE OR REPLACE TRIGGER supplier_on_insert
  BEFORE INSERT ON supplier
  FOR EACH ROW
BEGIN
  SELECT supplier_sequence.nextval
  INTO :new.id
  FROM dual;
END;
/