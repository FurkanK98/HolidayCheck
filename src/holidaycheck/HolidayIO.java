
package holidaycheck;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface HolidayIO extends Remote {
    // Alle Methodenköpfe von HolidayImpl als Übergangsstelle gelistet, um eine Mehrfachverarbeitung von Objekten liefern zu können.
    Mitarbeiter login(String nachname, String passwort) throws RemoteException;
    int getUrlaubsTageVonMitarbeiter(int id) throws RemoteException;
    List<Urlaubsantrag> listUrlaubsAntraege() throws RemoteException;
    List<Urlaubsantrag> listUrlaubsAntraege(int id) throws RemoteException;
    Urlaubsantrag urlaubBeantragen(int id, Urlaubsantrag urlaubsAntrag)throws RemoteException;
    void updateFreieTage(int id, int tage) throws RemoteException;
    List<Vertreter> listVertreter() throws RemoteException;
    void updateUrlaubsAntrag(int id, String status) throws RemoteException;
    void createVertreter(Vertreter vertreter) throws RemoteException;
    void updateVertreter(int id, String status) throws RemoteException;
}