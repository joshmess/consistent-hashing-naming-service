import java.io.*;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.Scanner;

/*
* Driving class for a nameserver.
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

        while(!query.equals("quit")){

            // take in query
            System.out.print(prompt);
            query = scan.nextLine();
            String[] query_list = query.split(" ");

            switch(query_list[0]){

                case "enter":
                    Socket sock = new Socket(server_ip,server_port);
                    ObjectOutputStream outs = new ObjectOutputStream(sock.getOutputStream());
                    ObjectInputStream ins = new ObjectInputStream(sock.getInputStream());
                    //write to bootstrap: entry:nsID:nsIP:conn_port
                    outs.writeObject("enter:"+id+":"+ Inet4Address.getLocalHost().getHostAddress()+":"+conn_port);
                    //read in 'bootstrap_ip:bootstrap_port'
                    String bootstrap_address = (String) ins.readObject();
                    String[] bootstrap_tuple = bootstrap_address.split(":");
                    //read in 'ns1,ns2,ns3...'
                    String servers_visited = (String) ins.readObject();


                    //read in pred_id:succ_id
                    String pred_succ_id = (String) ins.readObject();
                    String[] id_tuple = pred_succ_id.split(":");

                    //read in pred_ip:pred_port
                    String pred_info = (String) ins.readObject();
                    String[] pred_tuple = pred_info.split(":");

                    //read in succ_ip:succ_port
                    String succ_info = (String) ins.readObject();
                    String[] succ_tuple = succ_info.split(":");

                    ns.configuration.id = id;
                    ns.configuration.reconfigure(Integer.parseInt(succ_tuple[1]),Integer.parseInt(pred_tuple[1]),Integer.parseInt(id_tuple[1]),Integer.parseInt(id_tuple[0]),succ_tuple[0],pred_tuple[0]);
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

                    //start NSCommandHandler
                    NSCommandHandler cmd_thread = new NSCommandHandler(ns);
                    cmd_thread.start();

                    System.out.println(">_Successful Entry");
                    System.out.println(">_Predecessor ID: "+ns.configuration.predecessor_id);
                    System.out.println(">_Successor ID: "+ns.configuration.successor_id);
                    System.out.println(">_Range of Keys Managed: ("+id_tuple[0]+","+id+"]");
                    System.out.println(">_Servers Visited: ["+servers_visited+"]");

                    outs.close();
                    ins.close();
                    sock.close();

                    break;

                case "exit":
                    //configure connection with successor
                    Socket succ_sock = new Socket(ns.configuration.successor_ip,ns.configuration.successor_port);
                    ObjectOutputStream succ_outs = new ObjectOutputStream(succ_sock.getOutputStream());
                    ObjectInputStream succ_ins = new ObjectInputStream(succ_sock.getInputStream());

                    //tell successor about exit
                    succ_outs.writeObject("update_pred");
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
                    
                    //configure connection with predecessor
                    Socket pred_sock = new Socket(ns.configuration.predecessor_ip,ns.configuration.predecessor_port);
                    ObjectOutputStream pred_outs = new ObjectOutputStream(pred_sock.getOutputStream());
                    ObjectInputStream pred_ins = new ObjectInputStream(pred_sock.getInputStream());
                    succ_outs.writeObject("update_succ");
                    //send succ_id:succ_ip:succ_port
                    succ_outs.writeObject(""+ns.configuration.successor_id+":"+ns.configuration.successor_ip+":"+ns.configuration.successor_port);
                    System.out.println(">_Successful Exit");
                    System.out.println(">_Successor ID: "+ns.configuration.successor_id);
                    System.out.println(">_Range of Keys Transferred: ["+ns.configuration.predecessor_id+","+ns.configuration.id+"]");
        
                default:
                    System.out.println(">_ Query Not Recognized.");


            }
        }

    }
}
