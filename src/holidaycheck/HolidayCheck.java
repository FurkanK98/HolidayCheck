
package holidaycheck;

import java.io.Serializable;
import java.net.*;
import java.rmi.*;
import java.sql.Date;
import java.text.*;
import java.util.*;

public class HolidayCheck implements Serializable {
    private static HolidayIO webdev;
    private static Mitarbeiter eingeloggterMitarbeiter; // Speichert den aktuell angemeldeten Benutzer. Wird im späteren Verlauf benutzt, um z.B. zu prüfen, ob er ein Chef ist.
    private static Scanner scanner = new Scanner(System.in); // Wird benutzt, um die Eingaben vom Benutzer zu bekommen.
    
    public static void main(String[] args) throws RemoteException, MalformedURLException, AlreadyBoundException, NotBoundException {
        webdev = (HolidayIO) Naming.lookup("rmi://localhost:1099/holidaycheck_server"); //RMI-Client
        
        System.out.println("Herzlich Willkommen bei MyCompanion!\n");
        login(); //Die Methode "Login" wird aufgerufen.

        boolean isActive = true;
        while (isActive) {
            System.out.println("");
            System.out.println("1- Urlaubstage anzeigen");
            System.out.println("2- Urlaub Beantragen");
            System.out.println("3- Meine Urlaubsanträge anzeigen");

            if (eingeloggterMitarbeiter.istChef) {
                System.out.println("4- Urlaubsanträge verwalten");
                System.out.println("5- Vertreter verwalten");
            }
            
            System.out.println("0- Beenden");

            System.out.print("\nEingabe: ");
            int input = scanner.nextInt();
            switch (input) {
                case 0: // Beendet das Programm.
                isActive = false;
                break;
                case 1: // Hier werden Urlaubstage angezeigt.
                int freieTage = webdev.getUrlaubsTageVonMitarbeiter(eingeloggterMitarbeiter.id);
                System.out.println("\nDu hast noch " + freieTage + " Urlaubstage");
                break;
                case 2:
                urlaubBeantragen();
                break;
                case 3:
                List<Urlaubsantrag> urlaubsAntragList = webdev.listUrlaubsAntraege(eingeloggterMitarbeiter.id);
                printUrlaubAntraege(urlaubsAntragList);
                break;
                case 4:
                if (eingeloggterMitarbeiter.istChef) {
                    urlaubsAntraegeVerwalten();
                } else {
                    System.out.println("Keine Berechtigung für diese Aktion");
                }
                break;
                case 5:
                if (eingeloggterMitarbeiter.istChef) {
                    verwalteVertreter();
                } else {
                    System.out.println("Keine Berechtigung für diese Aktion");
                }
            }
        }
        System.out.println("Auf Wiedersehen!");
    }
    
    
    
    //Wird für das Login benutzt. Username und Passwort wird an den User gefragt.
    private static void login() throws RemoteException {
        String nachname;
        String passwort;
        while (true) {
            System.out.println(" === Login === ");
            System.out.print("Nachname: ");
            nachname = scanner.nextLine();

            System.out.print("Passwort: ");
            passwort = scanner.nextLine();

            Mitarbeiter mitarbeiter = webdev.login(nachname, passwort); // Um auf die Datenbank zuzugreifen.

            if (mitarbeiter != null) {
                System.out.println("\nLogin erfolgreich!");
                System.out.println("\nWillkommen " + mitarbeiter.vorname + " " + mitarbeiter.nachname + "!");
                eingeloggterMitarbeiter = mitarbeiter;
                break;
            } else {
                System.out.println("Login Fehlgeschlagen! Nachname oder Passwort falsch");
                System.out.println("");
            }
        }
    }
    
    
    
    // Methode zur Urlaubsbeantragung. Fragt den User nach Urlaubsantragsinformationen.
    private static void urlaubBeantragen() throws RemoteException {
        System.out.println();
        System.out.println(" === Urlaubsantrag === ");
        System.out.print("Urlaubsdatum (Format YYYY-MM-DD): ");
        String start = scanner.next();
        System.out.print("Tage: ");
        int tage = scanner.nextInt();

        // Eine neues Urlaubsantrag-Objekt wird miterstellt.
        Urlaubsantrag urlaubsantrag = new Urlaubsantrag(getDateFromString(start), tage, eingeloggterMitarbeiter.id);
        webdev.urlaubBeantragen(eingeloggterMitarbeiter.id, urlaubsantrag);
    }
    
    
    
    // Geht die Lsite der Urlaubsanträge durch und gibt diese aus.
    private static void printUrlaubAntraege(List<Urlaubsantrag> urlaubsAntragList) {
        for (Urlaubsantrag urlaubsAntrag : urlaubsAntragList) {
        System.out.println(urlaubsAntrag);
        }
    }
    
    
    
