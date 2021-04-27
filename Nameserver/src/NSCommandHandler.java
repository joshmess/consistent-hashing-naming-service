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

                    case "insert":
                        //insert into ns or succ
                        int key_to_insert = Integer.parseInt(query_list[1]);
                        String value_to_insert = query_list[2];

                        String result = ns.insert(key_to_insert, value_to_insert);
                        outs.writeObject(ns.configuration.id + " > " + result);
                        break;

                    case "highest-entry":
                        //ns entering with highest id
                        int new_ns_id = Integer.parseInt(query_list[1]);
                        String new_ns_ip  = query_list[2];
                        int new_ns_port = Integer.parseInt(query_list[3]);

                        //store succ info
                        String nxt_ip = ns.configuration.successor_ip;
                        int nxt_port = ns.configuration.successor_port;

                        //if new id higher than ns and succ is not bootstrap
                        if(ns.configuration.successor_id != 0 && new_ns_id > ns.configuration.id){
                            Socket  nxt_sock = new Socket(nxt_ip,nxt_port);
                            ObjectOutputStream nxt_outs = new ObjectOutputStream(nxt_sock.getOutputStream());
                            ObjectInputStream nxt_ins = new ObjectInputStream(nxt_sock.getInputStream());
                            nxt_outs.writeObject("highest-entry "+new_ns_id +" " + new_ns_ip + " " + new_ns_port);

                            //read in pred_id:succ_id-
                            String pred_succ_id = (String) nxt_ins.readObject();
                            String[] id_tuple = pred_succ_id.split(":");

                            //read in pred_ip:pred_port
                            String pred_info = (String) nxt_ins.readObject();
                            String[] pred_tuple = pred_info.split(":");

                            //read in succ_ip:succ_port
                            String succ_info = (String) nxt_ins.readObject();
                            String[] succ_tuple = succ_info.split(":");

                            //send pred_id:succ_id
                            outs.writeObject(""+id_tuple[0]+":"+id_tuple[1]);
                            //send pred_ip:pred_port
                            outs.writeObject(""+pred_tuple[0]+":"+pred_tuple[1]);
                            //send succ_ip:succ_port
                            outs.writeObject(""+succ_tuple[0]+":"+succ_tuple[1]);
                        }else{
                            //succ is bootstrap --> dont read, just write back

                            //send pred_id:succ_id
                            outs.writeObject(""+ns.configuration.id+":"+ns.configuration.successor_id);
                            //send pred_ip:pred_port
                            outs.writeObject(""+Inet4Address.getLocalHost().getHostAddress()+":"+ns.configuration.conn_port);
                            //send succ_ip:succ_port
                            outs.writeObject(""+nxt_ip+":"+nxt_port);

                            ns.configuration.reconfigure(new_ns_port, ns.configuration.predecessor_port, new_ns_id, ns.configuration.predecessor_id, new_ns_ip, Inet4Address.getLocalHost().getHostAddress());
                        }
                        break;

                    case "lookup":
                        
                        int key = Integer.parseInt(query_list[1]);
					    String server_list = (String) ins.readObject();
                        //lookup in ns
					    String[] value = ns.lookup(key,server_list).split(" ");	
					    if(value.length > 1)
						    server_list = server_list.concat(" > "+value[1]);
					    else
						    server_list = server_list.concat(" > "+ns.configuration.id);
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