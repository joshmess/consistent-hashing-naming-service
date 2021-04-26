import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Scanner;

public class BootstrapDriver {

    // Main driver of system, starts bootstrap server and UI thread
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        // Parse config file
        File bnconfig = new File(args[0]);
        Scanner config_scan = new Scanner (bnconfig);
        int bootstrap_id = Integer.parseInt(config_scan.nextLine());
        int bootstrap_conn_port = Integer.parseInt(config_scan.nextLine());
        ServerSocket ss = new ServerSocket(bootstrap_conn_port);

        Bootstrap bootstrap_ns = new Bootstrap(bootstrap_id);

        while (config_scan.hasNextLine()) {
            String[] pair = config_scan.nextLine().split(" ");
            bootstrap_ns.pairs.put(Integer.parseInt(pair[0]),pair[1]);
        }

        UIThread bootstrap_ui = new UIThread(bootstrap_ns);
        bootstrap_ui.start();

        while(true){

            Socket ns_socket = ss.accept();

            ObjectInputStream ins = new ObjectInputStream(ns_socket.getInputStream());
            ObjectOutputStream outs = new ObjectOutputStream(ns_socket.getOutputStream());
            String nameserver_details = (String) ins.readObject();
            String[] ns_config = nameserver_details.split(":");

            switch(ns_config[0]){
                case "enter":
                    bootstrap_ns.server_list.add(Integer.parseInt(ns_config[1]));
                    Collections.sort(bootstrap_ns.server_list);
                    // write back tuple
                    outs.writeObject(""+Inet4Address.getLocalHost().getHostAddress()+":"+bootstrap_conn_port);
                    String servers_visited = "0";
                    //create list of servers visited in format 'ns1,ns2,ns3...'
                    for (int id : bootstrap_ns.server_list) {
                        if (Integer.parseInt(ns_config[1]) > id && id != 0) {
                            servers_visited += "," + id;
                        }
                    }
                    outs.writeObject(servers_visited);

                    if(bootstrap_ns.configuration.successor_id == 0){
                        //bootstrap is the only server upon entry

                        //send pred_id:succ_id
                        outs.writeObject(""+bootstrap_ns.configuration.id+":"+bootstrap_ns.configuration.id);
                        //send pred_ip:pred_port
                        outs.writeObject(""+Inet4Address.getLocalHost().getHostAddress()+":"+bootstrap_ns.configuration.conn_port);
                        //send succ_ip:succ_port
                        outs.writeObject(""+Inet4Address.getLocalHost().getHostAddress()+":"+bootstrap_ns.configuration.conn_port);

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
                    }else if(Collections.max(bootstrap_ns.server_list) < Integer.parseInt(ns_config[1])){
                        // new ns id greater than maximum server id


                    }
                    break;

                case "update_pred":
                    
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
                    
                    break;

                case "update_succ":
                    //take in new succ info
                    String succ_info = (String) ins.readObject();
                    String[] new_succ = succ_info.split(":");
                    //reconfigure with new predecessor & successor information
                    bootstrap_ns.configuration.reconfigure(Integer.parseInt(new_succ[2]), bootstrap_ns.configuration.predecessor_port, Integer.parseInt(new_succ[0]), bootstrap_ns.configuration.predecessor_id,new_succ[1], bootstrap_ns.configuration.predecessor_ip);
                    //once this is done, the exit is complete
                    break;
            }
        }

    }
}
