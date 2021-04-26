import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/*
* This class services requests at nameservers other than the Bootstrap.
*/
public class NSCommandHandler extends Thread{

    Nameserver ns;
    ServerSocket ss;
    Socket sock;

    // Default Constructor 
    public NSCommandHandler(Nameserver ns){
        this.ns = ns;
    }

    public void run(){

        try{
            int port = ns.configuration.conn_port;
            ss = new ServerSocket(port);

            while(true){
                //configure incoming connection
                sock = ss.accept();
                ObjectOutputStream outs = new ObjectOutputStream(sock.getOutputStream());
                ObjectInputStream ins = new ObjectInputStream(sock.getInputStream());

                String query = (String) ins.readObject();
                String[] query_list = query.split(" ");
                System.out.println("Servicing at NS: "+ query_list[0]);

                switch(query_list[0]){

                    case "lookup":
                        //lookup in ns
                        //read in servers visited
                        String servers_visited = (String) ins.readObject();
                        //parse key from query
                        int key = Integer.parseInt(query_list[1]);
                        String result = ns.lookup(key);
                    
                        if(!result.equals("NOT FOUND") && !result.equals("CHECK SUCC")){
                            servers_visited += ","+ns.configuration.id;
                        }
                        //send value:ns1,ns2,ns3,...
                        outs.writeObject(""+result+":"+servers_visited);
                        break;

                }

            }
        }catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }   
    }
}