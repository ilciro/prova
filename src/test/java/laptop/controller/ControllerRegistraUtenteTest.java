package laptop.controller;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

class ControllerRegistraUtenteTest {
    private final ControllerRegistraUtente cRU=new ControllerRegistraUtente();
    private final ResourceBundle rbOggetto=ResourceBundle.getBundle("configurations/infoObjects");

    @Test
    void registra() throws SQLException {
        LocalDate date=LocalDate.of(1995,10,31);
        assertTrue(cRU.registra(rbOggetto.getString("nome"),rbOggetto.getString("cognome"),rbOggetto.getString("email"),rbOggetto.getString("pass"),rbOggetto.getString("pass"),date));
    }
}