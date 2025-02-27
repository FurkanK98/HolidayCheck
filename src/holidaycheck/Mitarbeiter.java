
package holidaycheck;

public class Mitarbeiter implements java.io.Serializable {
    int id;
    String vorname;
    String nachname; // Wird für den Login benötigt
    String passwort;
    int freieTage;
    String status; // verfügbar, abwesend
    boolean istChef;

    /* Konstruktur um Mitarbeiter-Objekt zu erzeugen & wird von der DB benutzt. */
    public Mitarbeiter(int id, String vorname, String nachname, String passwort, int freieTage, boolean istChef) {
        this.id = id;
        this.vorname = vorname;
        this.nachname = nachname;
        this.passwort = passwort;
        this.freieTage = freieTage;
        this.istChef = istChef;
    }
}
