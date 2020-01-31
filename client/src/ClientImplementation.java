import common.ClientInterface;
import common.Item;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class ClientImplementation extends UnicastRemoteObject implements ClientInterface {
    private int id ;

    protected ClientImplementation() throws RemoteException {
    }

    public void set_id(int i) throws RemoteException{
        id = i;
    }

    public int getId() throws RemoteException{
        return id;
    }

    @Override
    public Integer getUserOption() throws RemoteException{
        String user_input;
        Integer chosen_option;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Select an option:");
        user_input = scanner.nextLine();
        chosen_option = getOptionNumber(user_input);
        return chosen_option;
    }

    @Override
    public void showItemsListToClient(HashMap<Integer, Item> options_available) throws RemoteException{
        System.out.println("Select one of those options:");
        options_available.forEach((key,value) -> System.out.println(key + ": " + value.getName() + ",  " + value.getDescription()));
    }

    private Integer getOptionNumber(String option) throws RemoteException{
        try {
            Integer result;
            result = Integer.parseInt(option);
            return result;
        }
        catch (NumberFormatException e) {
            return -1;
        }
    }
}
