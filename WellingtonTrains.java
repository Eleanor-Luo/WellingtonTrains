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
        //UI.addButton("Find Trip",           () -> {findTrip(this.stationName, this.destinationName, this.startTime);});
        UI.addButton("Clear", UI::clearText);
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
        ArrayList<String> stationInfo = readAllLines(".\\data\\stations.data");

        Scanner stationScan = null;
        String stationName;
        int zone;
        float distFromHub;

        //try catch as scanner might cause error incase format changes
        try {

            if(!stationInfo.isEmpty()) {

                //looping over each line
                for (String station : stationInfo) {
                    stationScan = new Scanner(station);

                    //get all the tokens from the line
                    while (stationScan.hasNext()) {

                        //getting the name then zone then distance
                        stationName = stationScan.next();
                        zone = stationScan.nextInt();
                        distFromHub = stationScan.nextFloat();

                        //adding each station to the map using the name as the key and the station object as value
                        stationsMap.put(stationName, new Station(stationName, zone, distFromHub));
                    }

                }
            }
        }
        catch(Exception e){

            //as stations cant be loaded must close the program
            UI.println("Error reading stations file (incorrect format)");
            UI.sleep(2000);
            UI.quit();
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
        ArrayList<String> trainLinesInfo = readAllLines(".\\data\\train-lines.data");
        ArrayList<String> linesStations;

        if(!trainLinesInfo.isEmpty()) {


            //for each line create a new trainline object and add it to the map each line only has the name so no scanner needed
            for (String trainLine : trainLinesInfo) {

                //adding the line to the lines map and creating object
                linesMap.put(trainLine, new TrainLine(trainLine));

                //reading all the stations that this train passes through
                linesStations = readAllLines(".\\data\\" + trainLine + "-stations.data");

                if(!linesStations.isEmpty()) {

                    for (String station : linesStations) {

                        //if the station is real
                        if (stationsMap.containsKey(station)) {

                            //add the station object to the lines station list and the line to the stations line list
                            linesMap.get(trainLine).addStation(stationsMap.get(station));
                            stationsMap.get(station).addTrainLine(linesMap.get(trainLine));

                        }
                    }
                }
            }
        }

    }

    /**
     * loads the services times for each train line each time is added to the trainService object
     * and then that is added to the trainServices Collection on the related line object
     *
     * */
    public void loadTrainServicesData(){


        ArrayList<String> times;
        Scanner sc = null;

        //looping over all line names (key)
        for(String tL : linesMap.keySet()){

            //reading the services file for that line
            times = readAllLines(".\\data\\" + tL + "-services.data");

            //make sure safe to loop
            if(!times.isEmpty()) {

                //looping over each line in the file
                for (String s : times) {

                    //making a temp servive for the current line looped over so can add times then add to the line
                    TrainService tempService = new TrainService(linesMap.get(tL));

                    //dealing with scanner and format could be wrong
                    try {

                        sc = new Scanner(s);

                        //adding all the times on the current line of file to the temperary service object
                        while (sc.hasNext()) {

                            tempService.addTime(sc.nextInt());
                        }

                    } catch (Exception e) {
                        UI.println("error reading times wrong format");
                    }

                    //all times added so add the service to the lines trainService list
                    linesMap.get(tL).addTrainService(tempService);
                }
            }
        }

        //closing scanner
        if(sc != null){
            sc.close();
        }
    }

    /**
     * querry 1 this will list to the user all of the stations names from the stations map
     * as the names are the keys it will print all the keys
     * */
    public void listAllStations(){

        //cant loop on empty map
        if(!stationsMap.isEmpty()) {

            //looping over all the keys (station names) and printing them to the screan
            for (String s : stationsMap.keySet()) {
                UI.println(s);
            }

            UI.println(".....................");
        }
        else{
            UI.println("no stations avalible (not loaded)");
        }
    }

    /**
     * querry 2 this will list the names of the train lines by printing all the keys as they are the names
     * */
    public void listAllTrainLines(){

        //cant loop on empty map
        if(!linesMap.isEmpty()) {

            //looping over all the keys (line names) and printing them to screen
            for (String l : linesMap.keySet()) {
                UI.println(l);
            }

            UI.println(".....................");
        }
        else{
            UI.println("no Lines avalible (not loaded)");
        }

    }



    /**
     * query 3
     * takes a station name as parameter
     * lists all the lines that go through the selected station
     * @param stationN
     * */
    public void listLinesOfStation(String stationN){

        //making sure the station to check exists
        if(stationsMap.containsKey(stationN)) {

            //storing the selected station in a objecct
            Station selectedStation = stationsMap.get(stationN);

            //getting all the lines that go through that station into a set
            Set<TrainLine> linesThroughStation = selectedStation.getTrainLines();

            //listing each lines name that is in that set of lines
            for (TrainLine tL : linesThroughStation) {

                UI.println(tL.getName());
            }

            UI.println(".....................");
        }
        else{
            UI.println("Station doesnt exist make sure to use - for spaces");
        }

    }

    /**
     * 4th querry
     * takes the line to get the stations on it as parameter
     * then lists all the stations that are on this line
     *
     * @param lineN
     * */
    public void listStationsOnLine(String lineN){

        //making sure the line to check for stations exists
        if(linesMap.containsKey(lineN)) {

            //storing the selected trainline as an object
            TrainLine selectedLine = linesMap.get(lineN);

            //getting all the stations that are on that line
            List<Station> stationsOnline = selectedLine.getStations();

            //printing each station name to the screen
            for (Station s : stationsOnline) {

                UI.println(s.getName());
            }

            UI.println(".....................");
        }
        else{
            UI.println("line doesnt exist dont forget - for spaces and _ to seperate first and final station on line");
        }

    }


    /**
     * prints to screen the lines that has the selected station and the destination station
     * heading in the correect direction
     * searches each line to check if it has the stations in correct order
     * @param stationN
     * @param destN
     * */
    public void checkConnected(String stationN ,String destN ){

        boolean isConnected = false;

        //making sure both stations exist
        if(stationsMap.containsKey(stationN) && stationsMap.containsKey(destN)) {

            boolean hasDepart;

            //looping over each line
            for (TrainLine tL : linesMap.values()) {

                //set to false to make sure not true from last line check
                hasDepart = false;

                //getting all stations on that line
                List<Station> linesStations = tL.getStations();

                if(!linesStations.isEmpty()) {
                    for (Station s : linesStations) {

                        //making sure that the station name is before the destination in the list so the order isnt messed up
                        if (s.getName().equals(stationN)) {

                            //set to true so can now check for the destination station
                            hasDepart = true;
                        } else if (hasDepart && s.getName().equals(destN)) {

                            UI.println(tL.getName());
                            isConnected = true;
                            break;
                        }
                    }
                }
            }

            //checking if its connected or not
            if(isConnected){

                UI.println(".......................");
            }
            else{

                UI.println("No Connection");
            }
        }
        else{

            UI.println("Stations must exist dont forget - for spaces");
        }

    }



    /**
     * will find the next departing service time for all the lines on the service
     *the station and start time are parameters will find the next time either equall to or greater than the time given in 24hr
     * @param stationN
     * @param startT
     * */
    public void findNextServices(String stationN, int startT){


        Set<TrainLine> linesOnStation;      //set of all lines on the station
        List<Station> stationsOnLine;       //set for all stations on the line
        List<TrainService> servicesOnLine;  //set for all services on the line
        int stationPosition = -1;           //start position to find where the station is on service times


        boolean serviceFound = false;

        //station must exist and time not be negative
        if(stationsMap.containsKey(stationN) && startT >= 0){

            Station chosenStation = stationsMap.get(stationN);

            //getting the lines on station
            linesOnStation =  chosenStation.getTrainLines();

            if(!linesOnStation.isEmpty()){

                for(TrainLine tL : linesOnStation){

                    //getting the stations on the current line being looped over
                    stationsOnLine = tL.getStations();

                    if(!stationsOnLine.isEmpty()){

                        //start position -1 as starting new check
                        stationPosition = -1;

                        //loop over all stations on the line
                        for(int i = 0; i < stationsOnLine.size(); i++){

                            //if the name of the station is the same as selected then store the index as that is the index for time  to search for
                            if(stationsOnLine.get(i).getName().equals(stationN)){
                                stationPosition = i;
                            }
                        }
                    }


                    //getting the services on the line
                    servicesOnLine = tL.getTrainServices();

                    if(stationPosition != -1) {
                        //looping over each service
                        for (TrainService tS : servicesOnLine) {

                            //getting the time in the station position the selected stations time the train is there
                            int timeCheck = tS.getTimes().get(stationPosition);

                            //checking if it is valid to be next service
                            if (timeCheck >= startT) {

                                //print the line name and time then return as dont want all times
                                UI.println(tL.getName() + ": " + timeCheck);
                                serviceFound = true;
                                break;

                            }
                        }
                    }

                }

            }

            //printing end seperator or if it wasnt found
            if(!serviceFound){

                UI.println("no services found");
            }
            else{

                UI.println("......................");
            }
        }
        else{

            UI.println("station must exist and time be greater atleast 0");
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
