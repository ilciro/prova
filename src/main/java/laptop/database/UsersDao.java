package laptop.database;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.LocalDate;

import java.util.logging.Level;
import java.util.logging.Logger;


import laptop.utilities.ConnToDb;
import laptop.model.TempUser;
import laptop.model.User;


public class UsersDao {


	private static String query;

	private static boolean state = false;
	private static final String ECCEZIONE = "errore in mysql :";
	private static int row = 0;
	private static final String TXT_FILE_NAME="src/main/resources/riepilogoUtenti.txt";
	private static final String TXT_FILE_NAME_WEB="src/main/webapp/WEB-INF/riepilogoUtenti.txt";

	private static final String UTENTI="utenti";
	private static final GenerateDaoReportClass gRC=new GenerateDaoReportClass(UTENTI);







    // use this function on controller after you had check the email
	// add an user on db after registration
	// prende i dati dall'oggetto che gli abbiamo passato 
	public static boolean createUser(User u) throws SQLException {


		LocalDate d = u.getDataDiNascita();
		u.setIdRuolo("U");

		query = "INSERT INTO `USERS`"
				+ "(`idRuolo`,"
				+ "`Nome`,"
				+ "`Cognome`,"
				+ "`Email`,"
				+ "`pwd`,"
				+ " `descrizione`,"
				+ "`DataNascita`)"
				+ "VALUES"
				+ "(?,?,?,?,?,?,?)";

		try (Connection conn = ConnToDb.connectionToDB();
			 PreparedStatement prepQ = conn.prepareStatement(query)) {


			prepQ.setString(1,User.getInstance().getIdRuolo().substring(0,1));
			prepQ.setString(2, User.getInstance().getNome());
			prepQ.setString(3, User.getInstance().getCognome());
			prepQ.setString(4, User.getInstance().getEmail());
			prepQ.setString(5, User.getInstance().getPassword());
			prepQ.setString(6, "utente aggiunto");
			prepQ.setDate(7,Date.valueOf(d));
			prepQ.executeUpdate();

			state = true;
		} catch (SQLException e) {
			Logger.getLogger("createUser").log(Level.INFO, ECCEZIONE, e);

		}


		return state;

	}



	//check User email if we found that we return true else we return false
	//Qui viene passato dal controller un oggetto di tipo user
	public static int checkUser(User u) throws SQLException {
		int status = 0;
		// ritorna int per motivi legati al controller
		// anche se lo tratto come boolean
		//levato pwd se no non aggiorna


		query = "SELECT idUser FROM USERS where Email =? and pwd=?";
		try (Connection conn = ConnToDb.connectionToDB();
			 PreparedStatement prepQ = conn.prepareStatement(query)) {

			prepQ.setString(1, u.getEmail());
			prepQ.setString(2, u.getPassword());
			ResultSet rs = prepQ.executeQuery();
			if (rs.next()) {
				int id = rs.getInt("idUser");
				if (id > 0)
					status = 1;

			}
		} catch (SQLException e) {
			Logger.getLogger("check user").log(Level.INFO, ECCEZIONE, e);

		}


		return status;
	}



	public static String getRuolo(User u) throws SQLException {

		String r = "";
		query = "SELECT idRuolo FROM USERS where Email = ?";
		try (Connection conn = ConnToDb.connectionToDB();
			 PreparedStatement prepQ = conn.prepareStatement(query)) {
			prepQ.setString(1, u.getEmail());

			ResultSet rs = prepQ.executeQuery();
			while (rs.next()) {
				r = rs.getString("idRuolo");

			}

		} catch (SQLException e) {
			Logger.getLogger("get ruolo user").log(Level.INFO, ECCEZIONE, e);

		}
		u.setIdRuolo(r);


		return r;

	}

