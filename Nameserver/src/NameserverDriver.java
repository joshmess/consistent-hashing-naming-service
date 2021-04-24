import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class NameserverDriver {

    public static void main(String[] args) throws FileNotFoundException {

        // Parse config file
        File nsconfig = new File(args[0]);
        Scanner config_scan = new Scanner (nsconfig);
        int id = Integer.parseInt(config_scan.nextLine());
        int conn_port = Integer.parseInt(config_scan.nextLine());
        String serverIP = config_scan.nextLine().split(" ")[0];
        int serverPort = Integer.parseInt(config_scan.nextLine().split(" ")[1]);

        Scanner scan = new Scanner(System.in);
        String bootstrap_ip = "";
        int bootstrap_port = -1;
        String query = "";
        Nameserver ns = new Nameserver(id,conn_port);
        String prompt = "Nameserver"+id+">_";

        while(!query.equals("quit")){

            // take in query
            System.out.println(prompt);
            query = scan.nextLine();
            String[] query_list = query.split(" ");

            switch(query_list[0]){

                case "enter":


            }
        }

    }
}
