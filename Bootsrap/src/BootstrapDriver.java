import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
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
* This class drives the Bootstrap process.
*/
public class BootstrapDriver {

    // Main driver of system, starts bootstrap server and UI thread
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        // Parse config file
        File bnconfig = new File(args[0]);
        Scanner config_scan = new Scanner (bnconfig);
        int bootstrap_id = Integer.parseInt(config_scan.nextLine());
        int bootstrap_conn_port = Integer.parseInt(config_scan.nextLine());
        ServerSocket ss = new ServerSocket(bootstrap_conn_port);
        int highest_ns_id = 0;

        //Create Bootstrap server
        Bootstrap bootstrap_ns = new Bootstrap(bootstrap_id);

        //add orginal keys to bootstrap
        while (config_scan.hasNextLine()) {
            String[] kvp = config_scan.nextLine().split(" ");
            bootstrap_ns.pairs.put(Integer.parseInt(kvp[0]),kvp[1]);
        }

        //create & start UI thread for services
        UIThread bootstrap_ui = new UIThread(bootstrap_ns);
        bootstrap_ui.start();

        //Bootstrap server process waiting for more nameservers
        while(true){

            Socket ns_socket = ss.accept();
            ///open connection with new ns
            ObjectInputStream ins = new ObjectInputStream(ns_socket.getInputStream());
            ObjectOutputStream outs = new ObjectOutputStream(ns_socket.getOutputStream());
            String nameserver_details = (String) ins.readObject();
            String[] ns_config = nameserver_details.split(":");
            //variables for new ns info
            int new_ns_id = -1;
            int new_ns_port = -1;
            String new_ns_ip = "";

            //new ns entering ring
            if(ns_config[0].equals("500")){
             
                new_ns_id = Integer.parseInt(ns_config[1]);
                new_ns_ip = ns_config[2];
                new_ns_port = Integer.parseInt(ns_config[3]);
                bootstrap_ns.server_list.add(new_ns_id);
                Collections.sort(bootstrap_ns.server_list);
                // write back tuple
                outs.writeObject(""+Inet4Address.getLocalHost().getHostAddress()+":"+bootstrap_conn_port);
                String servers_visited = "0";
                //create list of servers visited in format 'ns1 > ns2 > ns3...'
                for (int id : bootstrap_ns.server_list) {
                    if (new_ns_id > id && id != 0) {
                        servers_visited += " > " + id; 
                    }
                }
                outs.writeObject(servers_visited);
                
                if(bootstrap_ns.configuration.successor_id == 0 && bootstrap_ns.configuration.predecessor_id == 0){

                    //bootstrap is the only server upon entry

                    //update BS successor
                    bootstrap_ns.configuration.successor_id = new_ns_id;
                    bootstrap_ns.configuration.successor_ip = new_ns_ip;
                    bootstrap_ns.configuration.successor_port = new_ns_port;
                    //update BS predecessor
                    bootstrap_ns.configuration.predecessor_id = new_ns_id;
                    bootstrap_ns.configuration.predecessor_ip = new_ns_ip;
                    bootstrap_ns.configuration.predecessor_port = new_ns_port;

                    //send pred_id:succ_id
                    outs.writeObject(""+bootstrap_ns.configuration.id+":"+bootstrap_ns.configuration.id);
                    //send pred_ip:pred_port
                    outs.writeObject(""+Inet4Address.getLocalHost().getHostAddress()+":"+4780);
                    //send succ_ip:succ_port
                    outs.writeObject(""+Inet4Address.getLocalHost().getHostAddress()+":"+4780);

                    // transfer keys from range [0,id]
                    for(int i=0;i<Integer.parseInt(ns_config[1]);i++){

                         if(bootstrap_ns.pairs.containsKey(i)){
                            //write key:value
                            outs.writeObject(""+i+":"+bootstrap_ns.pairs.get(i));
                            //remove from bootstrap
                            bootstrap_ns.pairs.remove(i);
                        }
                    }
                    //signal end of transfer
                    outs.writeObject("END");

                }else if(highest_ns_id < new_ns_id){
                    // new ns id greater than maximum server id
                        
                    //update bootstrap predecessor
                    bootstrap_ns.configuration.predecessor_id = new_ns_id;
                    bootstrap_ns.configuration.predecessor_ip = new_ns_ip;
                    bootstrap_ns.configuration.predecessor_port = new_ns_port;

                    //store succ info
                    String nxt_ip = bootstrap_ns.configuration.successor_ip;
                    int nxt_port = bootstrap_ns.configuration.successor_port;

                    Socket nxt_sock = new Socket(nxt_ip,nxt_port);
                    ObjectOutputStream nxt_outs = new ObjectOutputStream(nxt_sock.getOutputStream());
                    ObjectInputStream nxt_ins = new ObjectInputStream(nxt_sock.getInputStream());
                    nxt_outs.writeObject(""+ 900 +" "+ new_ns_id +" " + new_ns_ip + " " + new_ns_port);

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

                    //send keys to succ    
                    for(int i=highest_ns_id;i< new_ns_id;i++){
                        if(bootstrap_ns.pairs.containsKey(i)){
                            //write key:value
                            outs.writeObject(""+i+":"+bootstrap_ns.pairs.get(i));
                            bootstrap_ns.pairs.remove(i);
                        }
                    }
                    outs.writeObject("END");
       
                }else{
                    //insert in between bootstrap and ns
                    //connect to successor
                    Socket succ_sock = new Socket(bootstrap_ns.configuration.successor_ip,bootstrap_ns.configuration.successor_port);
                    ObjectInputStream succ_ins = new ObjectInputStream(succ_sock.getInputStream());
					ObjectOutputStream succ_outs = new ObjectOutputStream(succ_sock.getOutputStream());

                    succ_outs.writeObject( "" + 901 + " " + new_ns_id + " " + new_ns_ip +  " " + new_ns_port);

                    //read in pred_id:succ_id-
                    String pred_succ_id = (String) succ_ins.readObject();
                    String[] id_tuple = pred_succ_id.split(":");

                    //read in pred_ip:pred_port
                    String pred_info = (String) succ_ins.readObject();
                    String[] pred_tuple = pred_info.split(":");

                    //read in succ_ip:succ_port
                    String succ_info = (String) succ_ins.readObject();
                    String[] succ_tuple = succ_info.split(":");

                    //send pred_id:succ_id
                    outs.writeObject(""+id_tuple[0]+":"+id_tuple[1]);
                    //send pred_ip:pred_port
                    outs.writeObject(""+pred_tuple[0]+":"+pred_tuple[1]);
                    //send succ_ip:succ_port
                    outs.writeObject(""+succ_tuple[0]+":"+succ_tuple[1]);

                    String tuple = "";
                    String[] kvp;
                    //read in pairs
                    do{
                        tuple = (String) succ_ins.readObject();
                        kvp = tuple.split(":");
                        if(tuple.equals("END")){
                            break;
                        }
                        //forward to new entry
                        outs.writeObject(kvp[0]+":"+kvp[1]);
                    }while(true);
                    outs.writeObject("END");
                    succ_sock.close();
                }

            }else if(ns_config[0].equals("600")){
                //take in new pred info
                String pred_info = (String) ins.readObject();
                String[] new_pred = pred_info.split(":");
                //reconfigure with new predecessor & successor information
                bootstrap_ns.configuration.reconfigure(bootstrap_ns.configuration.successor_port, Integer.parseInt(new_pred[2]), bootstrap_ns.configuration.successor_id, Integer.parseInt(new_pred[0]),bootstrap_ns.configuration.successor_ip, new_pred[1]);
                String tuple = "";
                String[] kvp = null;
                do{
                    tuple = (String) ins.readObject();
                    kvp = tuple.split(":");
                    if(tuple.equals("END")){
                        break;
                    }
                    //insert into new ns
                    bootstrap_ns.pairs.put(Integer.parseInt(kvp[0]),kvp[1]);
                }while(true);
                    
            }else if(ns_config[0].equals("601")){
            
                    //take in new succ info
                    String succ_info = (String) ins.readObject();
                    String[] new_succ = succ_info.split(":");
                    //reconfigure with new predecessor & successor information
                    bootstrap_ns.configuration.reconfigure(Integer.parseInt(new_succ[2]), bootstrap_ns.configuration.predecessor_port, Integer.parseInt(new_succ[0]), bootstrap_ns.configuration.predecessor_id,new_succ[1], bootstrap_ns.configuration.predecessor_ip);
                    //once this is done, the exit is complete
            }
            highest_ns_id = Collections.max(bootstrap_ns.server_list);
        }

    }
    /*
    * This private inner class represents the UI for a Bootstrap Nameserver
    */
    private static class UIThread  extends Thread{

        Bootstrap bootstrap_ns;

        // Default Constructor
        public UIThread(Bootstrap bootstrap_ns){
            this.bootstrap_ns = bootstrap_ns;
        }

        public void run(){

            Scanner scan = new Scanner(System.in);
            String query = "";

            while(!query.equals("quit")) {

                System.out.print("Bootstrap-NS>_");
                query = scan.nextLine();
                String[] query_list = query.split(" ");

                if(query.equals("quit")){
                    break;
                }

                if(query_list[0].equals("lookup")){

                    try {
                        System.out.println(bootstrap_ns.lookup(Integer.parseInt(query_list[1])));
                    } catch (NumberFormatException | ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }
                }else if(query_list[0].equals("insert")){

                    try {
                        bootstrap_ns.insert(Integer.parseInt(query_list[1]), query_list[2]);
                    } catch (NumberFormatException | ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }
    
                }else if(query_list[0].equals("delete")){

                
                    try {
                        bootstrap_ns.delete(Integer.parseInt(query_list[1]));
                    } catch (NumberFormatException | ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    System.out.println(">_Query Not Recognized");
                }
            }

            System.out.println("Thank You! :)");
            System.exit(0);
        }
    }
}
