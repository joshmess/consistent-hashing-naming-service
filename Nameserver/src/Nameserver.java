import java.net.Socket;
import java.net.UnknownHostException;

import java.util.HashMap;
import java.io.*;

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
            outs.writeObject("lookup "+key);
			outs.writeObject(server_list);
			String value = (String) ins.readObject();
			String new_servers = (String) ins.readObject();

			succ_sock.close();
			return value + " " + new_servers;
        
        }else{
            return ">_404NotFound";
        }
    }
    public String insert(int key, String value) throws UnknownHostException, IOException,ClassNotFoundException{

        if(key < configuration.id) {
            pairs.put(key,value);
            return ""+configuration.id;
        }else if(key > configuration.id) {
            //insert into successor
            Socket nxt_sock = new Socket(configuration.successor_ip, configuration.successor_port);
            ObjectInputStream nxt_ins = new ObjectInputStream(nxt_sock.getInputStream());
            ObjectOutputStream nxt_outs = new ObjectOutputStream(nxt_sock.getOutputStream());
            nxt_outs.writeObject("insert "+key+" "+value);
            nxt_outs.writeObject(configuration.id);
            value = (String) nxt_ins.readObject();
            nxt_sock.close();
            return value;
        }else{
            return ">_FAIL";
        }
    }

}
