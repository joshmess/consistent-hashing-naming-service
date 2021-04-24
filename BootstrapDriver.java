import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class BootstrapDriver {

    // Main driver of system, starts bootstrap server and UI thread
    public static void main(String[] args) throws IOException {

        // PArse config file
        File bnconfig = new File("bnconfig.txt");
        Scanner scan = new Scanner (bnconfig);
        int bootstrap_id = Integer.parseInt(scan.nextLine());
        int bootstrap_conn_port = Integer.parseInt(scan.nextLine());
        ServerSocket ss = new ServerSocket(bootstrap_conn_port);

        Bootstrap bootstrap_ns = new Bootstrap(bootstrap_id);

        while (scan.hasNextLine()) {
            String[] pair = scan.nextLine().split(" ");
            bootstrap_ns.pairs.put(Integer.parseInt(pair[0]),pair[1]);
        }

        UIThread bootstrap_ui = new UIThread(bootstrap_ns);
        bootstrap_ui.start();

        while(true){

            Socket socket = ss.accept();
            System.out.println("Nameserver connected");
        }

    }
}
