import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.Inet4Address;

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

                switch(query_list[0]){

                    case "lookup":
                        
                        int key = Integer.parseInt(query_list[1]);
					    String server_list = (String) ins.readObject();
                        //lookup in ns
					    String[] value = ns.lookup(key,server_list).split(" ");	
					    if(value.length > 1)
						    server_list = server_list.concat("->"+value[1]);
					    else
						    server_list = server_list.concat("->"+ns.configuration.id);
					    outs.writeObject(value[0]);
					    outs.writeObject(server_list);
					    break;
                    case "update_succ":
                        //new ns entry
                        
                        //update this NS
                        ns.configuration.successor_id = Integer.parseInt(query_list[1]);
                        ns.configuration.successor_ip = query_list[2];
                        ns.configuration.successor_port = Integer.parseInt(query_list[3]);
                    
                        break;
                    case "update_pred":
                        //update pred and transfer keys
                        String new_pred = (String) ins.readObject();
                        String[] pred_list = new_pred.split(" ");
                        ns.configuration.predecessor_id = Integer.parseInt(pred_list[0]);
                        ns.configuration.predecessor_ip = pred_list[1];
                        ns.configuration.predecessor_port = Integer.parseInt(pred_list[2]);
                        String tuple = "";
                        String[] kvp;
                        do{
                            tuple = (String) ins.readObject();
                            kvp = tuple.split(":");
                            if(tuple.equals("END")){
                                break;
                            }
                            ns.pairs.put(Integer.parseInt(kvp[0]),kvp[1]);
                        }while(true);
                        break;

                }

            }
        }catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }   
    }
}