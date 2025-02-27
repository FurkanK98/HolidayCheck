
package holidaycheck;

public class Vertreter implements java.io.Serializable {
    int id;
    String vorname;
    String nachname;
    String status; // gesperrt, frei

    // Wird vom Controller benutzt
    public Vertreter(String vorname, String nachname, String status) {
        this.vorname = vorname;
        this.nachname = nachname;
        this.status = status;
    }

    // Wird von der Datenbank benutzt
    public Vertreter(int id, String vorname, String nachname, String status) {
        this.id = id;
        this.vorname = vorname;
        this.nachname = nachname;
        this.status = status;
    }

    /* Wird beim System.out.println() aufgerufen, wenn ein Vertreter ausgegeben wird*/
    @Override
    public String toString() {
        return "[" + id + "] Name: " + vorname + " " + nachname + ", Status: " + status;
    }
}