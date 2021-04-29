import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

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
* This class represents a bootstrap name server and provides service implementation.
*/
public class Bootstrap {

    BSNConfig configuration;
    ServerSocket server;
    int bootstrap_conn_port;
    ArrayList<Integer> server_list;
    HashMap<Integer, String> pairs;

    // Default Constructor
    public Bootstrap(int id){
        server_list = new ArrayList<>();
        pairs = new HashMap<>();
        configuration = new BSNConfig(id,bootstrap_conn_port);
        server_list.add(id);
    }

    // lookup service for a key
    public String lookup(int key) throws UnknownHostException, IOException, ClassNotFoundException {

        // check for key
        if(pairs.containsKey(key)) {
            System.out.println(">_[Servers Visited]: 0 (Bootstrap-NS Only)");
            return (pairs.get(key));
        }

        // connect to succ
        Socket succ_sock = new Socket(configuration.successor_ip, configuration.successor_port);
		ObjectInputStream ins = new ObjectInputStream(succ_sock.getInputStream());
		ObjectOutputStream outs = new ObjectOutputStream(succ_sock.getOutputStream());
        //write 'lookup key' then 0 for this server
		outs.writeObject("" + 800 + " " + key);
		outs.writeObject("0");
        //read value and  server list
        String value = (String) ins.readObject();
		String servers_visited = (String) ins.readObject();
        //sort ids for printing
        Collections.sort(server_list);
        //count servers visited
		int numserv = 0;
		for(int i = 0; i < servers_visited.length(); i++)
		{
			if(servers_visited.charAt(i) == '>'){
				numserv+=1;
            }
		}
		
		System.out.print(">_[Servers Visited]: ");
        //print correct ids
		for(int id : server_list) {
			if(numserv-1 < 0){
				System.out.println(id);
            }else{
				System.out.print(id + " >> ");
            }
				
			numserv-=1;
			if(numserv < 0){
				break;
            }
		}
        System.out.println();
		succ_sock.close();
		return value;
    }

    // insert service for a key
    public void insert(int key, String value) throws IOException, ClassNotFoundException {

        if(key > Collections.max(server_list)) {
            System.out.println(">_[Servers Visited] ID:0 (Bootstrap-NS)");
            System.out.println(">_Key inserted at Bootstrap Nameserver");
            pairs.put(key,value);
        }else{
            Collections.sort(server_list);
            //check successor
            Socket nxt_sock = new Socket(configuration.successor_ip,configuration.successor_port);
            ObjectInputStream nxt_ins = new ObjectInputStream(nxt_sock.getInputStream());
            ObjectOutputStream nxt_outs = new ObjectOutputStream(nxt_sock.getOutputStream());
            //write insert key value
            nxt_outs.writeObject("" + 801 + " "+ key + " " + value);
            String servers_visited = (String) nxt_ins.readObject();
            int servcount = 0;
            //iterate over servers that should be displayed
            for(int i = 0; i < servers_visited.length(); i++)
            {
                if(servers_visited.charAt(i) == '>'){
                    servcount+=1;
                }
            }
            System.out.print(">_[Servers Visited]: "  );
            int final_id = -1;
            for(int id : server_list) {
                if(servcount <= 0){
                    System.out.println(id);
                    final_id = id;
                }else{
                    System.out.print(id + " >> ");
                }
                    
                servcount-=1;
                if(servcount < 0)
                    break;
            }
            System.out.println();
            System.out.println(">_Item inserted at Nameserver"+final_id);
            nxt_sock.close();
        }
    }

    // delete service for a key
    public void delete(int key) throws UnknownHostException, IOException, ClassNotFoundException {

        //if key in bootstrap server then delete
        if(key > Collections.max(server_list)) {
            System.out.println("[Server Visited] ID:0 (Bootstrap-NS)");
            System.out.println(">_Key Deleted Successfully");
            pairs.remove(key);
        }else{
            //connect with successor
            Socket nxt_sock = new Socket(configuration.successor_ip,configuration.successor_port);
            ObjectInputStream nxt_ins = new ObjectInputStream(nxt_sock.getInputStream());
            ObjectOutputStream nxt_outs = new ObjectOutputStream(nxt_sock.getOutputStream());
            nxt_outs.writeObject("" + 802 + " " + key);

            String id_list = (String) nxt_ins.readObject();
            int servcount = 0;
            //iterate over servers that should be displayed
            for(int i = 0; i < id_list.length(); i++){
                if(id_list.charAt(i) == '>'){
                    servcount+=1;
                }
                if(id_list.charAt(i) == '*'){
                    System.out.println(">_ Key Not Found");
                    return;
                }
            }
            System.out.println(">_ Deletion Successful "  );
            System.out.print(">_ [Servers Visited]: "  );
            for(int id : server_list) {
                if(servcount <= 0){
                    System.out.println(id);
                    int final_id = id;
                }else{
                    System.out.print(id + " >> ");
                }
                    
                servcount-=1;
                if(servcount < 0)
                    break;
            }
        }
        // check successor
    }

    /*
    * This protected inner class stores information about each nodes predecessor and successor
    */
    protected class BSNConfig {

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
        public BSNConfig(int id, int conn_port){

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
