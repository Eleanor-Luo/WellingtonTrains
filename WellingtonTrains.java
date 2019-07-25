// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2019T2, Assignment 2
 * Name:Joe SMith
 * Username:smithjose4
 * ID:300488105
 */

import ecs100.*;
import javafx.beans.property.MapProperty;

import java.awt.Color;
import java.lang.reflect.Array;
import java.util.*;
import java.io.*;

/**
 * WellingtonTrains
 * A program to answer queries about Wellington train lines and timetables for
 *  the train services on those train lines.
 * 
 * See the assignment page for a description of the program and what you have to do.
 */

public class WellingtonTrains{
    //Fields to store the collections of Stations and Lines
    /*# YOUR CODE HERE */

    //hash map created to store all the stations info in it the key is the name as all stations have different names
    Map<String,Station> stationsMap = new HashMap<>();

    //hash map for the train lines uses the key of its name and stores the object as value
    Map<String, TrainLine> linesMap = new HashMap<>();
    // Fields for the suggested GUI.
    private String stationName;            // station to get info about, or to start journey from
    private String lineName;               // train line to get info about.
    private String destinationName;  
    private int startTime = 0;                  // time for enquiring about

    /**
     * main method:  load the data and set up the user interface
     */
    public static void main(String[] args){
        WellingtonTrains wel = new WellingtonTrains();
        wel.loadData();   // load all the data
        wel.setupGUI();   // set up the interface
    }

    /**
     * Load data files
     */
    public void loadData(){
        loadStationData();
        UI.println("Loaded Stations");
        loadTrainLineData();
        UI.println("Loaded Train Lines");
        // The following is only needed for the Completion and Challenge
        loadTrainServicesData();
        UI.println("Loaded Train Services");
    }

    /**
     * User interface has buttons for the queries and text fields to enter stations and and train line
     * You will need to implement the methods here.
     */
    public void setupGUI(){
        UI.addButton("All Stations",   this::listAllStations);
        UI.addButton("All Lines",      this::listAllTrainLines);
        UI.addTextField("Station",     (String name) -> {this.stationName=name;});
        UI.addTextField("Train Line",  (String name) -> {this.lineName=name;});
        UI.addTextField("Destination", (String name) -> {this.destinationName=name;});
        UI.addTextField("Time (24hr)", (String time) ->
            {try{this.startTime=Integer.parseInt(time);}catch(Exception e){UI.println("Enter four digits");}});
        UI.addButton("Lines of Station",    () -> {listLinesOfStation(this.stationName);});
        UI.addButton("Stations on Line",    () -> {listStationsOnLine(this.lineName);});
        UI.addButton("Stations connected?", () -> {checkConnected(this.stationName, this.destinationName);});
        UI.addButton("Next Services",       () -> {findNextServices(this.stationName, this.startTime);});
        UI.addButton("Find Trip",           () -> {findTrip(this.stationName, this.destinationName, this.startTime);});
        UI.addButton("Quit", UI::quit);
        UI.setDivider(1.0);
    }

    // Methods for loading data and answering queries

    /*# YOUR CODE HERE */

    /**
     * loads all the info about the train stations from the stations data file
     * stores each station in the stationsMap and creates the station object to store in it
     * */
    public void loadStationData(){

        //getting each line of the stations data file each line is related to a station
        ArrayList<String> stationInfo = readAllLines("stations.data");

        Scanner stationScan = null;
        String stationName;
        int zone;
        float distFromHub;

        //looping over each line
        for(String station : stationInfo){
            stationScan = new Scanner(station);

            //get all the tokens from the line
            while (stationScan.hasNextLine()){

                //getting the name then zone then distance
                stationName = stationScan.next();
                zone = stationScan.nextInt();
                distFromHub = stationScan.nextFloat();

                //adding each station to the map using the name as the key and the station object as value
                stationsMap.put(stationName,new Station(stationName,zone,distFromHub));
            }

        }

        //if the scanner was used close it
        if(stationScan != null) {
            stationScan.close();
        }

    }

    /**
     * loads all the train lines into the train line map
     * also reads each related stations data file for each
     * line this allows the stations list in the line object to be
     * filled and the lines list on the stations object to be filled
     * */
    public void loadTrainLineData(){

        //getting all the lines from the train lines data file
        ArrayList<String> trainLinesInfo = readAllLines("train-lines.data");
        ArrayList<String> linesStations;

        //for each line create a new trainline object and add it to the map each line only has the name so no scanner needed
        for(String trainLine : trainLinesInfo){

            //adding the line to the lines map and creating object
            linesMap.put(trainLine, new TrainLine(trainLine));

            //reading all the stations that this train passes through
            linesStations = readAllLines(trainLine + "-stations.data");

            for(String station : linesStations){

                //if the station is real
                if(stationsMap.containsKey(station)){

                    //add the station object to the lines station list and the line to the stations line list
                    linesMap.get(trainLine).addStation(stationsMap.get(station));
                    stationsMap.get(station).addTrainLine(linesMap.get(trainLine));

                }
            }

        }

    }


    /**
     * querry 1 this will list to the user all of the stations names from the stations map
     * as the names are the keys it will print all the keys
     * */
    public void listAllStations(){

        //looping over all the keys (station names) and printing them to the screan
        for(String s : stationsMap.keySet()){
            UI.println(s);
        }

    }

    /**
     * querry 2 this will list the names of the train lines by printing all the keys as they are the names
     * */
    public void listAllTrainLines(){

        //looping over all the keys (line names) and printing them to screen
        for(String l : linesMap.keySet()){
            UI.println(l);
        }

    }



    /**
     * query 3
     * takes a station name as parameter
     * lists all the lines that go through the selected station
     * @param stationN
     * */
    public void listLinesOfStation(String stationN){

        //storing the selected station in a objecct
        Station selectedStation = stationsMap.get(stationN);

        //getting all the lines that go through that station into a set
        Set<TrainLine> linesThroughStation = selectedStation.getTrainLines();

        //listing each lines name that is in that set of lines
        for(TrainLine tL: linesThroughStation){

            UI.println(tL.getName());
        }

    }

    /**
     * 4th querry
     * takes the line to get the stations on it as parameter
     * then lists all the stations that are on this line
     *
     * @param lineName
     * */
    public void listStationsOnLine(String lineName){

        //storing the selected trainline as an object
        TrainLine selectedLine = linesMap.get(lineName);

        //getting all the stations that are on that line
        List<Station> stationsOnline = selectedLine.getStations();

        //printing each station name to the screen
        for (Station s: stationsOnline){

            UI.println(s.getName());
        }

    }

    // Utility method to help with reading data files
    /** Read all lines in a file into a list of Strings */
    public ArrayList<String> readAllLines(String filename){
        try{
            ArrayList<String> ans = new ArrayList<String>();
            Scanner sc = new Scanner(new File(filename));
            while (sc.hasNext()){
                ans.add(sc.nextLine());
            }
            sc.close();
            return ans;
        }
        catch(IOException e){UI.println("Fail: " + e); return null;}
    }

}
