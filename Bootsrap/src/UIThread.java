import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;

/*
* This class represents the UI for a Bootstrap Nameserver
*/
public class UIThread  extends Thread{

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
    }
}
