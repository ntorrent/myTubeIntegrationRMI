package common;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public interface CenterInterface  extends Remote {

    int register(TubeInterface new_server) throws RemoteException;
    List<Item> global_search(String search_key) throws ClassNotFoundException, RemoteException, SQLException;
    Item downloadFileItem(String search_key, ClientInterface client) throws SQLException, ClassNotFoundException, IOException;
    public int uploadFile(String name, String description, int owner,int server_id,String type) throws SQLException, ClassNotFoundException, RemoteException;
    void start_db() throws SQLException, ClassNotFoundException, RemoteException;
    Item searchByOwner(int owner, ClientInterface client) throws SQLException, RemoteException, ClassNotFoundException;
    List<Item> global_search_owner(int owner) throws ClassNotFoundException, RemoteException, SQLException;
    void deleteByKey(int key) throws SQLException, ClassNotFoundException, RemoteException;
    void updatebyKey(int key,String newname,String newdesc) throws SQLException, ClassNotFoundException, RemoteException;
    int NewUser(String name,String pass) throws SQLException, ClassNotFoundException, RemoteException;
    ArrayList<String> GetNames() throws SQLException, ClassNotFoundException, RemoteException;
    String getPass(String name) throws SQLException, ClassNotFoundException, RemoteException;
    int getId(String name) throws SQLException, ClassNotFoundException, RemoteException;

}
