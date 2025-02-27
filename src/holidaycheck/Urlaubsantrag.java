
package holidaycheck;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class Urlaubsantrag implements java.io.Serializable {
    int id;
    Date start;
    int tage;
    String status; // bestätigt, abgelehnt, beantragt
    int mitarbeiterId; // Die ID vom Mitarbeiter, der den Urlaubsantrag gestellt hat

    /* Konstruktur um Urlaubsantrag Objekt zu erstellen. Der Status wird Standardmäßig auf beantragt gesetzt & wird vom Controller benutzt. */
    public Urlaubsantrag(Date startDatum, int tage, int mitarbeiterId) {
        this.start = startDatum;
        this.tage = tage;
        this.mitarbeiterId = mitarbeiterId;
        status = "Beantragt";
    }

    /* Konstruktur um Urlaubsantrag Objekt zu erstellen & wird von der Datenbank benutzt */
    public Urlaubsantrag(int id, Date start, int tage, String status, int mitarbeiterId) {
        this.id = id;
        this.start = start;
        this.tage = tage;
        this.status = status;
        this.mitarbeiterId = mitarbeiterId;
    }

    @Override
    public String toString() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return "[" + id + "] Urlaubsdatum: " + format.format(start) + ", Tage: " + tage + ", Status: " + status + ", MitarbeiterID: " + mitarbeiterId;
    }
}
