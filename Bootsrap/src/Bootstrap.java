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
            System.out.println(">_[Servers Visited]: 0 (Bootstrap-NS Only)");
            return (pairs.get(key));
        }

        // connect to succ
        Socket succ_sock = new Socket(configuration.successor_ip, configuration.successor_port);
		ObjectInputStream ins = new ObjectInputStream(succ_sock.getInputStream());
		ObjectOutputStream outs = new ObjectOutputStream(succ_sock.getOutputStream());
        //write 'lookup key' then 0 for this server
		outs.writeObject("lookup "+key);
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
            nxt_outs.writeObject("insert "+ key + " " + value);
            String servers_visited = (String) nxt_ins.readObject();
            int servcount = 0;
            //iterate over servers that should be displayed
            for(int i = 0; i < servers_visited.length(); i++)
            {
                if(servers_visited.charAt(i) == '>')
                    servcount++;
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
                    
                servcount--;
                if(servcount< 0)
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
        }

        // check successor
    }


}
