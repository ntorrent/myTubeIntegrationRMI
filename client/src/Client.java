
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.ClientInterface;
import common.Item;
import common.ServerData;
import common.TubeInterface;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;
import java.util.*;

public class Client {
    public static void main(String[] args) {
        String host = (args.length < 1) ? null : args[0];
        Boolean exit = Boolean.FALSE;

        try {

            System.out.println("enter port number for server rmi");
            Scanner in = new Scanner(System.in);
            int i = in.nextInt();
            Registry registry = LocateRegistry.getRegistry(i);
            TubeInterface my_tube = (TubeInterface) registry.lookup("TheTube");
            ClientInterface my_client = new ClientImplementation();
            if (my_tube.register(my_client)) {
                System.out.println("success");

                int owner = 0;
                /*
                Random rand = new Random();
                owner = rand.nextInt(100)+1;
                owner = 8;
                System.out.println("this user's id: " +owner);
                 */
                String method_option;
                while (!exit) {
                    method_option = getMethodOption(0);
                    switch (method_option) {
                        case "R":
                            owner = register(my_tube);
                            exit = true;
                            break;
                        case "L":
                            owner  = login(my_tube);
                            exit = true;
                            break;
                        default:
                            System.out.println("Option not correct. Try it again.");
                    }
                }
                exit = false;
                while (!exit) {
                    method_option = getMethodOption(1);
                    switch (method_option) {
                        case "U":
                            uploadFile(getFileName(), getFileDescrip(), getFilePath(), my_tube, owner);
                            break;
                        case "S":
                            globalSearchFileByName(getNameToSearch(), my_tube);
                            break;
                        case "D":
                            globalDownloadFileByName(getNameToSearch(), my_tube, my_client);
                            break;
                        case "M":
                            updateFileinDb(my_tube, my_client, getNameToSearch(), owner);
                            break;
                        /*
                        case "R":
                            DeleteFile(my_tube,my_client,owner);
                            break;
                        */
                        case "E":
                            exit = Boolean.TRUE;

                            break;
                        default:
                            System.out.println("Option not correct. Try it again.");
                    }
                }

                System.exit(0);

            }

        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString()); e.printStackTrace();
        }

    }
    private static int register(TubeInterface my_tube ) throws ClassNotFoundException, RemoteException, SQLException {
        ArrayList<String> names = my_tube.getnames();
        System.out.println("Enter new name:");
        Scanner scanner = new Scanner(System.in);
        String new_name = scanner.nextLine();
        if (names != null){
            while (names.contains(new_name)){
                System.out.println("Name already taken. Please enter new name:");
                scanner = new Scanner(System.in);
                new_name = scanner.nextLine();
            }
        }

        System.out.println("Please enter password:");
        scanner = new Scanner(System.in);
        String new_pass = scanner.nextLine();
        int owner = my_tube.NewUser(new_name,new_pass);
        System.out.println("Registered and logged in");
        return owner;
    }

    private static int login(TubeInterface my_tube) throws ClassNotFoundException, IOException, SQLException {

        ArrayList<String> names = my_tube.getnames();
        System.out.println("Enter name:");
        Scanner scanner = new Scanner(System.in);
        String name = scanner.nextLine();

        System.out.println("Please enter password:");
        scanner = new Scanner(System.in);
        String pass = scanner.nextLine();


        Integer owner = my_tube.getUserIdLogin(name, pass);

        if(owner == -1) {
            System.out.println("Username or password incorrect");
            System.exit(1);
        }
        System.out.println("logged in");
        return owner;
    }

    private static void DeleteFile(TubeInterface my_tube, ClientInterface my_client, String search_name, Integer owner) throws SQLException, ClassNotFoundException, IOException {
        Item selected_item = my_tube.searchByOwner(search_name, owner, my_client);
        // call the RMIserver with the item id and my client_id (owner) to delete it

    }

    private static void updateFileinDb(TubeInterface my_tube, ClientInterface my_client, String search_name, Integer owner) throws IOException, SQLException, ClassNotFoundException {
        Item selected_item = my_tube.searchByOwner(search_name, owner, my_client);
        // change what you want in the item
        System.out.println("Insert a new name and description to the Item: ");
        my_tube.updatebyItem(selected_item, getFileName(), getFileDescrip());
    }

    private static void uploadFile(String name, String description, String pathToFile, TubeInterface my_tube,int owner) throws IOException, SQLException, ClassNotFoundException {
        File clientpathfile = new File(pathToFile);
        byte [] mydata=new byte[(int) clientpathfile.length()];
        FileInputStream in=new FileInputStream(clientpathfile);
        System.out.println("uploading to server...");
        in.read(mydata, 0, mydata.length);
        String file_name = pathToFile.substring(pathToFile.lastIndexOf("/") + 1); // get original name of the file, extension included
        //System.out.println(file_name);
        my_tube.uploadFile(mydata, name, description, file_name , owner);
    }
    /*
    private static void localSearchFileByName(String search_name, TubeInterface my_tube) throws IOException, SQLException, ClassNotFoundException {
        //System.out.println("here1");
        ArrayList<Item> items = (ArrayList<Item>) my_tube.searchLocal(search_name);
        //System.out.println("here2");
        // --------------------------------------------------------
        Integer option = 1;
        HashMap<Integer, Item> options_available = new HashMap<>();
        for (Item item : items) {
            options_available.put(option, item);
            option += 1;
        }
        // --------------------------------------------------------

        showItemsListToClient(options_available);
        //System.out.println("here3");
    }

     */

    private static void globalSearchFileByName(String search_name, TubeInterface my_tube) throws IOException, SQLException, ClassNotFoundException {

        ArrayList<Item> items = (ArrayList<Item>) my_tube.searchFile(search_name);
        // --------------------------------------------------------
        Integer option = 1;
        HashMap<Integer, Item> options_available = new HashMap<>();
        for (Item item : items) {
            options_available.put(option, item);
            option += 1;
        }
        // --------------------------------------------------------

        showItemsListToClient(options_available);

    }
