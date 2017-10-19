# skip-assistant
A Spotify-connected app that detects when you skip a song frequently and suggests you remove it from the playlist

This project makes use of the [Spotify Web API Java](https://github.com/thelinmichael/spotify-web-api-java) library.

## Running
The application can be run in IntelliJ through the `Application.java` file

To run through command line, use the `run.bat` file

To debug, connect a remote debugger to localhost:5005 and run the `debug.bat` file 
or run debug mode on `Application.java` in IntelliJ

## Database Access
Spring runs a built-in [H2](http://www.h2database.com/html/main.html) database.

To connect to the database, you can go to <http://localhost:8080/h2-console/>

Use the following settings:

    Driver Class: org.h2.Driver
    JDBC URL: jdbc:h2:file:~/SkipAssistantDB
    User Name: sa
    Password: <empty>
