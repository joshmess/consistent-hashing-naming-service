import java.net.Socket;
import java.util.HashMap;

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

}
