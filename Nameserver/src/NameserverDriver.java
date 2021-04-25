import java.io.*;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.Scanner;

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

                    System.out.println(">_Successful Entry");
                    System.out.println(">_Range of IDs Managed: ["+id_tuple[0]+","+id+"]");
                    System.out.println(">_Servers Visited: ["+servers_visited+"]");

                    outs.close();
                    ins.close();
                    sock.close();

                    break;

                default:
                    System.out.println(">_ Query Not Recognized.");


            }
        }

    }
}
