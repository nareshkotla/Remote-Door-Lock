import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.security.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VMReceiverThread extends Thread{

	private Socket clientSocket;
	private BufferedReader input;
	private PrintWriter output;
	private boolean isOneTimeUser = false;

	public VMReceiverThread(Socket clientSocket) {
		this.clientSocket = clientSocket;
		try {
			this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private Boolean validateOneTimeUser(String usr, String pwd) {

		Connection c = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(pwd.getBytes(),0,pwd.length());

			String md5 = new BigInteger(1,m.digest()).toString(16);
			if ((md5.length() % 2) != 0) {
				md5 = "0" + md5;
			}
			System.out.println("md5 value : "+md5);

			int cnt = 0;
			boolean flag = true;
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:acngroup10");

			c.setAutoCommit(false);
			System.out.println("Opened acngroup10 database successfully");
			stmt = c.createStatement();

			rs = stmt.executeQuery("select count(*),* from OneTimeUser where username='"+ usr +"' and password='"+ md5 +"';");

			while(rs.next()){

				System.out.println("Count: "+cnt);
				System.out.println("One Time User ID: "+rs.getString("id"));
				System.out.println("One Time Username: "+rs.getString("username"));
				System.out.println("One Time Password: "+rs.getString("password"));
				System.out.println("Flag value: "+rs.getBoolean("flag"));
				cnt = Integer.parseInt(rs.getString(1));
				System.out.println("boolean value ------------------------"+rs.getString("flag"));
				flag = rs.getBoolean("flag");
			}
			System.out.println("One Time Records Retrieved successfully");

			rs.close();
			stmt.close();


			if(cnt == 1){

				if(!flag) {
		stmt.executeUpdate("update OneTimeUser set flag=1 where username='"+ usr +"' and password='"+ md5 +"';");
					System.out.println("Flag set for onetime user, he can no more login ... ");
					stmt.close();
					c.commit();
					c.close();

					return true;
				} else {
					System.out.println("Invalid user!!");
					stmt.close();
					c.commit();
					c.close();

					return false;
				}				
			}

			stmt.close();
			c.commit();
			c.close();

			return false;

		} 
		catch (Exception e ) 
		{
			System.err.println( e.getClass().getName	() + ": " + e.getMessage() );
			return false;
		}

	}

	private Boolean validateCredentials(String usr, String pwd) 
	{

		Connection c = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(pwd.getBytes(),0,pwd.length());
			String md5 = new BigInteger(1,m.digest()).toString(16);
			if ((md5.length() % 2) != 0) {
				md5 = "0" + md5;
			}
			System.out.println("md5 value : "+md5);
			int cnt = 0;

			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:acngroup10");

			c.setAutoCommit(false);
			System.out.println("Opened acngroup10 database successfully");
			stmt = c.createStatement();

			rs = stmt.executeQuery("select count(*),* from AndroidClients where username='"+ usr +"' and password='"+ md5 +"';");

			while(rs.next()){
				cnt = Integer.parseInt(rs.getString(1));
				System.out.println("Count:"+cnt);
				System.out.println("ID:"+rs.getString("id"));
				System.out.println("Username:"+rs.getString("username"));
				System.out.println("Password:"+rs.getString("password"));
			}
			System.out.println("Records Retrieved successfully");

			stmt.close();
			c.commit();
			c.close();
			if(cnt == 1){
				return true;
			}
			return false;

		} 
		catch (Exception e ) 
		{
			System.err.println( e.getClass().getName	() + ": " + e.getMessage() );
			return false;
		}


	}

	private String validateRaspberryPi(String usr, String usrType)
	{

		Connection c = null;
		Statement stmt = null;
		String serial_no = null;
		ResultSet rs = null;
		int cnt = 0;
		try 
		{
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:acngroup10");

			c.setAutoCommit(false);
			System.out.println("Opened acngroup10 database successfully for retrieving matching Raspberry IDs");
			stmt = c.createStatement();

			if(usrType.equals("1")) {
				rs = stmt.executeQuery("select count(*), r.serialno from onetimeuser a, raspberrypi r where a.serialno = r.serialno and a.username = '"+ usr + "';");
			} else {
				rs = stmt.executeQuery("select count(*), r.serialno from androidclients a, raspberrypi r, users u where a.id = u.userid"+ 
						" and u.piid = r.id and a.username = '"+ usr + "';");
			}

			while(rs.next()){
				cnt = Integer.parseInt(rs.getString(1));
				System.out.println("Count:"+cnt);
				serial_no = rs.getString(2);
				System.out.println("Raspberry Pi Serial #:"+ serial_no);

			}

			stmt.close();
			c.commit();
			c.close();
			if(cnt == 1){
				return serial_no;
			}

		} 
		catch (Exception e ) 
		{
			System.err.println( e.getClass().getName() + ": " + e.getMessage());
		}
		return null;

	}

	public int sqlQueryExecuteUpdate(String command) {

		Connection c = null;
		Statement stmt = null;
		int rowCount = 0;

		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:acngroup10");

			c.setAutoCommit(false);
			System.out.println("Opened acngroup10 database successfully");
			stmt = c.createStatement();

			rowCount = stmt.executeUpdate(command);

			stmt.close();
			c.commit();
			c.close();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}	

		return rowCount;
	}

	public ResultSet sqlQueryExecute(String command) {

		Connection c = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:acngroup10");

			c.setAutoCommit(false);
			System.out.println("Opened acngroup10 database successfully");
			stmt = c.createStatement();

			rs = stmt.executeQuery(command);

			while(rs.next()){
				System.out.println(""+rs.getInt(1));
			}

			stmt.close();
			c.commit();
			c.close();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}	

		return rs;
	}

	public synchronized void run() {

		try {
			while(true) {
				try {
					String read = this.input.readLine();				
					System.out.println("\nMessage Received : " + read.toString());

					String[] words = read.split(":"); 
					if(words[0].equals("android_validation")){

						if(words[3].equals("0")) {
							isOneTimeUser = false;

							this.output = new PrintWriter(VMConnection.androidClient.getOutputStream(), true);
							Boolean valid_user = validateCredentials(words[1], words[2]);

							if(valid_user){		
								this.output.println("PASS");
							}
							else{
								this.output.println("FAIL");
							}
						} else if(words[3].equals("1")) {

							isOneTimeUser = true;

							this.output = new PrintWriter(VMConnection.androidClient.getOutputStream(), true);
							Boolean valid_user = validateOneTimeUser(words[1], words[2]);

							if(valid_user){		
								this.output.println("PASS");
							}
							else{
								this.output.println("FAIL");
							}
						} else {
							this.output.println("FAIL");
						}

						break;
					}

					else if(words[0].equals("android")){

						String serialno = validateRaspberryPi(words[1], words[3]);

						if(words[2].equals("logout")) {
							this.output = new PrintWriter(VMConnection.raspberryPiClient.getOutputStream(), true);
							this.output.println("raspberrypi" + ":" + serialno + ":" + "lock");

							this.output = new PrintWriter(VMConnection.androidClient.getOutputStream(), true);

							System.out.println("Android client time out ... ");
							this.output.println("loggedout");
						} else {							

							this.output = new PrintWriter(VMConnection.raspberryPiClient.getOutputStream(), true);

							if(serialno != null)
							{
								if(words[2].equals("unlock")){
									System.out.println("Android client requests for unlock ... ");
									this.output.println("raspberrypi" + ":" + serialno + ":" + "unlock");

									if(words[3].equals("1")){

										DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
										Date date = new Date();
										int id = 0;

										Connection c = null;
										Statement stmt = null;
										ResultSet rs = null;

										try {
											Class.forName("org.sqlite.JDBC");
											c = DriverManager.getConnection("jdbc:sqlite:acngroup10");

											c.setAutoCommit(false);
											System.out.println("Opened acngroup10 database successfully");
											stmt = c.createStatement();

											rs = stmt.executeQuery("select id from onetimeuser where username='"+words[1]+"';");


											while(rs.next()){
												System.out.println("setting ID : "+rs.getInt("id"));
												id = rs.getInt("id");
											}

											stmt.close();
											c.commit();
											c.close();
											
											System.out.println("ID--"+id);
											sqlQueryExecuteUpdate("insert into onetimeuserlog (ID,LOCKSTATUS,TIMESTAMP) values ("+id+",'UNLOCK','"+dateFormat.format(date)+"');");

											

										} catch (ClassNotFoundException e) {
											e.printStackTrace();
										} catch (SQLException e) {
											e.printStackTrace();
										}	
									}									

								} else if(words[2].equals("lock")) {
									System.out.println("Android client requests for lock ... ");
									this.output.println("raspberrypi" + ":" + serialno + ":" + "lock");

									if(words[3].equals("1")){
										DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
										Date date = new Date();
										int id = 0;

										Connection c = null;
										Statement stmt = null;
										ResultSet rs = null;

										try {
											Class.forName("org.sqlite.JDBC");
											c = DriverManager.getConnection("jdbc:sqlite:acngroup10");

											c.setAutoCommit(false);
											System.out.println("Opened acngroup10 database successfully");
											stmt = c.createStatement();

											rs = stmt.executeQuery("select id from onetimeuser where username='"+words[1]+"';");


											while(rs.next()){
												System.out.println("setting ID : "+rs.getInt("id"));
												id = rs.getInt("id");
											}

											stmt.close();
											c.commit();
											c.close();
											
											System.out.println("ID--"+id);
											sqlQueryExecuteUpdate("insert into onetimeuserlog (ID,LOCKSTATUS,TIMESTAMP) values ("+id+",'LOCK','"+dateFormat.format(date)+"');");

											

										} catch (ClassNotFoundException e) {
											e.printStackTrace();
										} catch (SQLException e) {
											e.printStackTrace();
										}	
									}									
								} 
							} else {
								PrintWriter send_android = new PrintWriter(VMConnection.androidClient.getOutputStream(), true);
								send_android.println("android" + "Error1: No RaspberryPi linked with your UserId");
								send_android.close();
							}
						}			

						break;
					} 

					else if(read.contains("raspberrypi")) {						
						this.output = new PrintWriter(VMConnection.androidClient.getOutputStream(), true);
						if(read.contains("unlocked")) {
							System.out.println("raspberrypi unlocked the door successfully ... ");
							this.output.println("android" + " " + "unlocked");
						} else if(read.contains("locked")){
							if(read.contains("force")) {
								this.output = new PrintWriter(VMConnection.androidClientForPi.getOutputStream(), true);
								System.out.println("raspberrypi locked the door forcefully ... ");
								this.output.println("android" + " " + "locked");
							} else {
								System.out.println("raspberrypi locked the door successfully ... ");
								this.output.println("android" + " " + "locked");
							}							
						} else {
							System.out.println("Error2: RaspberryPi Serial Number is not matching");
							this.output.println("android" + " " + "Error2: RaspberryPi Serial Number is not matching");
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} 
	}

}