	// this function check if you have changed password successfully 
	public static boolean checkResetpass(User u, String pwd, String email) throws SQLException {

		query = "Update USERS SET pwd = ?  where Email = ?";
		try (Connection conn = ConnToDb.connectionToDB();
			 PreparedStatement prepQ = conn.prepareStatement(query)) {


			prepQ.setString(1, pwd);
			prepQ.setString(2, email);
			row = prepQ.executeUpdate();
			if (row == 1)
				state = true;

		} catch (SQLException e) {
			Logger.getLogger("check reset pwd").log(Level.INFO, ECCEZIONE, e);

		}


		Logger.getLogger("Reset pwd").log(Level.INFO, "row affected{0}", u.getEmail());
		return state;
	}


	// delete a user from db  terzo caso d'uso

	public static boolean deleteUser(User user) throws SQLException {
		String email = user.getEmail();
		int id= user.getId();

		query = "DELETE FROM USERS WHERE Email = ? or idUser=?";
		try (Connection conn = ConnToDb.connectionToDB();
			 PreparedStatement prepQ = conn.prepareStatement(query)) {


			prepQ.setString(1, email);
			prepQ.setInt(2, id);
			row = prepQ.executeUpdate();
			if (row == 1)
				state = true;

		} catch (SQLException e) {
			Logger.getLogger("delete user").log(Level.INFO, ECCEZIONE, e);

		}

		gRC.ripristinaOrdine(UTENTI);
		Logger.getLogger("delete user ruolo").log(Level.INFO, "cancello user id{0}", id);


		return state;

	}

	public static boolean deleteTempUser(TempUser uT) throws SQLException {
		String email = uT.getEmailT();
		int id=uT.getId();


		query = "DELETE FROM USERS WHERE Email = ? or idUser=?";
		try (Connection conn = ConnToDb.connectionToDB();
			 PreparedStatement prepQ = conn.prepareStatement(query)) {

			prepQ.setString(1, email);
			prepQ.setInt(2,id);
			row = prepQ.executeUpdate();
			if (row == 1)
				state = true;

		} catch (SQLException e) {
			Logger.getLogger("delete user").log(Level.INFO, ECCEZIONE, e);

		}
		Logger.getLogger("delete user okr").log(Level.INFO, "user deleted ");

		return state;
	}

	// Con pickData prendo i dati dall'utente creato per il login
	// per poi restituirlo in un nuovo oggetto di tipo User
	// e poi il controller lo specializza nelle 4 classi 
	public static User pickData(User u) throws SQLException {


		query = "SELECT idRuolo,Nome,Cognome,Email,descrizione,dataNascita from USERS where Email=? or idUser=?";
		try (Connection conn = ConnToDb.connectionToDB();
			 PreparedStatement prepQ = conn.prepareStatement(query)) {
			prepQ.setString(1, u.getEmail());
			prepQ.setInt(2,u.getId());

			ResultSet rs = prepQ.executeQuery();
			while (rs.next()) {


				// setto i vari dati

				u.setIdRuolo(rs.getString("idRuolo"));
				u.setNome(rs.getString("Nome"));
				u.setCognome(rs.getString("Cognome"));
				u.setDescrizione(rs.getString("descrizione"));
				u.setDataDiNascita(rs.getDate("dataNascita").toLocalDate());


			}
		} catch (SQLException e) {
			Logger.getLogger("pick data exception ").log(Level.SEVERE, "errore nel pick data {0}", e.toString());


		}


		// errore
		return u;
	}





	public static void getListaUtenti() throws IOException {



			Path path = Path.of(TXT_FILE_NAME);
			Path path1 = Path.of(TXT_FILE_NAME_WEB);
			gRC.checkFilePath(path);
			if(Boolean.TRUE.equals(gRC.generateReport(UTENTI)))
				gRC.checkFilePath(path1);
			Files.copy(path, path1, StandardCopyOption.REPLACE_EXISTING);



}



