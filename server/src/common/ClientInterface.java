package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.HashMap;

public interface ClientInterface extends Remote {
    Integer getUserOption() throws RemoteException, SQLException, ClassNotFoundException;
    void showItemsListToClient(HashMap<Integer, Item> options_available) throws RemoteException, SQLException, ClassNotFoundException;
    void set_id(int i) throws RemoteException;
    int getId() throws RemoteException;

}
