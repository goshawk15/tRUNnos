import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandle extends Thread {
    ObjectInputStream in;
    ObjectOutputStream out;
    Socket connection;
    // Constructor
    public ClientHandle(Socket socket) {
        try {
            connection = socket;
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Results Reduce(List<Pair> results_of_chunks){
        double total_distance = 0;
        double total_time = 0;
        double total_elevation = 0;
        double total_avg_speed = 0;

        for(Pair chunk : results_of_chunks){
            total_distance += chunk.value.distance;
            total_time += chunk.value.time;
            total_elevation += chunk.value.elevation;
            total_avg_speed += chunk.value.avgspeed;
        }
        total_avg_speed = total_avg_speed/results_of_chunks.size();

        int lepta = (int) total_time / 60;
        double seconds = (int) total_time % 60 * 0.01;

        total_time = lepta + seconds;

        return new Results(total_distance,total_time,total_elevation,total_avg_speed);

    }
    public void run() {
        try {
            //Get the message
            String message = (String) in.readObject();

            //Client Handle
            if(message.equals("Client")) {
                
                //Read the connection type
                String type = (String) in.readObject();
                
                if(type.equals("file_upload"))
                {
                    //Read the file from the Client
                    String file = (String) in.readObject();

                    //Split the file in chunks
                    GPXSplitter s = new GPXSplitter(file, 4);
                    List<String> chunks = s.run();

                    //Get the username of the Client
                    String user = s.getCreator();
                    System.out.println("New file received by user: "+user);

                    //Get the number of chunks created
                    int number_of_chunks = chunks.size();

                    //Initialize a counter that represents the position of the Client in the Queue
                    int this_counter;

                    //Update the List of the routes received by the server
                    synchronized (Master.routes) {
                        this_counter = Master.getCounter();
                        Master.routes.add(new Pair(this_counter, chunks.size()));
                        Master.updateCounter();
                    }

                    //Add the user in the dictionary if he is not already registered
                    synchronized (Master.chunks_dictionary) {
                        if(!Master.chunks_dictionary.containsKey(user)){
                            Master.chunks_dictionary.put(user, new user_stats(user));
                        }
                    }

                    //Add the chopped chunks to the queue
                    synchronized (Master.queue) {
                        for (String chunk : chunks) {
                            Master.queue.add(new Pair(this_counter, chunk));
                        }
                    }

                    //Create a list to store the Results of the map function for each chunk
                    List<Pair> chunks_to_reduce = new ArrayList<>();

                    //Wait for the results of the map function and call the reduce function
                    while(true){
                        int count = 0;
                        synchronized (Master.mid_results) {
                            for (Pair p : Master.mid_results) {
                                if (p.key == this_counter) {
                                    count++;
                                    if (!chunks_to_reduce.contains(p)) {
                                        chunks_to_reduce.add(p);
                                    }
                                }
                            }
                        }
                        //if all results from the map function have arrived for this file call the reduce function
                        if (count == number_of_chunks) {
                            //Call the Reduce function to get the final results
                            Results final_results = Reduce(chunks_to_reduce);

                            //Update the statistcs of the user
                            Master.chunks_dictionary.get(user).addResults(final_results);

                            
                            StringBuilder results = new StringBuilder();
                            results.append(user+'/'+final_results.distance+'/'+final_results.elevation+'/'+final_results.time+'/'+final_results.avgspeed*1000);

                            out.writeObject(results);
                            out.flush();

                            //Update the global Statistics
                            synchronized (Master.global_stats){
                                Master.updateStats(final_results);
                            }
                        }
                    }
                }
                //Return the statistics
                else{
                    String username = (String) in.readObject();
                    try{
                        user_stats statistics = Master.chunks_dictionary.get(username);
                        global_stats global_stats = Master.getGlobal_stats();
                        
                        StringBuilder statistics_to_send = new StringBuilder();
                        //Personal Data + global data
                        statistics_to_send.append(username+'/'+statistics.total_distance+'/'+statistics.avg_distance+'/'+statistics.total_ele+'/'+statistics.avg_ele+'/'+statistics.total_time+'/'+statistics.avg_time+'/'+statistics.total_routes+'/'+global_stats.total_distance+'/'+global_stats.avg_distance+'/'+global_stats.total_elevation+'/'+global_stats.avg_elevation+'/'+global_stats.total_time+'/'+global_stats.avg_time+'/'+global_stats.total_routes);

                        out.writeObject(statistics_to_send);
                        out.flush();
                        
                    }
                    catch(NullPointerException e){
                        out.writeObject("No available Statistics");
                        out.flush();
                    }
                }

            }
            //Worker Handle
            else {
                //Initialize an id for this Worker-Handler
                int this_worker;
                synchronized (Master.routes){
                    Master.updateWorkersCounter();
                    this_worker = Master.getWorkers_counter();
                }

                //Keep the connection
                while (true) {
                    {
                        Thread.sleep(1);
                        //Implementation of the Round-Robin technique
                        if (this_worker == Master.getrrCounter()) {

                            //Get the next chunk from the queue
                            synchronized (Master.queue) {
                                if (!Master.queue.isEmpty()) {
                                    {
                                        //Send the chunk to the Worker
                                        out.writeObject(Master.getElement());
                                        out.flush();

                                        //Get the Results of the map function
                                        Pair result = (Pair) in.readObject();

                                        //Update the Round-Robin counter
                                        synchronized (Master.mid_results) {
                                            Master.mid_results.add(result);
                                            Master.updaterrCounter();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("No more available chunks to process!");;
        }
        finally {
            try {
                in.close();
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
