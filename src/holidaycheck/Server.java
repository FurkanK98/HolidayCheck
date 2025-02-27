
package holidaycheck;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Server {
    public static void main(String[] args) throws RemoteException, MalformedURLException, AlreadyBoundException, NotBoundException {
        java.rmi.registry.LocateRegistry.createRegistry(1099); //Registriert einen Port für die entfernten Methoden.
        Naming.bind("rmi://localhost:1099/holidaycheck_server", new HolidayImpl()); //Der Konstruktor wird an den RMI-Server gebunden.
    }
}