    // Ist die Methode für den Vorgesetzten. Geht die Liste der Urlaubsanträge durch und kann diese entsprechend bestätigen/ablehnen.
    private static void urlaubsAntraegeVerwalten() throws RemoteException {
        System.out.println();
        System.out.println("=== Urlaubsanträge verwalten ===");
        List<Urlaubsantrag> urlaubsAntragList = webdev.listUrlaubsAntraege();
        printUrlaubAntraege(urlaubsAntragList);
        System.out.print("Urlaubsantrag ID zum bearbeiten: ");
        int id = scanner.nextInt();
        System.out.println("\nUrlaubsantrag:");
        System.out.println("(1) Bestätigen");
        System.out.println("(2) Ablehnen");
        System.out.println("(0) Abbrechen");

        System.out.print("\nEingabe: ");
        int inputStatus = scanner.nextInt();
        String status = "";
        switch (inputStatus) {
            case 0:
            return;
            case 1:
            status = "bestätigt";
            List<Urlaubsantrag> alleUrlaubsAntraege = webdev.listUrlaubsAntraege();
            Urlaubsantrag ausgewaehlterUrlaubsAntrag = null;
            for (Urlaubsantrag urlaubsAntrag : alleUrlaubsAntraege) {
                if (urlaubsAntrag.id == id) {
                    ausgewaehlterUrlaubsAntrag = urlaubsAntrag;
                }
            }
            int alteFreieTage = webdev.getUrlaubsTageVonMitarbeiter(ausgewaehlterUrlaubsAntrag.mitarbeiterId);
            webdev.updateFreieTage(id, alteFreieTage - ausgewaehlterUrlaubsAntrag.tage);
            break;
            case 2:
            status = "abgelehnt";
            break;
            default:
            System.out.println("Ungütlige Eingabe");
        }
        webdev.updateUrlaubsAntrag(id, status);
    }
    
    
    
    // Wird für den Vorgesetzten, zur Verwaltung von Vertretern benötigt.
    private static void verwalteVertreter() throws RemoteException {
        System.out.println();
        System.out.println("=== Vertreter Verwalten ===");
        System.out.println("(1) Vertreter erstellen");
        System.out.println("(2) Vertreter anzeigen");
        System.out.println("(3) Vertreter Status ändern");
        System.out.println("(0) Abbrechen");

        System.out.print("\nEingabe: ");
        int input = scanner.nextInt();
        switch (input) {
            case 0:
            break;
            case 1:
            vertreterErstellen();
            break;
            case 2:
            List<Vertreter> vertreterList = webdev.listVertreter();
            printVertreter(vertreterList);
            break;
            case 3:
            vertreterStatusAendern();
            break;
        }
    }
    
    
    
    // Wird vom Chef verwendet, um einen Vertreter zu erstellen.
    private static void vertreterErstellen() throws RemoteException {
        System.out.println();
        System.out.println("=== Vertreter erstellen ===");
        System.out.print("Vorname: ");
        String vorname = scanner.next();
        System.out.print("Nachname: ");
        String nachname = scanner.next();
        System.out.println("Status: ");
        System.out.println("(1) frei");
        System.out.println("(2) gesperrt");
        int inputStatus = scanner.nextInt();
        String status = null;
        switch (inputStatus) {
            case 1:
            status = "frei";
            break;
            case 2:
            status = "gesperrt";
            break;
        }
        Vertreter vertreter = new Vertreter(vorname, nachname, status);
        webdev.createVertreter(vertreter);
    }
    
    
    
    // Wird vom Chef benutzt, um den Status eines Vertreters zu ändern.
    private static void vertreterStatusAendern() throws RemoteException {
        System.out.println();
        System.out.println("=== Vertreter Status Ändern ===");
        printVertreter(webdev.listVertreter());
        System.out.print("Vertreter ID zum ändern wählen: ");
        int id = scanner.nextInt();
        System.out.println("Status: ");
        System.out.println("(1) frei");
        System.out.println("(2) gesperrt");

        System.out.print("\nEingabe: ");
        int inputStatus = scanner.nextInt();
        String status = null;
        switch (inputStatus) {
            case 1:
            status = "frei";
            break;
            case 2:
            status = "gesperrt";
            break;
        }
        webdev.updateVertreter(id, status);
    }
    
    
    
    // Die Vertreter-Liste wird aufgelistet.
    private static void printVertreter(List<Vertreter> vertreterList) {
        for (Vertreter vertreter : vertreterList) {
            System.out.println(vertreter);
        }
    }
    
    
    
    // Wird für den Urlaubsantrag benötigt. Datumeingabe vom User erfolgt in dieser Methode.
    private static Date getDateFromString(String start) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            java.util.Date date = format.parse(start);
            return new Date(date.getTime());
        } catch (ParseException e) {
            System.out.println("Falsches Datum-Format");
            return null;
        }
    }
}