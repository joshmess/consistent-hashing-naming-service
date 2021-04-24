import java.net.Socket;

public class Nameserver {

    private NSConfig configuration;
    private static Socket socket;

    public Nameserver(int id,int conn_port){
        configuration = new NSConfig(id, conn_port);
    }

}
