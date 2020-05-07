import java.io.*;
import java.util.*;
import javax.swing.*;
import java.sql.*;

public class JavaMysql {
	static Connection conn;
	static Statement s;
	static ResultSet rs;

	public static void main(String[] args) {	
	String SqlCommando = new String();
	int result;
	String database_password= new String();
	String database_user= new String();
	String database_connection= new String();
	int maxIdCliente = 0;
	database_password = "senha123";
	database_user = "root";
	database_connection = "jdbc:mysql://localhost/hotel";
	
	try{ 	
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		conn =  DriverManager.getConnection(database_connection+"?user="+database_user+"&password="+database_password);
		System.out.println("passou");
	}			
	catch (Exception e){System.out.println("Server down, unable to make the connection. ");}						
	SqlCommando ="Select max(Numero_Cliente) as  Maximo from cliente;";
	try{ 
		s = conn.createStatement();
		rs = s.executeQuery(SqlCommando);
		while (rs.next())
		{
			maxIdCliente = rs.getInt("Maximo")+1;
		}
	 		
		SqlCommando = "Insert into Cliente (Numero_Cliente, Nome_Cliente, Tipo_Cliente) values (" + maxIdCliente + ",'novo nome', 'I');";
		result = new Integer(s.executeUpdate(SqlCommando));	
           		
		SqlCommando ="Select Numero_Cliente, Nome_Cliente From Cliente;";
		rs = s.executeQuery(SqlCommando);
		while (rs.next())
		{
			System.out.println(rs.getString("Nome_Cliente"));
			System.out.println(rs.getInt("Numero_Cliente"));
		}		                   
		s.close();
	}	
	catch (Exception e){System.out.println("Error quering  the database . " + e);}	
	
	}
}


