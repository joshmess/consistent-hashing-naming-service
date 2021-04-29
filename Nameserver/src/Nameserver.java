import java.net.Socket;
import java.net.UnknownHostException;

import java.util.HashMap;
import java.io.*;

/*SERVICE CODES
*
* NS Process
*   500 --> NS ENTER
*   NS EXIT      
*       600 --> UPDATE_PRED
*       601 --> UPDATE_SUCC
*
* Bootstrap UI (Client Interaction)
*   800 --> LOOKUP
*   801 --> INSERT
*   802 --> DELETE
*
* Bootstrap Process
*   900 --> HIGHEST ENTRY
*   901 --> MIDDLE ENTRY
*   902 --> BOOTSTRAP ONLY ENTRY
*
*/

/*
* This class is a blueprint for a nameserver object, stores data pairs, and provides service implmentation.
*/
public class Nameserver {

    NSConfig configuration;
    static Socket socket;
    HashMap<Integer, String> pairs;

    public Nameserver(int id,int conn_port){
        pairs = new HashMap<>();
        configuration = new NSConfig(id, conn_port);
    }
    //implements ns lookup
    public String lookup(int key, String server_list)throws IOException, ClassNotFoundException{

        if(pairs.containsKey(key)){
            //found in ns
            return pairs.get(key);
        }else if(key > configuration.id){

            // check successor
            Socket succ_sock = new Socket(configuration.successor_ip,configuration.successor_port);
            ObjectInputStream ins = new ObjectInputStream(succ_sock.getInputStream());
            ObjectOutputStream outs = new ObjectOutputStream(succ_sock.getOutputStream());

            //write lookup key
            outs.writeObject("800 "+key);
			outs.writeObject(server_list);
			String value = (String) ins.readObject();
			String new_servers = (String) ins.readObject();

			succ_sock.close();
			return value + " " + new_servers;
        
        }else{
            return "*404NotFound";
        }
    }
    //implement ns insert
    public String insert(int key, String value) throws UnknownHostException, IOException,ClassNotFoundException{

        if(key < configuration.id) {
            //this is the NS to insert at
            pairs.put(key,value);
            return ""+configuration.id;
        }else if(key > configuration.id) {
            //insert into successor
            Socket nxt_sock = new Socket(configuration.successor_ip, configuration.successor_port);
            ObjectInputStream nxt_ins = new ObjectInputStream(nxt_sock.getInputStream());
            ObjectOutputStream nxt_outs = new ObjectOutputStream(nxt_sock.getOutputStream());
            nxt_outs.writeObject("801 "+key+" "+value);
            nxt_outs.writeObject(configuration.id);
            value = (String) nxt_ins.readObject();
            nxt_sock.close();
            return value;
        }else{
            return ">_FAIL";
        }
    }
    //implement ns delete
    public String delete(int key)throws UnknownHostException, IOException, ClassNotFoundException{

        if(key < configuration.id) {
            //this ns has the key
            if(pairs.containsKey(key)){
                pairs.remove(key);
                return ""+configuration.id;
            }
            return "*404NotFound";
        }else if(key > configuration.id){
            //connect with successor
            Socket nxt_sock = new Socket(configuration.successor_ip,configuration.successor_port);
            ObjectInputStream nxt_ins = new ObjectInputStream(nxt_sock.getInputStream());
            ObjectOutputStream nxt_outs = new ObjectOutputStream(nxt_sock.getOutputStream());
            nxt_outs.writeObject("802 " + key);
            return (String) nxt_ins.readObject();
        }else{
            return "*404NotFound";
        }
    }
    /*
    * This protected inner class stores information about each nodes predecessor and successor
    */
    protected static class NSConfig {

        int id;
        int conn_port;

        //Predecessor info
        String predecessor_ip;
        int predecessor_port;
        int predecessor_id;

        //Successor info
        String successor_ip;
        int successor_port;
        int successor_id;

        // Default Constructor
        public NSConfig(int id, int conn_port){

            this.id = id;
            this.conn_port = conn_port;

            successor_port = 0;
            successor_id = 0;
            predecessor_id = 0;
        }

        // If changes to pred/succ occur
        public void reconfigure(int successor_port, int predecessor_port, int  successor_id, int predecessor_id, String successor_ip, String predecessor_ip) {

            this.successor_port = successor_port;
            this.successor_id = successor_id;
            this.predecessor_id = predecessor_id;
            this.predecessor_ip = predecessor_ip;
            this.successor_ip = successor_ip;
            this.predecessor_port = predecessor_port;
        }
    }

}