	public static TempUser getTempUserSingolo(TempUser uT) throws SQLException {


		query = "SELECT * FROM USERS where idUser = ?";
		try (Connection conn = ConnToDb.connectionToDB();
			 PreparedStatement prepQ = conn.prepareStatement(query)) {

			prepQ.setInt(1, uT.getId());

			ResultSet rs = prepQ.executeQuery();
			while (rs.next()) {

				uT.setIdRuolo(rs.getString(2));
				uT.setNomeT(rs.getString(3));
				uT.setCognomeT(rs.getString(4));
				uT.setEmailT(rs.getString(5));
				uT.setPasswordT(rs.getString(6));
				uT.setDescrizioneT(rs.getString(7));
				uT.setDataDiNascitaT(rs.getDate(8).toLocalDate());


			}
		} catch (SQLException e) {
			Logger.getLogger("get single temp user").log(Level.INFO, ECCEZIONE, e);

		}

		return uT;
	}

	public static User aggiornaUtente(User u,String vecchiaEmail)  {


		query = "UPDATE USERS set idRuolo=? , Nome=? , Cognome=? , Email=? , pwd=? , descrizione=? , dataNascita=? where idUser=? or Email=?";


		try (Connection conn = ConnToDb.connectionToDB();
			 PreparedStatement prepQ = conn.prepareStatement(query)) {


			// setto i vari dati


			prepQ.setString(1, u.getIdRuolo().substring(0, 1));
			prepQ.setString(2, u.getNome());
			prepQ.setString(3, u.getCognome());
			prepQ.setString(4, u.getEmail());
			prepQ.setString(5, u.getPassword());
			prepQ.setString(6, u.getDescrizione());
			prepQ.setString(7, u.getDataDiNascita().toString());
			prepQ.setInt(8, u.getId());
			prepQ.setString(9,vecchiaEmail);


			prepQ.executeUpdate();

		} catch (SQLException e) {
			Logger.getLogger("aggiorna utente").log(Level.INFO, ECCEZIONE, e);

		}

		return u;
	}


	private UsersDao() {
	}

	public static String getUserList() throws SQLException {
		query = "select * from USERS";
		StringBuilder s = new StringBuilder();
		try (Connection conn = ConnToDb.connectionToDB();
			 PreparedStatement prepQ = conn.prepareStatement(query)) {
			ResultSet rs = prepQ.executeQuery();
			while (rs.next()) {
				s.append("\n Id User \t");
				s.append("Id Ruolo \t");
				s.append("Nome \t");
				s.append("Cognome \t");
				s.append("Email \t");
				s.append("Data di nascita \n");

				s.append(rs.getInt(1));
				s.append("\t");
				s.append(rs.getInt(1));
				s.append("\t");
				s.append(rs.getString(2));
				s.append("\t");
				s.append(rs.getString(3));
				s.append("\t");
				s.append(rs.getString(4));
				s.append("\t");
				s.append(rs.getString(5));
				s.append("\t");
				s.append(rs.getDate(8).toLocalDate());
				s.append("\n");
			}
		} catch (SQLException e) {
			Logger.getLogger("user list").log(Level.INFO, "user list {0}.", e.toString());

		}
		return s.toString();
	}

	public static int aggiornaTempUser(TempUser uT) throws SQLException {

		int row = 0;

		query = "UPDATE USERS set idRuolo=? , Nome=? , Cognome=? , Email=? , pwd=? , descrizione=? , DataNascita=? where idUser=?";


		try (Connection conn = ConnToDb.connectionToDB();
			 PreparedStatement prepQ = conn.prepareStatement(query)) {


			// setto i vari dati


			prepQ.setString(1, uT.getIdRuolo().substring(0, 1));
			prepQ.setString(2, uT.getNomeT());
			prepQ.setString(3, uT.getCognomeT());
			prepQ.setString(4, uT.getEmailT());
			prepQ.setString(5, uT.getPasswordT());
			prepQ.setString(6, uT.getDescrizioneT());
			prepQ.setString(7, uT.getDataDiNascitaT().toString());
			prepQ.setInt(8, uT.getId());


			row=prepQ.executeUpdate();

		} catch (SQLException e) {
			Logger.getLogger("aggiorna utente").log(Level.INFO, ECCEZIONE, e);

		}

		return row;
	}


    public static int getIdMax() {
		return gRC.getIdMax(UTENTI);
    }
}

