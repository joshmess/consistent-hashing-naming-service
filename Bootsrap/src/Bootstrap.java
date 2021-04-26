import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;


/*
* This class represents a bootstrap name server and provides service implementation.
*/
public class Bootstrap {

    NSConfig configuration;
    ServerSocket server;
    int bootstrap_conn_port;
    ArrayList<Integer> server_list;
    HashMap<Integer, String> pairs;

    // Default Constructor
    public Bootstrap(int id){
        server_list = new ArrayList<>();
        pairs = new HashMap<>();
        configuration = new NSConfig(id,bootstrap_conn_port);
        server_list.add(id);
    }

    // lookup service for a key
    public String lookup(int key) throws UnknownHostException, IOException, ClassNotFoundException {

        // check for key
        if(pairs.containsKey(key)) {
            System.out.println(">_[Server Visited]: 0 (Bootstrap-NS Only)");
            return (pairs.get(key));
        }

        // check successor
        System.out.println(configuration.successor_ip+":"+configuration.successor_port);
        Socket succ_sock = new Socket(configuration.successor_ip,configuration.successor_port);
        ObjectInputStream ins = new ObjectInputStream(succ_sock.getInputStream());
        ObjectOutputStream outs = new ObjectOutputStream(succ_sock.getOutputStream());
        
        //write lookup key
        outs.writeObject("lookup "+key);
        //write string representing list of visited servers (BS only)
        outs.writeObject("0");

        String result = (String)ins.readObject();
        String[] result_list = result.split(":");
        System.out.println(">_[Server Visited]: "+result_list[1]);
        
        return result_list[0];
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
