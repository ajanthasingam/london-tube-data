#include <iostream>
#include <mysql/mysql.h>
#include <stdio.h>
#include <stdlib.h>
#include <jsoncpp/json/value.h>
#include <fstream>
 using namespace std;

 struct station{
  string name;
  string id;
  float longitude;
  float latitude;
 };

 struct line{
  string name;
  int id;
 };

struct connection_details
{
    char *server;
    char *user;
    char *password;
    char *database;
};

MYSQL* mysql_connection_setup(struct connection_details mysql_details)
{
     // first of all create a mysql instance and initialize the variables within
    MYSQL *connection = mysql_init(NULL);

    // connect to the database with the details attached.
    if (!mysql_real_connect(connection,mysql_details.server, mysql_details.user, mysql_details.password, mysql_details.database, 0, NULL, 0)) {
      printf("Conection error : %s\n", mysql_error(connection));
      exit(1);
    }
    return connection;
}


void process_json( MYSQL *conn){
ifstream train_file("../train-network.json", std::ifstream::binary);
//train_file >> info;
 MYSQL_STMT *stmt;
 //ulonglong  affected_rows;
// int        rc;

MYSQL_STMT* const statement = mysql_stmt_init(conn);
std::string const query = "CREATE TABLE station;";
mysql_stmt_prepare(statement, query.c_str()     , query.size());
//MYSQL_BIND bind[1] = {};
//bind[0].buffer_type = MYSQL_TYPE_STRING;
//bind[0].buffer = json_example.c_str();
//bind[0].buffer_length = json_example.size();
//mysql_stmt_bind_param(statement, bind);
mysql_stmt_execute(statement);
statement->




}
int main()
{
    MYSQL *conn;
  MYSQL_RES *res;
  MYSQL_ROW row;

  struct connection_details mysqlD;
  mysqlD.server = "localhost";
  mysqlD.user = "root";
  mysqlD.password = "password";
  mysqlD.database = "train_stations";

  // connect to the mysql database
  conn = mysql_connection_setup(mysqlD);

  // assign the results return to the MYSQL_RES pointer
 // int stmt = mysql_stmt_execute("CREATE TABLE station");
 //process_json(conn);
 res = mysql_list_dbs(conn, "show databases");
 string const query = "CREATE TABLE station";
mysql_query(conn, query.c_str());

  printf("MySQL Tables in mysql database:\n");
  while ((row = mysql_fetch_row(res)) !=NULL)
      printf("%s\n", row[0]);

  /* clean up the database result set */
  mysql_free_result(res);
  /* clean up the database link */
  mysql_close(conn);

  return 0;
}
