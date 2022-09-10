import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MyUtils {
    
    private Connection connection;
    private Statement statement;
    private String schemaName;


    public Connection createConnection() throws SQLException {
        
        DriverManager.registerDriver(new org.h2.Driver());
        
        connection = DriverManager.getConnection("jdbc:h2:mem:test", "", "");
        
        return connection;
        
    }

    public void closeConnection() throws SQLException {
        
        if (connection != null && !connection.isClosed()) {
            
            connection.close();
            
        }
        
        connection = null;
    }

    public Statement createStatement() throws SQLException {
        
        if (statement == null || statement.isClosed()) {
            
            statement = connection.createStatement();
            
        }
        return statement = connection.createStatement();
    }

    public void closeStatement() throws SQLException {
        
        if (statement != null && !statement.isClosed()) {
            
            statement.close();
        }
        statement = null;
    }

    public void createSchema(String schemaName) throws SQLException {
        
        final String SQL = "CREATE SCHEMA IF NOT EXISTS %s;";
        
        this.schemaName = schemaName;
        
        statement.executeUpdate(String.format(SQL, this.schemaName));
        
    }
    public void useSchema() throws SQLException {
        
        final String SQL = "SET SCHEMA %s;";
        
        statement.executeUpdate(String.format(SQL, schemaName));
        
    }

    public void dropSchema() throws SQLException {
        
        final String SQL = "DROP SCHEMA IF EXISTS %s;";
        
        statement.executeUpdate(String.format(SQL, schemaName));
        
    }

     

    public void createTableRoles() throws SQLException {
        
        statement.executeUpdate(
            
                "CREATE TABLE Roles (" +
                        "id INT NOT NULL AUTO_INCREMENT, " +
                        "roleName VARCHAR(20) NOT NULL, " +
                        "PRIMARY KEY (id) );"
                        
        );
    }

    public void createTableDirections() throws SQLException {
        
        statement.executeUpdate(
            
                "CREATE TABLE Directions (" +
                        "id INT NOT NULL AUTO_INCREMENT, " +
                        "directionName VARCHAR(30) NOT NULL, " +
                        "PRIMARY KEY (id));"
                        
        );
    }

    public void createTableProjects() throws SQLException {
        
        statement.executeUpdate(
            
                "CREATE TABLE Projects (" +
                        "id INT NOT NULL AUTO_INCREMENT, " +
                        "projectName VARCHAR(30) NOT NULL, " +
                        "directionId INT, " +
                        "PRIMARY KEY (id), " +
                        "FOREIGN KEY (directionId) REFERENCES Directions (id));"
                        
        );
    }

    public void createTableEmployee() throws SQLException {
        
        statement.executeUpdate(
            
                "CREATE TABLE Employee (" +
                        "id INT NOT NULL AUTO_INCREMENT, " +
                        "firstName VARCHAR(30) NOT NULL, " +
                        "roleId INT, " +
                        "projectId INT, " +
                        "PRIMARY KEY (id), " +
                        "FOREIGN KEY (roleId) REFERENCES Roles (id), " +
                        "FOREIGN KEY (projectId) REFERENCES Projects (id));"
                        
        );
    }

    public void dropTable(String tableName) throws SQLException {
        
        final String SQL = "DROP TABLE IF EXISTS %s;";
        
        statement.executeUpdate(String.format(SQL, tableName));
        
    }

    public void insertTableRoles(String roleName) throws SQLException {
        
        final String SQL = "INSERT INTO Roles (roleName) VALUES (%s);";
        
        roleName = statement.enquoteLiteral(roleName);
        
        statement.executeUpdate(String.format(SQL, roleName));
        
    }

    public void insertTableDirections(String directionName) throws SQLException {
        
        final String SQL = "INSERT INTO Directions (directionName) VALUES (%s);";
        
        directionName = statement.enquoteLiteral(directionName);
        
        statement.executeUpdate(String.format(SQL, directionName));
        
    }

    public void insertTableProjects(String projectName, String directionName) throws SQLException {
        final String SQL = "INSERT INTO Projects (projectName, directionId) VALUES (%s, %d);";
        projectName = statement.enquoteLiteral(projectName);
        int directionId = getDirectionId(directionName);
        statement.executeUpdate(String.format(SQL, projectName, directionId));
    }

    public void insertTableEmployee(String firstName, String roleName, String projectName) throws SQLException {
        final String SQL = "INSERT INTO Employee (firstName, roleId, projectId) VALUES (%s, %d, %d);";
        firstName = statement.enquoteLiteral(firstName);
        int roleId = getRoleId(roleName);
        int projectId = getProjectId(projectName);
        statement.executeUpdate(String.format(SQL, firstName, roleId, projectId));
    }
    
    private int getId(String sql) throws SQLException {
        int result;
        try (ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                result = resultSet.getInt(1);
            } else {
                throw new SQLException("Trying to get non existing data: " + sql);
            }
        }
        return result;
    }

    public int getRoleId(String roleName) throws SQLException {
        final String SQL = "SELECT id FROM Roles WHERE roleName = %s;";
        roleName = statement.enquoteLiteral(roleName);
        return getId(String.format(SQL, roleName));
    }

    public int getDirectionId(String directionName) throws SQLException {
        final String SQL = "SELECT id FROM Directions WHERE directionName = %s;";
        directionName = statement.enquoteLiteral(directionName);
        return getId(String.format(SQL, directionName));
    }

    public int getProjectId(String projectName) throws SQLException {
        final String SQL = "SELECT id FROM Projects WHERE projectName = %s;";
        projectName = statement.enquoteLiteral(projectName);
        return getId(String.format(SQL, projectName));
    }

    public int getEmployeeId(String firstName) throws SQLException {
        final String SQL = "SELECT id FROM Employee WHERE firstName = %s;";
        firstName = statement.enquoteLiteral(firstName);
        return getId(String.format(SQL, firstName));
    }

    public List<String> getAllRoles() throws SQLException {
        final String SQL = "SELECT roleName FROM Roles;";
        return getSingleColumnOfStrings(SQL);
    }

    public List<String> getAllDirestion() throws SQLException {
        final String SQL = "SELECT directionName FROM Directions;";
        return getSingleColumnOfStrings(SQL);
    }

    public List<String> getAllProjects() throws SQLException {
        final String SQL = "SELECT projectName FROM Projects;";
        return getSingleColumnOfStrings(SQL);
    }

    public List<String> getAllEmployee() throws SQLException {
        final String SQL = "SELECT firstName FROM Employee;";
        return getSingleColumnOfStrings(SQL);
    }

    public List<String> getAllDevelopers() throws SQLException {
        final String SQL = "SELECT firstName " +
                "FROM Employee " +
                "   INNER JOIN Roles ON Employee.roleId = Roles.id " +
                "WHERE Roles.roleName = 'Developer';";
        return getSingleColumnOfStrings(SQL);
    }

    public List<String> getAllJavaProjects() throws SQLException {
        final String SQL = "SELECT projectName " +
                "FROM Projects " +
                "   INNER JOIN Directions ON Projects.directionId = Directions.id " +
                "WHERE directionName = 'Java';";
        return getSingleColumnOfStrings(SQL);
    }
    
    public List<String> getAllJavaDevelopers() throws SQLException {
        final String SQL = "SELECT Employee.firstName " +
                "FROM Employee " +
                "WHERE roleId IN " +
                "   (SELECT id FROM Roles " +
                "    WHERE Roles.roleName = 'Developer') " +
                "AND projectId IN " +
                "   (SELECT Projects.id FROM Projects " +
                "       INNER JOIN Directions ON Projects.directionId = Directions.id " +
                "    WHERE Directions.directionName = 'Java');";
        return getSingleColumnOfStrings(SQL);
    }
     private List<String> getSingleColumnOfStrings(String sql) throws SQLException {
        
        List<String> result = new ArrayList<>();
        
        try (ResultSet resultSet = statement.executeQuery(sql)) {
            
            while (resultSet.next()) {
                
                result.add(resultSet.getString(1));
                
            }
        }
        return result;
    }
     

     
}
