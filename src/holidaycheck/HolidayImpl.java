
package holidaycheck;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.*;
import java.sql.Date;

public class HolidayImpl extends UnicastRemoteObject implements HolidayIO {
    
    public static final String dburl = "jdbc:mariadb://localhost:3306/urlaubsantrag";
    public static final String driver = "org.mariadb.jdbc.Driver";
    public static final String user = "root";
    public static final String password = "";
    public static Connection conn;
    
    // Treiberanbindung für die Datenbank.
    static {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    // Verbindung und Anmeldung mit der Datenbank.
    static {
        try {
            conn = DriverManager.getConnection(dburl, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    // Konstruktor der Klasse "HolidayImpl"
    public HolidayImpl() throws RemoteException {
    }



    // Kontrolliert die Zugangsdaten vom Benutzer, mit der Datenbank.
    @Override
    public Mitarbeiter login(String nachname, String passwort) {
        Mitarbeiter mitarbeiter;
        try {
            String sql = "SELECT * FROM mitarbeiter WHERE nachname=? AND passwort=?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, nachname);
            statement.setString(2, passwort);
            ResultSet resultSet = statement.executeQuery();
            mitarbeiter = getMitarbeiterVonResultSet(resultSet);
            } catch (SQLException e) {
                System.out.println("Login Fehlgeschlagen");
                return null;
            }
        return mitarbeiter; // Bei erfolgreichem Login wird der Mitarbeiter zurückgegeben, der sich eingeloggt hat.
    }



    // Selektiert die freien Tage eines Mitarbeiter, anhand seiner ID.
    @Override
    public int getUrlaubsTageVonMitarbeiter(int id) {
        int urlaubstage = -1;
        try {
            String sql = "SELECT freieTage FROM Mitarbeiter WHERE id=?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                urlaubstage = resultSet.getInt("freieTage");
                System.out.println("urlaubstage = " + urlaubstage);
            }
        } catch (SQLException e) {
            System.out.println("Datenbankanfrage Fehlgeschlagen!");
        }
        return urlaubstage; //Urlaubstage vom Mitarbeiter wird zurückgegeben.
    }



    // Gibt die Urlaubsanträge des entsprechenden Mitarbeiters zurück.
    @Override
    public List<Urlaubsantrag> listUrlaubsAntraege() {
        List<Urlaubsantrag> urlaubsAntragList = new ArrayList<Urlaubsantrag>();
        try {
            String sql = "SELECT * FROM Urlaubsantraege";
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                int id2 = resultSet.getInt("id");
                Date start = resultSet.getDate("start");
                int tage = resultSet.getInt("tage");
                String status = resultSet.getString("status");
                int mitarbeiterId = resultSet.getInt("mitarbeiterId");
                Urlaubsantrag urlaubsAntrag = new Urlaubsantrag(id2, start, tage, status, mitarbeiterId);
                urlaubsAntragList.add(urlaubsAntrag);
            }
        } catch (SQLException e) {
            System.out.println("Selektion der Vertreter fehlgeschlagen");
        }
        return urlaubsAntragList; //Die Liste der Urlaubsanträge von den Mitarbeitern wird zurückgegeben.
    }    



    // Listet alle Urlaubsanträge eines Mitarbeiters auf, die mit der ID spezifiziert sind.
    @Override
    public List<Urlaubsantrag> listUrlaubsAntraege(int id) {
        List<Urlaubsantrag> urlaubsAntragList = new ArrayList<Urlaubsantrag>();
        try {
            String sql = "SELECT * FROM Urlaubsantraege WHERE mitarbeiterId=?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id2 = resultSet.getInt("id");
                Date start = resultSet.getDate("start");
                int tage = resultSet.getInt("tage");
                String status = resultSet.getString("status");
                int mitarbeiterId = resultSet.getInt("mitarbeiterId");
                Urlaubsantrag urlaubsAntrag = new Urlaubsantrag(id2, start, tage, status, mitarbeiterId);
                urlaubsAntragList.add(urlaubsAntrag);
            }
        } catch (SQLException e) {
            System.out.println("Selektion der Vertreter fehlgeschlagen");
        }
        return urlaubsAntragList; //Die Liste der Urlaubsanträge von einem bestimmten Mitarbeiter wird zurückgegeben.
    }
    
    
    
    @Override
    public Urlaubsantrag urlaubBeantragen(int id, Urlaubsantrag urlaubsAntrag) throws RemoteException {
        // Es wird überprüft, ob der Mitarbeiter genug Urlaubstage hat.
        int freieTage = getUrlaubsTageVonMitarbeiter(id);
        if (urlaubsAntrag.tage > freieTage) {
            System.out.println("Zu wenig freie Tage um so einen langen Urlaub zu beantragen");
            System.out.println("Restliche Urlaubstage: " + freieTage);
            return null;
        }

        // Prüfung auf Vertreter.
        Vertreter verfuegbarerVertreter = null;
        List<Vertreter> vertreterList = listVertreter(); // Alle Vertreter aus der Datenbank holen.
        for (Vertreter vertreter : vertreterList) { // Durch Vertreter-Liste iterieren.
            if (vertreter.status.equals("frei")) { // Prüfen, ob ein Vertreter frei ist.
            verfuegbarerVertreter = vertreter;
            break; // Vertreter gefunden -> Iteration beenden.
            }
        }

        if (verfuegbarerVertreter != null) { // Vertreter ist verfügbar.
            System.out.println("Vertretung gefunden. Urlaub betätigt");
            updateVertreter(verfuegbarerVertreter.id, "gesperrt");
            urlaubsAntrag.status = "bestätigt";
            int alteFreieTage = getUrlaubsTageVonMitarbeiter(id);
            updateFreieTage(id, alteFreieTage - urlaubsAntrag.tage);
        } else { // Kein Vertreter Verfügbar, Urlaubsantrag wartet auf Bestätigung vom Chef.
            System.out.println("Keine Vertretung gefunden. Status wurde auf Beantragt gesetzt.");
            urlaubsAntrag.status = "beantragt";
        }

        try {
            String sql = "INSERT INTO Urlaubsantraege (start, tage, status, mitarbeiterId) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setDate(1, urlaubsAntrag.start);
            statement.setInt(2, urlaubsAntrag.tage);
            statement.setString(3, urlaubsAntrag.status);
            statement.setInt(4, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Urlaubsantrag's Stellung fehlgeschlagen");
            e.printStackTrace();
            return null;
        }
        System.out.println("Urlaub beantragt");
        return urlaubsAntrag;
    }



    // Aktualisiert die freien Tage des Mitarbeiters, anhand seiner ID.
    @Override
    public void updateFreieTage(int id, int tage) throws RemoteException {
        try {
            String sql = "UPDATE Mitarbeiter SET freieTage=? WHERE id=?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, tage);
            statement.setInt(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Urlaubstage Update fehlgeschlagen");
            return;
        }
        System.out.println("Urlaubstage aktualisiert. Verbleibende Urlaubstage: " + tage);
    }



    // Gibt die Liste aller Vertreter zurück.
    @Override
    public List<Vertreter> listVertreter() throws RemoteException {
        List<Vertreter> vertreterList = new ArrayList<Vertreter>();
        try {
            String sql = "SELECT * FROM Vertreter";
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String vorname = resultSet.getString("vorname");
            String nachname = resultSet.getString("nachname");
            String status = resultSet.getString("status");
            // Neuen Vertreter, anhand der Werte aus der Datenbank (ResultSet) erstellen.
            Vertreter vertreter = new Vertreter(id, vorname, nachname, status);
            vertreterList.add(vertreter); // Der Vertreter-Liste hinzufügen.
            }
        } catch (SQLException e) {
            System.out.println("Selektion der Vertreter fehlgeschlagen");
        }
        return vertreterList; //Gibt die Liste der Vertreter zurück.
    }



    // Aktualisierung (Update) der Urlaubsantrag-Status, anhand Urlaubsantrag-ID.
    @Override
    public void updateUrlaubsAntrag(int id, String status) throws RemoteException {
        try {
            String sql = "UPDATE Urlaubsantraege SET status=? WHERE id=?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, status);
            statement.setInt(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Urlaubsantrag Update fehlgeschlagen");
            return;
        }
        System.out.println("Urlaubsantrag Status wurde auf " + status + " geändert");
    }

    
    
    //Einfügen eines Vertreters.
    @Override
    public void createVertreter(Vertreter vertreter) throws RemoteException {
        try {
            String sql = "INSERT INTO Vertreter (Vorname, Nachname, Status) VALUES (?,?,?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, vertreter.vorname);
            statement.setString(2, vertreter.nachname);
            statement.setString(3, vertreter.status);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Vertreter Erstellung fehlgeschlagen");
            return;
        }
        System.out.println("Vertreter erfolgreich erstellt");
    }

    
    
    // Aktualisierung (Update) einer Vertreter-Status, anhand der Vertreter-ID.
    @Override
    public void updateVertreter(int id, String status) throws RemoteException {
        try {
            String sql = "UPDATE Vertreter SET status=? WHERE id=?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, status);
            statement.setInt(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Vertreter Status Update Fehlgeschlagen");
            return;
        }
        System.out.println("Vertreter Status auf " + status + " geändert");
    }



    // Holt aus dem ResultSet, die relevanten Daten heraus, um ein Mitarbeiter-Objekt zu erstellen.
    private Mitarbeiter getMitarbeiterVonResultSet(ResultSet resultSet) throws SQLException {
        Mitarbeiter mitarbeiter = null;
        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String vorname = resultSet.getString("vorname");
            String nachname = resultSet.getString("nachname");
            String passwort = resultSet.getString("passwort");
            int freieTage = resultSet.getInt("freieTage");
            boolean istChef = resultSet.getBoolean("istChef");
            mitarbeiter = new Mitarbeiter(id, vorname, nachname, passwort, freieTage, istChef);
        }
        return mitarbeiter;
    }    
}