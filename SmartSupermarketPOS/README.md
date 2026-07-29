# Smart Supermarket POS & Self-Checkout System

This is a pure Java (Swing/AWT) desktop application with JDBC MySQL connectivity.

## Setup Instructions

1. **Database Setup**
   - Install MySQL Server if you haven't already.
   - Run the provided `schema.sql` script in your MySQL environment to create the `smartpos` database, tables, and insert dummy data.
   - **Important**: Open `src/main/java/com/smartpos/util/DatabaseConnection.java` and update the `URL`, `USER`, and `PASSWORD` to match your local MySQL credentials.

2. **Compiling and Running**
   - The project is structured as a Maven project (`pom.xml` is included). If you have Maven installed, you can simply run:
     ```bash
     mvn clean compile exec:java -Dexec.mainClass="com.smartpos.Main"
     ```
   - Alternatively, if you are using an IDE like IntelliJ IDEA or Eclipse:
     - Open the `SmartSupermarketPOS` folder as a project.
     - Add `mysql-connector-j` to your project dependencies (if not using Maven).
     - Run `com.smartpos.Main`.

3. **Login Credentials**
   - Admin Login: ID: `admin123`, Password: `admin` (Make sure the Admin toggle is selected)
   - Cashier Login: ID: `cashier01`, Password: `cashier` (Make sure the Cashier toggle is selected)
