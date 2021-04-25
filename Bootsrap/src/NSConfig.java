
/*
* This class stores information about each nodes predecessor and successor
*/
public class NSConfig {

    int id;
    int conn_port;

    //Predecessor info
    String predecessor_ip;
    int predecessor_port;
    int predecessor_id;

    //Successor info
    String successor_ip;
    int successor_port;
    int successor_id;

    // Default Constructor
    public NSConfig(int id, int conn_port){

        this.id = id;
        this.conn_port = conn_port;

        successor_port = 0;
        successor_id = 0;
        predecessor_id = 0;
    }

    // If changes to pred/succ occur
    public void reconfigure(int successor_port, int predecessor_port, int  successor_id, int predecessor_id, String successor_ip, String predecessor_ip) {

        this.successor_port = successor_port;
        this.successor_id = successor_id;
        this.predecessor_id = predecessor_id;
        this.predecessor_ip = predecessor_ip;
        this.successor_ip = successor_ip;
        this.predecessor_port = predecessor_port;
    }
}
