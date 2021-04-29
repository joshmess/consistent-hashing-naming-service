import java.io.*;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.Scanner;
import java.net.ServerSocket;

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
* Driving class for the main nameserver process and the NS query handling thread.
*/
public class NameserverDriver {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        // Parse config file
        File nsconfig = new File(args[0]);
        Scanner config_scan = new Scanner (nsconfig);
        int id = Integer.parseInt(config_scan.nextLine());
        int conn_port = Integer.parseInt(config_scan.nextLine());

        String address_tuple = config_scan.nextLine();
        String server_ip = address_tuple.split(" ")[0];
        int server_port = Integer.parseInt(address_tuple.split(" ")[1]);

        // create new ns object
        Nameserver ns = new Nameserver(id,conn_port);
        String prompt = "Nameserver"+id+">_";
        Scanner scan = new Scanner(System.in);
        String query = "";

        //main NS process of entry and exit
        while(!query.equals("quit")){

            // take in query
            System.out.print(prompt);
            query = scan.nextLine();
            String[] query_list = query.split(" ");

            if(query_list[0].equals("enter")){

                Socket sock = new Socket(server_ip,server_port);
                ObjectOutputStream outs = new ObjectOutputStream(sock.getOutputStream());
                ObjectInputStream ins = new ObjectInputStream(sock.getInputStream());
                //write to bootstrap: entry:nsID:nsIP:conn_port
                outs.writeObject("500:"+id+":"+ Inet4Address.getLocalHost().getHostAddress()+":"+conn_port);
                //read in 'bootstrap_ip:bootstrap_port'
                String bootstrap_address = (String) ins.readObject();
                String[] bootstrap_tuple = bootstrap_address.split(":");
                //read in 'ns1 > ns2 > ns3...'
                String servers_visited = (String) ins.readObject();

                //enter if statement

                //read in pred_id:succ_id
                String pred_succ_id = (String) ins.readObject();
                String[] id_tuple = pred_succ_id.split(":");

                //read in pred_ip:pred_port
                String pred_info = (String) ins.readObject();
                String[] pred_tuple = pred_info.split(":");

                //read in succ_ip:succ_port
                String succ_info = (String) ins.readObject();
                String[] succ_tuple = succ_info.split(":");

                //reconfigure ns
                ns.configuration.reconfigure(Integer.parseInt(succ_tuple[1]),Integer.parseInt(pred_tuple[1]),Integer.parseInt(id_tuple[1]),Integer.parseInt(id_tuple[0]),succ_tuple[0],pred_tuple[0]);
                ns.configuration.id = id;
                String tuple = "";
                String[] kvp = null;

                do{
                    tuple = (String) ins.readObject();
                    kvp = tuple.split(":");
                    if(tuple.equals("END")){
                         break;
                    }
                    //insert into new ns
                    ns.pairs.put(Integer.parseInt(kvp[0]),kvp[1]);
                }while(true);

                System.out.println(">_Successful Entry");
                System.out.println(">_Predecessor ID: "+ns.configuration.predecessor_id);
                System.out.println(">_Successor ID: "+ns.configuration.successor_id);
                System.out.println(">_Range of Keys Managed: ("+id_tuple[0]+","+id+"]");
                System.out.println(">_Servers Visited: ["+servers_visited+"]");

                //start NSCommandHandler
                NSQueryHandler cmd_thread = new NSQueryHandler(ns);
                cmd_thread.start();

                outs.close();
                ins.close();
                sock.close();

            }else if(query_list[0].equals("exit")){

                //configure connection with successor
                Socket succ_sock = new Socket(ns.configuration.successor_ip,ns.configuration.successor_port);
                ObjectOutputStream succ_outs = new ObjectOutputStream(succ_sock.getOutputStream());
                ObjectInputStream succ_ins = new ObjectInputStream(succ_sock.getInputStream());

                 //configure connection with predecessor
                 Socket pred_sock = new Socket(ns.configuration.predecessor_ip,ns.configuration.predecessor_port);
                 ObjectOutputStream pred_outs = new ObjectOutputStream(pred_sock.getOutputStream());
                 ObjectInputStream pred_ins = new ObjectInputStream(pred_sock.getInputStream());

                //tell successor about exit
                succ_outs.writeObject("600");
                //send pred_id:pred_ip:pred_port
                succ_outs.writeObject(""+ns.configuration.predecessor_id+":"+ns.configuration.predecessor_ip+":"+ns.configuration.predecessor_port);
                    
                //transfer all keys to successor
                for(int i=ns.configuration.predecessor_id;i<ns.configuration.id;i++){

                    if(ns.pairs.containsKey(i)){
                        //write key:value
                        succ_outs.writeObject(""+i+":"+ns.pairs.get(i));
                        ns.pairs.remove(i);
                    }
                }
                succ_outs.writeObject("END");
                    
                //contect predecessor
                pred_outs.writeObject("601");
                //send succ_id:succ_ip:succ_port
                pred_outs.writeObject(""+ns.configuration.successor_id+":"+ns.configuration.successor_ip+":"+ns.configuration.successor_port);
                
                System.out.println(">_Successful Exit");
                System.out.println(">_Successor ID: "+ns.configuration.successor_id);
                System.out.println(">_Range of Keys Transferred: ["+ns.configuration.predecessor_id+","+ns.configuration.id+"]");

            }else{
                System.out.println(">_ Query Not Recognized.");
            }
        }

    }

    /*
    * This class services requests at nameservers other than the Bootstrap.
    */
    private static class NSQueryHandler extends Thread{

        Nameserver ns;
        ServerSocket ss;
        Socket sock;

        // Default Constructor 
        public NSQueryHandler(Nameserver ns){
            this.ns = ns;
        }

        //Separate thread to listen for queries
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
                
                    if(query_list[0].equals("800")){

                        String server_list = (String) ins.readObject();
                        //lookup in ns
                        String[] result = ns.lookup(Integer.parseInt(query_list[1]),server_list).split(" ");
                        String value = result[0];
                        
                        if(!(result.length > 1)){
                            //value, found --> add id
                            server_list += " > "+ns.configuration.id;
                        }else{
                            //if value not found --> add id & next server found
                            server_list += " > "+ns.configuration.id + " > " + value;
                        }
                        outs.writeObject(value);
                        outs.writeObject(server_list);

                    }else if(query_list[0].equals("801")){
                        //insert into ns or succ
                        outs.writeObject(ns.configuration.id + " > " + ns.insert(Integer.parseInt(query_list[1]), query_list[2]));
                    }else if(query_list[0].equals("802")){
                        //call ns delete
                        outs.writeObject(ns.configuration.id + " > " + ns.delete(Integer.parseInt(query_list[1])));
                    }else if(query_list[0].equals("901")){
                        //ns entering in between other nameservers
                        int entering_id = Integer.parseInt(query_list[1]);
                        String entering_ip  = query_list[2];
                        int entering_port = Integer.parseInt(query_list[3]);

                        //middle entry BASE CASE
                        if(ns.configuration.id > entering_id){
                            //insert new nameserver now

                            //send pred_id:succ_id
                            outs.writeObject(""+ns.configuration.predecessor_id+":"+ns.configuration.id);
                            //send pred_ip:pred_port
                            outs.writeObject(""+Inet4Address.getLocalHost().getHostAddress()+":"+ns.configuration.predecessor_port);
                            //send succ_ip:succ_port
                            outs.writeObject(""+Inet4Address.getLocalHost().getHostAddress()+":"+ns.configuration.conn_port);
                            
                                //send keys to succ    
                            for(int i=ns.configuration.predecessor_id;i< entering_id;i++){
                                if(ns.pairs.containsKey(i)){
                                    //write key:value
                                    outs.writeObject(""+i+":"+ns.pairs.get(i));
                                    ns.pairs.remove(i);
                                }
                            }
                            outs.writeObject("END");

                            //update this ns
                            ns.configuration.predecessor_id = entering_id;
                            ns.configuration.predecessor_ip = entering_ip;
                            ns.configuration.predecessor_port = entering_port;
                        }
                        //more than one hop??
                    }else if(query_list[0].equals("900")){                    
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
                            nxt_outs.writeObject("900 "+new_ns_id +" " + new_ns_ip + " " + new_ns_port);

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
                    }
                }
            }catch(IOException | ClassNotFoundException e){
                e.printStackTrace();
            }   
        }
    }
}
