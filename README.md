# tRUNnos
Android Fitness App (oreo 8.0.0) where users can upload their routes in gpx format, and get results including: distance ran, total elevation, time and average speed
Additionally users can track and compare their results to other users of the app.
All of the data is stored in the Server who uses ServerSocket in java.
The Server distributes the tasks to connected machines called Workers who are multithreaded objects and therefore can process chunks of data in paralell.
