import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class UIThread  extends Thread{

    Bootstrap bootstrap_ns;

    public UIThread(Bootstrap bootstrap_ns){
        this.bootstrap_ns = bootstrap_ns;
    }

    public void run(){

        Scanner scan = new Scanner(System.in);
        String query = "";


        while(!query.equals("quit")) {

            System.out.print("myDHT>_");
            query = scan.nextLine();
            String[] query_list = query.split(" ");

            switch(query_list[0]) {

                case "lookup":

                    try {
                        System.out.println(bootstrap_ns.lookup(Integer.parseInt(query_list[1])));
                    } catch (NumberFormatException | ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case "Insert":

                    try {
                        bootstrap_ns.insert(Integer.parseInt(query_list[1]), query_list[2]);
                    } catch (NumberFormatException | ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case "delete":
                    try {
                        bootstrap_ns.delete(Integer.parseInt(query_list[1]));
                    } catch (NumberFormatException | ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }
                    break;

                default:
                    System.out.println(">_Query Not Recognized");

            }
        }
    }
}
