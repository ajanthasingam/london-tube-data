import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Iterator;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Test {

    Connection conn = null;
    Statement stmt = null;

    public static void main(String[] args) {
        Test test = new Test();
        test.jdbc_init();
        test.create_tables();
        test.process_json();

        while(true) {

            Scanner myObj = new Scanner(System.in);  // Create a Scanner object

            System.out.println("Enter the number to select your query \n" +
                    "\t 1. Find the lines run through a station \n " +
                    "\t 2. Find the station names in a line");
            try {
                int option = myObj.nextInt();
                myObj.nextLine();

                if (option == 1){
                    System.out.println("Enter the station name :");
                    String stationName = myObj.nextLine();
                    test.print_lines(stationName);


                } else {
                    if (option == 2){
                        System.out.println("Enter the line name :");
                        String lineName = myObj.nextLine();
                        test.print_stations(lineName);


                    }
                    else{
                        System.out.println("Invalid Option");
                        break;
                    }
                }
            } catch (Exception ex){
                System.out.println("Invalid Option");
                break;
            }



        }

    }

    public void jdbc_init(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.conn = DriverManager.getConnection("jdbc:mysql://localhost/train_stations?" +
                    "user=root&password=password");
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void process_json(){
        JSONParser parser = new JSONParser();
        try {
            Object file_obj = parser.parse(new FileReader("../train-network.json"));

            JSONObject jsonObject =  (JSONObject) file_obj;

            // loop array
            JSONArray stations = (JSONArray) jsonObject.get("stations");
            Iterator<JSONObject> iterator = stations.iterator();
            while (iterator.hasNext()) {
                JSONObject station = iterator.next();
                this.add_station(station.get("id").toString(),station.get("name").toString(), station.get("longitude").toString(),
                        station.get("latitude").toString());
            }

            // loop array
            JSONArray lines = (JSONArray) jsonObject.get("lines");
            iterator = lines.iterator();
            int counter =0;
            while (iterator.hasNext()) {
                JSONObject line = iterator.next();
                this.add_line(counter,line.get("name").toString());
                JSONArray line_stations = (JSONArray) line.get("stations");
                Iterator<String> station_iterator = line_stations.iterator();
                while (station_iterator.hasNext()) {
                    this.add_station_line(station_iterator.next(),counter);
                }
                counter = counter +1;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void create_tables(){
        try {
            // The newInstance() call is a work around for some
            // broken Java implementations

            this.stmt = this.conn.createStatement();
            this.stmt.execute("CREATE TABLE IF NOT EXISTS station(" +
                    "id varchar(255) not NULL," +
                    "name varchar(255),"+
                    "longitude double," +
                    "latitude double," +
                    "PRIMARY KEY (id));");

            this.stmt.execute("CREATE TABLE IF NOT EXISTS line(" +
                    "id int not NULL," +
                    "name varchar(255),"+
                    "PRIMARY KEY (id));");

            this.stmt.execute("CREATE TABLE IF NOT EXISTS station_line(" +
                    "stationId varchar(255) not NULL," +
                    "lineId int not null,"+
                    "PRIMARY KEY (stationId,lineId)," +
                    "FOREIGN KEY (stationId) REFERENCES station(id)," +
                    "FOREIGN KEY (lineId) REFERENCES line(id));");



        }catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }
    public void add_station(String id, String name, String longitude, String latitude){

       try{
           PreparedStatement ps = conn.prepareStatement("INSERT INTO station values (?, ?,?,?)");
           ps.setString(1, id);
           ps.setString(2, name);
           ps.setDouble(3,Double.parseDouble(longitude));
           ps.setDouble(4,Double.parseDouble(latitude));
           ps.executeUpdate();
        }catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }

    }

    public void add_line(int id,String name){

        try{
            PreparedStatement ps = conn.prepareStatement("INSERT INTO line values (?, ?)");
            ps.setInt(1, id);
            ps.setString(2, name);
            ps.executeUpdate();

        }catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }

    }
    public void add_station_line(String station_id, int line_id){

        try{
            PreparedStatement ps = conn.prepareStatement("INSERT INTO station_line values (?, ?)");
            ps.setString(1, station_id);
            ps.setInt(2, line_id);
            ps.executeUpdate();

        }catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }

    }

    public void print_lines(String station_name){

        try{
            PreparedStatement ps = conn.prepareStatement("SELECT name from line " +
                            "WHERE id in (" +
                                "SELECT lineId from station_line " +
                                "WHERE stationID in(" +
                                    "SELECT id from station " +
                                    "WHERE name=?" +
                                ")" +
                            ");");
            ps.setString(1, station_name);
            ResultSet rs = ps.executeQuery();
            System.out.println("Lines going through " + station_name +" are :");
            while(rs.next())
            {
                System.out.println(rs.getString(1)); //or rs.getString("column name");
            }
        }catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }

    }

    public void print_stations(String line_name){

        try{
            PreparedStatement ps = conn.prepareStatement("SELECT name from station " +
                    "WHERE id in (" +
                    "SELECT stationId from station_line " +
                    "WHERE lineID in(" +
                    "SELECT id from line " +
                    "WHERE name=?" +
                    ")" +
                    ");");
            ps.setString(1, line_name);
            ResultSet  rs= ps.executeQuery();
            System.out.println("Lines going through " + line_name +" are :");
            while(rs.next())
            {
                System.out.println(rs.getString(1)); //or rs.getString("column name");
            }
        }catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }

    }
}
