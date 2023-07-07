import java.io.*;
import java.net.*;

public class Worker extends Thread {

    private Socket connection;
    private int port;
    private String host;
    public Worker(String h ,int p) {
        host = h;
        port = p;
    }
    public void run() {
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        try {
            //Try to connect to the WorkerServer
            connection = new Socket(host, port);

            // Initialize the input and output streams
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());

            //Send the message
            out.writeObject("Worker");
            out.flush();

            System.out.println("Connected to the Server");

            //Keep the connection on
            while(true){
                //Get the chunk from the Server
                Pair received_chunk = (Pair) in.readObject();

                //Create a thread to process the chunk and call the map function
                Thread threadformap = new ActionsforWorker(received_chunk.key,received_chunk,out);
                threadformap.start();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("No more available chunks to process!");;
        }
    }
    public static void main(String[] args) {
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        new Worker(host,port).start();
    }

}