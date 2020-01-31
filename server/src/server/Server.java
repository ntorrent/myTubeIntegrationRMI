package server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.CenterInterface;
import common.Item;
import common.ServerData;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.util.Scanner;

public class Server {
    private static Registry startRegistry(Integer port) throws RemoteException {
        if(port == null) {
            port = 1099;
        }
        try {
            Registry registry = LocateRegistry.getRegistry(port);
            registry.list( );
            // The above call will throw an exception
            // if the registry does not already exist
            return registry;
        }
        catch (RemoteException ex) {
            // No valid registry at that port.
            System.out.println("RMI registry cannot be located ");
            Registry registry= LocateRegistry.createRegistry(port);
            System.out.println("RMI registry created at port ");
            return registry;
        }
    }
    public static void main(String args[]) {
        try {
            //as a client

            System.out.println("If this server is already registered, insert the 'server_id' integer \n If is not registered, insert '0': ");
            Scanner in = new Scanner(System.in);
            Integer i = in.nextInt();

            Integer server_id = i;
            if (i == 0) {
                System.out.println("Insert the port for the new server (must be different to ohters): ");
                Scanner sc = new Scanner(System.in);
                String port = sc.next();
                server_id = postServer("hostx", port);
                if (server_id == -1) {
                    System.out.print("ERROR REGISTERING SERVER !");
                    System.exit(1);
                }
            }

            ServerData server = getServer(server_id);

            TubeImplementation this_server = new TubeImplementation(server_id);

            //as a server

            System.out.println("This server is using the port number " + server.port);
            Registry lower_registry = startRegistry(server.port);

            lower_registry.bind("TheTube", this_server);

            System.out.println("local server ready");

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString()); e.printStackTrace();
        }
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

    public static Integer postServer(String ip, String port_str) {

        Integer port = Integer.parseInt(port_str);

        String json = "{\"ip\":\""+ip+"\",\"port\":"+port+"}";
        //System.out.println(json);

        URL url;
        String server_id = "-1";
        try {
            url = new URL("http://localhost:8080/myRESTwsWeb/rest/server");
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
            server_id = br.readLine();
            System.out.println(server_id);
            conn.disconnect();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return Integer.parseInt(server_id);
    }
}
