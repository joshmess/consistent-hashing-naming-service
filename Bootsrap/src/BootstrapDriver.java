import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
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
            System.out.println("Nameserver connected");

            ObjectInputStream ins = new ObjectInputStream(ns_socket.getInputStream());
            ObjectOutputStream outs = new ObjectOutputStream(ns_socket.getOutputStream());
            String nameServerDetails = (String) ins.readObject();
            String[] ns_config = nameServerDetails.split(":");
        }

    }
}
