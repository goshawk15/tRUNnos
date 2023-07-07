import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Master {
    private static int port_number;
    /* Define the socket that receives requests */
    ServerSocket server;

    /* Define the socket that is used to handle the connection */
    Socket providerSocket;

    //Counter for connected Clients
    static int counter;

    //Counter for connected Workers
    static int workers_counter;

    //Counter for the round-robin implementation
    static int round_robin_counter;

    //List to store a Pair of every new route and the number of it's chunks
    static List<Pair> routes;

    //List to store the chunks
    static List<Pair> queue;

    //List to store the results of the map function
    static List<Pair> mid_results;

    //Custom type global_stats to store the statistics of all the users
    static global_stats global_stats;

    //Hashmap data structure to store the statistics of each user
    static HashMap<String, user_stats> chunks_dictionary;

    //Constructor of the Server
    public Master(int p) {
        counter = 0;
        workers_counter = 0;
        port_number = p;
        chunks_dictionary = new HashMap<>();
        queue = new ArrayList<>();
        routes = new ArrayList<>();
        mid_results = new ArrayList<>();
        global_stats = new global_stats();
        round_robin_counter = 1;
    }

    //Main function
    public static void main(String[] args) throws InterruptedException {
        int port = Integer.parseInt(args[0]);
        new Master(port).openServer();
    }

    //Synchronized method to update the global Stats
    static synchronized void updateStats(Results new_result){
        Master.global_stats.updateStatistics(new_result);
    }
    static synchronized global_stats getGlobal_stats(){
        return global_stats;
    }

    //Synchronized method to update the counter of connected Clients
    static synchronized void updateCounter(){
        counter++;
    }
    static synchronized int getCounter() {
        return counter;
    }

    //Synchronized method to update the counter of connected Workers
    static synchronized int getWorkers_counter() {
        return workers_counter;
    }
    static synchronized void updateWorkersCounter(){
        workers_counter++;
    }

    //Synchronized method to update the counter for the Round-Robin implementation
    static synchronized void updaterrCounter(){
        if(workers_counter > round_robin_counter){
            round_robin_counter++;
        }
        else
            round_robin_counter = 1;
    }
    static synchronized int getrrCounter() {
        return round_robin_counter;
    }

    //Synchronized method to get the last chunk in the queue
    static synchronized Pair getElement(){
        return queue.remove(queue.size()-1);
    }

    //Open the Server
    public void openServer() {
        try {
            /* Create Server Socket */
            server = new ServerSocket(port_number);
            System.out.println("Server is listening on port " + port_number);

            while (true) {
                /* Accept the connection */
                providerSocket = server.accept();

                System.out.println("New connection established server");

                /* Handle the request */
                Thread client = new ClientHandle(providerSocket);
                client.start();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                providerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}