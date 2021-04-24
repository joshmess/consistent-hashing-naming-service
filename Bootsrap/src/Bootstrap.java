import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

public class Bootstrap {

    private NSConfig configuration;
    private static ServerSocket server;
    private static int bootstrap_conn_port;
    private static ArrayList<Integer> server_list = new ArrayList<>();
    HashMap<Integer, String> pairs = new HashMap<>();

    // Default Constructor
    public Bootstrap(int id){
        configuration = new NSConfig(id,bootstrap_conn_port);
        server_list.add(id);
    }

    // lookup service for a key
    public String lookup(int key) throws UnknownHostException, IOException, ClassNotFoundException {

        // check for key
        if(pairs.containsKey(key)) {
            System.out.println(">_[Server Visited] ID:0 (Bootstrap-NS)");
            return (pairs.get(key));
        }

        // check successor
        return "NOT FOUND";

    }

    // insert service for a key
    public void insert(int key, String value) throws IOException, ClassNotFoundException {

        if(key > Collections.max(server_list)) {
            System.out.println(">_[Server Visited] ID:0 (Bootstrap-NS)");
            System.out.println(">_Key Inserted Successfully");
            pairs.put(key,value);
        }

        // insert in successor?
    }

    // delete service for a key
    public void delete(int key) throws UnknownHostException, IOException, ClassNotFoundException {

        //if key in bootstrap server then dekete
        if(key > Collections.max(server_list)) {
            System.out.println("[Server Visited] ID:0 (Bootstrap-NS)");
            System.out.println(">_Key Deleted Successfully");
            pairs.remove(key);
        }

        // check successor
    }


}
