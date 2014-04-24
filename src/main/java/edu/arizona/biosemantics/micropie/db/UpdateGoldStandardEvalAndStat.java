package edu.arizona.biosemantics.micropie.db;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;



public class UpdateGoldStandardEvalAndStat {

	public UpdateGoldStandardEvalAndStat() {
		// TODO Auto-generated constructor stub
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			// register driver
			con = DriverManager.getConnection("jdbc:mysql://localhost/new_schema?", "root","");//取得connection
			//jdbc:mysql://localhost/test?useUnicode=true&characterEncoding=Big5
			//hostname: localhost, database name: test
			//useUnicode=true&characterEncoding=Utf8
		}
		catch(ClassNotFoundException e)
		{
			System.out.println("DriverClassNotFound :"+e.toString());
		}// It is possible to encounter sqlexception situation 
		catch(SQLException x) {
			System.out.println("Exception :"+x.toString());
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// Just for testing
		
		UpdateGoldStandardEvalAndStat updateGoldStandardEvalAndStat = new UpdateGoldStandardEvalAndStat();
		//updateGoldStandardEvalAndStat.dropTable();		
		
		try {

			// InputStream inputStreamAAA = new FileInputStream("test.csv");
			// CSVReader readerAAA = new CSVReader(new BufferedReader(
			// 		new InputStreamReader(inputStreamAAA, "UTF8")));
			// List<String[]> linesReaderAAA = readerAAA.readAll();
			// System.out.println("linesReaderAAA.size()::" + linesReaderAAA.size());
			
			InputStream inputStreamGoldStandardEval = new FileInputStream("GoldStandardEval.csv");

			CSVReader readerGoldStandardEval = new CSVReader(new BufferedReader(
					new InputStreamReader(inputStreamGoldStandardEval, "UTF8")));
			List<String[]> linesReaderGoldStandardEval = readerGoldStandardEval.readAll();
			
			
			String tableName = "goldstandardeval";

			String insertdbSQL = "";

			// System.out.println("linesReaderGoldStandardEval.size()::" + linesReaderGoldStandardEval.size());
			
			for (int i = 1; i < linesReaderGoldStandardEval.size(); i++) {
				insertdbSQL = "insert into " + tableName + "(id,taxon_name,char_name,gold_standard,extracted_output,fvalue,precision_value,recall_value,extracted_date) " +
						"select ifNULL(max(id),0)+1";
				
				String[] lineReaderGoldStandardEval = linesReaderGoldStandardEval.get(i);
				// System.out.println("lineReaderGoldStandardEval.length::" + lineReaderGoldStandardEval.length);
				
				for (int j = 0; j < lineReaderGoldStandardEval.length; j++) {
					String cellValue = lineReaderGoldStandardEval[j];
					// System.out.println(cellValue);
					// example::insertdbSQL += ",?,?,?,?,?,?,?,?";
					insertdbSQL += ",\"" + cellValue + "\"";
				}
				insertdbSQL += " FROM " + tableName;
				if (lineReaderGoldStandardEval.length > 1) {
					System.out.println("insertdbSQL::" + insertdbSQL);
					updateGoldStandardEvalAndStat.insertTable(insertdbSQL);
				}

			}

			
			
			InputStream inputStreamGoldStandardEvalStat = new FileInputStream("GoldStandardEvalTotalStat.csv");

			CSVReader readerGoldStandardEvalStat = new CSVReader(new BufferedReader(
					new InputStreamReader(inputStreamGoldStandardEvalStat, "UTF8")));
			List<String[]> linesReaderGoldStandardEvalStat = readerGoldStandardEvalStat.readAll();
			
			
			tableName = "goldstandardevalstat";

			insertdbSQL = "";

			// System.out.println("linesReaderGoldStandardEvalStat.size()::" + linesReaderGoldStandardEvalStat.size());
			
			for (int i = 1; i < linesReaderGoldStandardEvalStat.size(); i++) {
				insertdbSQL = "insert into " + tableName + "(id,precision_value,recall_value,fvalue,extracted_date) " +
						"select ifNULL(max(id),0)+1";
				
				String[] lineReaderGoldStandardEvalStat = linesReaderGoldStandardEvalStat.get(i);
				// System.out.println("lineReaderGoldStandardEvalStat.length::" + lineReaderGoldStandardEvalStat.length);
				
				for (int j = 0; j < lineReaderGoldStandardEvalStat.length; j++) {
					String cellValue = lineReaderGoldStandardEvalStat[j];
					// System.out.println(cellValue);
					// example::insertdbSQL += ",?,?,?,?,?,?,?,?";
					insertdbSQL += ",\"" + cellValue + "\"";
				}
				insertdbSQL += " FROM " + tableName;
				if (lineReaderGoldStandardEvalStat.length > 1) {
					System.out.println("insertdbSQL::" + insertdbSQL);
					updateGoldStandardEvalAndStat.insertTable(insertdbSQL);
				}

			}
			
			//updateGoldStandardEvalAndStat.SelectTable();
			
			
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}	
	
	
	private Connection con = null; //Database objects
	//連接object
	private Statement stat = null;
	//執行,傳入之sql為完整字串
	private ResultSet rs = null;
	//結果集
	private PreparedStatement pst = null;
	//執行,傳入之sql為預儲之字申,需要傳入變數之位置
	//先利用?來做標示
	

	
	/*
	private String selectSQL = "select * from User ";
	*/


	//新增資料
	//可以看看PrepareStatement的使用方式
	public void insertTable(String insertdbSQL)
	{
		try
		{
			pst = con.prepareStatement(insertdbSQL);

			pst.executeUpdate();
		}
		catch(SQLException e)
		{
			System.out.println("InsertDB Exception :" + e.toString());
		}
		finally
		{
			Close();
		}
	}
	
	
	
	//刪除Table,
	//跟建立table很像
	
	public void dropTable(String dropdbSQL)
	{
		try
		{
			stat = con.createStatement();
			stat.executeUpdate(dropdbSQL);
		}
		catch(SQLException e)
		{
			System.out.println("DropDB Exception :" + e.toString());
		}
		finally
		{
			Close();
		}
	}
	
	
	//查詢資料
	//可以看看回傳結果集及取得資料方式
	public void SelectTable(String selectSQL)
	{
		try
		{
			stat = con.createStatement();
			rs = stat.executeQuery(selectSQL);
			System.out.println("ID\t\tName\t\tPASSWORD");
			while(rs.next())
			{
				System.out.println(rs.getInt("id")+"\t\t"+rs.getString("name")+"\t\t"+rs.getString("passwd"));
			}
		}
		catch(SQLException e)
		{
			System.out.println("DropDB Exception :" + e.toString());
		}
		finally
		{
			Close();
		}
	}
	
	//完整使用完資料庫後,記得要關閉所有Object
	//否則在等待Timeout時,可能會有Connection poor的狀況
	private void Close()
	{
		try
		{
			if(rs!=null)
			{
				rs.close();
				rs = null;
			}
			if(stat!=null)
			{
				stat.close();
				stat = null;
			}
			if(pst!=null)
			{
				pst.close();
				pst = null;
			}
		}
		catch(SQLException e)
		{
			System.out.println("Close Exception :" + e.toString());
		}
	}
	

	
	// Create table
	// Please take a look on Statement section
	public void createTable(String createdbSQL)
	{
		try
		{
			stat = con.createStatement();
			stat.executeUpdate(createdbSQL);
		}
		catch(SQLException e)
		{
			System.out.println("CreateDB Exception :" + e.toString());
		}
		finally
		{
			Close();
		}
	}


		
	
	
	
}








