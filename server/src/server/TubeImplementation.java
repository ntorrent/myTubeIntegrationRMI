package server;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

//import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import common.ClientInterface;
import common.Item;
import common.TubeInterface;
//import myRESTwsData.Item;

public class TubeImplementation extends UnicastRemoteObject implements TubeInterface {

    private int server_id;

    public void setServer_id(int server_id) {
        this.server_id = server_id;
    }

    protected TubeImplementation(Integer server_id) throws RemoteException {
        this.server_id = server_id;
        //local_db = new DataBase(db_id);
    }

    public boolean register(ClientInterface new_client) throws RemoteException {

        Random rand = new Random();
        int i = rand.nextInt(100);
        i++;
        new_client.set_id(i);
        return true;
    }

    public void uploadFile(byte[] filebyte, String name, String description, String file_name, int owner) throws IOException, SQLException, ClassNotFoundException, RemoteException {
        try{
            String path = "./content_folder/";
            String type = file_name.substring(file_name.lastIndexOf(".")+1);


            //int key = the_center.uploadFile(name,description,owner,this.server_id,type);
            String complete_file_path = path + owner + "_" + file_name; // THE SAME USER CAN'T STORE TWO FILES WITH THE SAME NAME ! (No puc agafar el id del item si encara no l'he posat!)
            File serverFile = new File(complete_file_path);
            String json = "{\"name\":\""+name+"\",\"description\":\""+description+"\",\"owner\":"+owner+",\"path\":\""+complete_file_path+"\",\"server\":"+this.server_id+"}";

            URL url;
            try {
                url = new URL("http://localhost:8080/myRESTwsWeb/rest/item");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");

                OutputStream os = conn.getOutputStream();
                os.write(json.getBytes());
                os.flush();

                int status = conn.getResponseCode();
                if (status != HttpURLConnection.HTTP_CREATED) {
                    System.out.println(status);
                    throw new IOException();
                }
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                System.out.println(br.readLine());
                conn.disconnect();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // -------------------------------------------------------------------

            FileOutputStream out = new FileOutputStream(serverFile);

            out.write(filebyte);
            out.flush();
            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*
        public ArrayList<Item> searchLocal(String fileName) throws SQLException, ClassNotFoundException, RemoteException {
            if (fileName.length() == 0){
                return local_db.getAll();
            }
            else {
                return local_db.search(fileName);
            }

        }

     */
    public List<Item> searchFile(String fileName) throws SQLException, ClassNotFoundException, IOException {

        String output = "";
        String json_result = "";


        URL url = new URL("http://localhost:8080/myRESTwsWeb/rest/global-item/"+ fileName);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");

        //conn.setRequestProperty("Accept", PageAttributes.MediaType.APPLICATION_JSON);

        if(conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        while((output = br.readLine()) != null) {
            System.out.println("\n Resultat json: " + output);
            json_result  = output;
        }
        conn.disconnect();


        List<Item> items;
        Gson gson = new Gson();
        items = gson.fromJson(json_result, new TypeToken<List<Item>>(){}.getType());

        System.out.println(items);

        return items;
    }

    public byte[] downloadFile(String fileName, ClientInterface client) throws SQLException, ClassNotFoundException, IOException {

        List<Item> items = this.searchFile(fileName);

        // --------------------------------------------------------
        Integer option = 1;
        HashMap<Integer, Item> options_available = new HashMap<>();
        for (Item item : items) {
            options_available.put(option, item);
            option += 1;
        }
        // --------------------------------------------------------

        client.showItemsListToClient(options_available);
        Integer chosen_option = client.getUserOption();

        if (chosen_option < 0 || chosen_option > options_available.size()) {
            System.out.println("Option not valid");
            return null;
        }

        Item selected_item = options_available.get(chosen_option);

        System.out.println(selected_item);
        System.out.println(chosen_option);
        System.out.println(selected_item.getName());
        System.out.println(selected_item.getPath());

        File selected_server_content_path_file = new File(selected_item.getPath());
        byte [] selected_data = new byte[(int) selected_server_content_path_file.length()];
        FileInputStream in=new FileInputStream(selected_server_content_path_file);
        in.read(selected_data, 0, selected_data.length);

        return selected_data;
    }

    public Item globalDownloadFileItem(String fileName, ClientInterface client) throws SQLException, ClassNotFoundException, IOException {

        List<Item> items = searchFile(fileName);

        // --------------------------------------------------------
        Integer option = 1;
        HashMap<Integer, Item> options_available = new HashMap<>();
        for (Item item : items) {
            options_available.put(option, item);
            option += 1;
        }
        // --------------------------------------------------------

        client.showItemsListToClient(options_available);
        Integer chosen_option = client.getUserOption();

        if (chosen_option < 0 || chosen_option > options_available.size()) {
            System.out.println("Option not valid");
            return null;
        }
        Item selected_item = options_available.get(chosen_option);

        System.out.println(chosen_option);
        System.out.println(selected_item.getName());

        System.out.println("Selected item server_id:" + selected_item.getServer_id());
        return selected_item;
    }

    public byte[] downloadFileByItem(Item item) throws IOException {
        Item selected_item = item;
        String path = "./content_folder/";

        //File selected_server_content_path_file = new File(path +selected_item.getPath());
        File selected_server_content_path_file = new File(selected_item.getPath()); // it already has the path !
        byte [] selected_data = new byte[(int) selected_server_content_path_file.length()];
        FileInputStream in=new FileInputStream(selected_server_content_path_file);
        in.read(selected_data, 0, selected_data.length);
        //in.flush();
        in.close();
        return selected_data;
    }

    public Item searchByOwner(String fileName, Integer client_id, ClientInterface client) throws ClassNotFoundException, SQLException, IOException {
        String output;
        String json_result = "";


        URL url = new URL("http://localhost:8080/myRESTwsWeb/rest/global-item/"+ fileName + "/user/" + client_id.toString());
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


        List<Item> items;
        Gson gson = new Gson();
        items = gson.fromJson(json_result, new TypeToken<List<Item>>(){}.getType());


        // --------------------------------------------------------
        Integer option = 1;
        HashMap<Integer, Item> options_available = new HashMap<>();
        for (Item item : items) {
            options_available.put(option, item);
            option += 1;
        }
        // --------------------------------------------------------

        client.showItemsListToClient(options_available);
        Integer chosen_option = client.getUserOption();

        if (chosen_option < 0 || chosen_option > options_available.size()) {
            System.out.println("Option not valid");
            return null;
        }
        Item selected_item = options_available.get(chosen_option);

        return selected_item;
    }

    public void deletebyItem(Item item) throws SQLException, ClassNotFoundException, RemoteException {
        String path = "./content_folder/";
        File f= new File(path+item.getPath());
        if (f.delete()){
            //local_db.delete(key);
            //System.out.println("111");
            System.out.println(f.getName() + " deleted");
            System.out.println((int) item.getKey());

            //this.the_center.deleteByKey((int) item.getKey()); #TODO


            //return true;
        }
        else {
            System.out.println(f.getName());
            System.out.println(path+item.getPath());
            // return false;
        }
    }

    public void updatebyItem(Item item, String newname, String newdesc) throws SQLException, ClassNotFoundException, RemoteException {
        // call the RMIserver with my modified item and my client_id (owner) to do the change

        //String output;
        //String json_result = "";

        try {
            // UPDATE NAME
            URL url = new URL("http://localhost:8080/myRESTwsWeb/rest/item/"+ item.getKey() + "/name/" + newname);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            if(conn.getResponseCode() != 204) {
                throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
            }
            conn.disconnect();

            // UPDATE DESCRIPTION
            url = new URL("http://localhost:8080/myRESTwsWeb/rest/item/"+ item.getKey() + "/description/" + newdesc);
            HttpURLConnection conn2 = (HttpURLConnection) url.openConnection();
            conn2.setRequestMethod("POST");
            if(conn2.getResponseCode() != 204) {
                throw new RuntimeException("Failed: HTTP error code: " + conn2.getResponseCode());
            }
            conn2.disconnect();

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        /*
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        while((output = br.readLine()) != null) {
            System.out.println("\n Resultat json: " + output);
            json_result  = output;
        }
        */


    }

    public Integer getUserIdLogin(String username, String password) throws IOException {

        String output;
        //String user_id = "";

        System.out.println(username + password);
        URL url = new URL("http://localhost:8080/myRESTwsWeb/rest/user/username/"+ username + "/password/" + password);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");


        if(conn.getResponseCode() != 201) {
            throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
        }


        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        output = br.readLine();
        System.out.println(Integer.parseInt(output));
        conn.disconnect();

        return Integer.parseInt(output);
    }


    public ArrayList<String> getnames() throws RemoteException, SQLException, ClassNotFoundException {
        //return this.the_center.GetNames();
        return null;
    }

    public int NewUser(String new_name,String new_pass) throws RemoteException, SQLException, ClassNotFoundException {
        //return this.the_center.NewUser(new_name, new_pass);
        return -1;
    }

    public String getPass(String name) throws RemoteException, SQLException, ClassNotFoundException {
        //return this.the_center.getPass(name);
        return null;
    }

    public int getId(String name) throws RemoteException, SQLException, ClassNotFoundException {
        //return this.the_center.getId(name);
        return -1;
    }












    public String hello(){
        System.out.println("hello");
        return "hello";
    }


}