/*
    private static void downloadFileByName(String search_name, TubeInterface my_tube, ClientInterface my_client) throws IOException, SQLException, ClassNotFoundException {

        ArrayList<Item> items = (ArrayList<Item>) my_tube.searchFile(search_name);

        // --------------------------------------------------------
        Integer option = 1;
        HashMap<Integer, Item> options_available = new HashMap<>();
        for (Item item : items) {
            options_available.put(option, item);
            option += 1;
        }
        // --------------------------------------------------------

        showItemsListToClient(options_available);


        byte[] downloaded_data = my_tube.downloadFile(search_name, my_client);

        String path = "./client_downloads/";
        File serverFile = new File( path + "some_name.jpg");
        FileOutputStream out = new FileOutputStream(serverFile);

        out.write(downloaded_data);
        out.flush();
        out.close();
    }
*/
    private static void globalDownloadFileByName(String search_name, TubeInterface my_tube, ClientInterface my_client) throws IOException, SQLException, ClassNotFoundException, NotBoundException {

        Item selected_item = my_tube.globalDownloadFileItem(search_name, my_client); // ALL OK
        ///TubeInterface target_server = selected_item.getTargetServerRef(); // UN ITEM NO TÉ TubeInterface al WS !! Si algun canvi gran hi ha, aquí està//TODO
        Integer target_server_id = selected_item.getServer_id();

        System.out.println(target_server_id);

        Integer target_server_port =  getServer(target_server_id).port;

        Registry registry = LocateRegistry.getRegistry(target_server_port);
        TubeInterface target_server = (TubeInterface) registry.lookup("TheTube");

        String item_mime_type = selected_item.getPath().substring(selected_item.getPath().lastIndexOf(".") + 1);
        String item_name = selected_item.getName();

        byte[] downloaded_data = target_server.downloadFileByItem(selected_item);

        String path = "./client_downloads/";
        //File serverFile = new File( path + "some_name.jpg");
        File serverFile = new File( path + getNameToSaveFile(item_name+"."+item_mime_type));
        FileOutputStream out = new FileOutputStream(serverFile);

        out.write(downloaded_data);
        out.flush();
        out.close();
        System.out.println("file downloaded");


        //System.out.println(target_server.hello());
    }

    private static ServerData getServer(Integer server_id) throws IOException {
        String output;
        String json_result = "";


        URL url = new URL("http://localhost:8080/myRESTwsWeb/rest/server/"+ server_id);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");

        if(conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        while((output = br.readLine()) != null) {
            System.out.println("\n Resultat json: " + output);
            json_result  = output;
        }
        conn.disconnect();


        ServerData server;
        Gson gson = new Gson();
        server = gson.fromJson(json_result, new TypeToken<ServerData>(){}.getType());

        //System.out.println(server);

        return server;
    }


    private static String getFileName() {
        String name;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Insert name of the file: ");
        name = scanner.nextLine();
        return name;
    }

    private static String getFileDescrip() {
        String description;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Insert description of the file: ");
        description = scanner.nextLine();
        return description;
    }

    private static String getFilePath() {
        String path_to_file;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Insert path to the file: ");
        path_to_file = scanner.nextLine();
        return path_to_file;
    }

    private static String getNameToSearch() {
        String name_to_search;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Insert the name of the file you want to search for: ");
        name_to_search = scanner.nextLine();

        // NEW!
        if (name_to_search.equals(""))
            name_to_search = "*";

        return name_to_search;
    }

    private static String getMethodOption(int i) {
        String option;
        Scanner scanner = new Scanner(System.in);
        if (i==1){
            System.out.println("Write: \n 'U' to upload \n 'S' to search \n 'D' to download \n 'M' to update \n 'R' to delete \n'E' to exit");
        }
        else{
            System.out.println("Write: \n 'R' to register \n 'L' to login ");
        }
        option = scanner.nextLine();
        return option.toUpperCase();
    }

    private static String getNameToSaveFile(String default_name) {
        String name, option;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Write 'Y ' if you want to save the file as '" + default_name + "'");
        option = scanner.nextLine();
        if (option.toUpperCase().equals("Y")) {
            return default_name;
        }
        System.out.println("Insert name in which you want to save the file: ");
        name = scanner.nextLine();
        String temp = default_name.substring(default_name.lastIndexOf("."));
        return name+temp;
    }

    public static void showItemsListToClient(HashMap<Integer, Item> options) {
        System.out.println("Select one of those options:");
        options.forEach((key,value) -> System.out.println(key + ": " + value.getName() + ",  " + value.getDescription()));
    }

}
