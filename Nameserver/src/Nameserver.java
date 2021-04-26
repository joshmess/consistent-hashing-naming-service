import java.net.Socket;
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
    public String lookup(int key)throws IOException, ClassNotFoundException{

        if(pairs.containsKey(key)){
            return pairs.get(key);
        }else{

            // check successor
            Socket succ_sock = new Socket(configuration.successor_ip,configuration.successor_port);
            ObjectInputStream ins = new ObjectInputStream(succ_sock.getInputStream());
            ObjectOutputStream outs = new ObjectOutputStream(succ_sock.getOutputStream());

            //write lookup key
            outs.writeObject("lookup "+key);
            //write string representing list of visited servers (BS only)
            outs.writeObject("0,"+configuration.id);
            String result = (String)ins.readObject();
            String[] result_list = result.split(":");
            System.out.println(">_[Server Visited]: "+result_list[1]);
            return result_list[0];
            
        }
    }

}
