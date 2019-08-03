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

        //if the called action displays text then also set the divider to be fully right
        UI.addButton("All Stations",   () ->{listAllStations(); UI.setDivider(1);});
        UI.addButton("All Lines",      () ->{listAllTrainLines(); UI.setDivider(1);});
        UI.addTextField("Station",     (String name) -> {this.stationName=name;});
        UI.addTextField("Train Line",  (String name) -> {this.lineName=name;});
        UI.addTextField("Destination", (String name) -> {this.destinationName=name;});
        UI.addTextField("Time (24hr)", (String time) ->
            {try{this.startTime=Integer.parseInt(time);}catch(Exception e){UI.println("Enter four digits");}});
        UI.addButton("Lines of Station",    () -> {listLinesOfStation(this.stationName); UI.setDivider(1);});
        UI.addButton("Stations on Line",    () -> {listStationsOnLine(this.lineName); UI.setDivider(1);});
        UI.addButton("Stations connected?", () -> {checkConnected(this.stationName, this.destinationName); UI.setDivider(1);});
        UI.addButton("Next Services",       () -> {findNextServices(this.stationName, this.startTime); UI.setDivider(1);});
        UI.addButton("Find Trip",           () -> {findTrip(this.stationName, this.destinationName, this.startTime); UI.setDivider(1);});
        UI.addButton("Show Geographical map", () -> {showMap(".\\data\\geographic-map.png");});
        UI.addButton("Show Lines map", () -> {showMap(".\\data\\system-map.png");});
        UI.addButton("Clear",UI::clearPanes);
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
        double distFromHub;

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
                        distFromHub = stationScan.nextDouble();

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
     * overloaded method
     * returns the lines that has the selected station and the destination station in a set
     * heading in the correect direction
     * searches each line to check if it has the stations in correct order
     * has boolean to tell if want all or just first returned
     *
     * @param stationN
     * @param destN
     * @param getAll
     * */
    public Set<TrainLine> checkConnected(String stationN ,String destN, boolean getAll){

        Set<TrainLine> connectedLines = new HashSet<>();

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

                            //adding the found line to the set
                            connectedLines.add(tL);

                            //if it isnt get all just return the one
                            if(!getAll){
                                return connectedLines;
                            }
                            break;
                        }
                    }
                }
            }

        }
        //return the set of connected train lines could be empty
        return connectedLines;
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

        //station must exist and time not be negative or 0
        if(stationsMap.containsKey(stationN) && startT > 0){

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

            UI.println("station must exist and time be greater at least 1");
        }

    }



    /**
     * method will find the train line and time the train leaves chosen departure station
     * it will also return the arival time at destination and the number of zones traveled through
     * takes parameters of station name destination name and start time
     * @param stationN
     * @param destN
     * @param startT
     * */
    public void findTrip(String stationN, String destN, int startT){

        //making sure the stations exist and the time is a valid one
        if(stationsMap.containsKey(stationN) && stationsMap.containsKey(destN) && startT > 0) {

            //getting the zones get the max number as start and min as end
            int startZone = Math.max(stationsMap.get(stationN).getZone(), stationsMap.get(destN).getZone());
            int endZone =  Math.min(stationsMap.get(stationN).getZone(), stationsMap.get(destN).getZone());

            int departTime = -1;
            int arivalTime = -1;

            //Map that holds the train line as key and list as value that will have the depart time in position 0 and arival in 1
            Map<TrainLine, List<Integer>> tripInfo = new HashMap<>();


           //finding number of zones traveled through add one as final zone is counted
            int totalZones = startZone - endZone + 1;

            //getting all the lines with these stations connected
            Set<TrainLine> connectedLines = checkConnected(stationN, destN, true);

            //return as no connection
            if(connectedLines.isEmpty()){

                UI.println("no direct connection");
                //calling multi line trip to find across multiple lines
                //findMultiLineTrip(stationN, destN, startT);
                return;
            }

            //looping over all connected lines for the stations
            for(TrainLine tL : connectedLines){

               //getting the stations on the current loop line
                List<Station> stationsOnLine = tL.getStations();
                int startPos = -1;
                int endPos = -1;

                //looping over to find the index of the departure and arival station
                for(int i = 0; i < stationsOnLine.size(); i++){

                    if(stationsOnLine.get(i).getName().equals(stationN)){
                        startPos = i;
                    }
                    else if(stationsOnLine.get(i).getName().equals(destN)){
                        endPos = i;
                    }

                }

                //getting the services on the current line and looping over them
                List<TrainService> linesServices =  tL.getTrainServices();

                for(TrainService tS : linesServices){

                    //seeing if the time in the start position station is greater than or is the selected start time and the end ends there doest skip it
                    if(tS.getTimes().get(startPos) >= startT && tS.getTimes().get(endPos) != -1){

                        departTime = tS.getTimes().get(startPos);
                        arivalTime = tS.getTimes().get(endPos);

                        //creating temperary list to pass the values into the tripInfo map as a list
                        List<Integer> tripsTimes = new ArrayList<>();
                        tripsTimes.add(departTime);
                        tripsTimes.add(arivalTime);

                        //adding the train line and the list with the times to the map
                        tripInfo.put(tL,tripsTimes);

                        //breaking from loop as only need soonest time for each line
                        break;
                    }

                }

            }

            TrainLine nextDepartLine = null;
            int lowestDepartTime = -1;

            //making sure there is a trip possible
            if(tripInfo.isEmpty()){

                UI.println("no trips exist sorry try setting earilier time");
                return;
            }

            //looping over all keys in the tripInfo map
            for(TrainLine tL : tripInfo.keySet()){

                //getting the departure time for check
                int tempDepart = tripInfo.get(tL).get(0);

                //checking if the time to check is less than the previous one or none are set yet
                if(tempDepart < lowestDepartTime || lowestDepartTime == -1){

                    //setting the new lowest and the related key (train line)
                    lowestDepartTime = tempDepart;
                    nextDepartLine = tL;
                }

            }

            //printing the line departure time and arival also how many zones the trip goes through
            UI.println("Train Line: " + nextDepartLine.getName());
            UI.println("Departure Time: " + tripInfo.get(nextDepartLine).get(0));
            UI.println("Arrival Time: " + tripInfo.get(nextDepartLine).get(1));
            UI.println("Number of zones for trip: " + totalZones);
            UI.println("......................");
        }
        else{
            UI.println("stations must exist and time be greater than 0");
        }


    }


    /////////////////////////////////////////////////////////////////////////////WORKING ON THIS METHOD AT MOMENT CURRENTLY NOT WORKING
   /* public void findMultiLineTrip(String departN, String ariveN, int leaveT){

        //making sure all input is valid for search
        if(stationsMap.containsKey(departN) && stationsMap.containsKey(ariveN) && leaveT > 0){

            Set<TrainLine> connectLines = new HashSet<>();
            Map<Station, Set<TrainLine>> connectionPoint = new HashMap<>();

            //getting the train lines on the departing station
            Set<TrainLine> linesOnDepart = stationsMap.get(departN).getTrainLines();

            for(TrainLine tL : linesOnDepart){

                //getting all stations on each line
                List<Station> stationsOnLine  = tL.getStations();

                for(Station linesStations : stationsOnLine){

                    //clearing old lines as new station
                    connectLines.clear();

                    //getting all the lines on each looped station
                    Set<TrainLine> linesOnStationsOnLine  = linesStations.getTrainLines();

                    for(TrainLine lineOnStation : linesOnStationsOnLine){

                        //checking if that line also connects to the destination and filling the line and station to the connectionPoint map
                        if(lineOnStation.getStations().contains(stationsMap.get(ariveN))){

                           //adding the line to the connectLines set storiing the lines on that station that connect
                           connectLines.add(lineOnStation);

                        }
                    }

                    if(!connectLines.isEmpty()) {

                        //adding the connect station and all the lines that connect
                        connectionPoint.put(linesStations, connectLines);
                    }
                }

            }



            for(Station connectionStation : connectionPoint.keySet()){


                //findTrip(connectionStation.getName(), ariveN, );



            }







        }





    }

*/


    /**
     * is callled when button pressed to show map will take the file name as parameter
     * then clears the old map or any graphics from screen and draws the full sized image just off the top corner
     * the divider is moved in to show the image no matter the window size and the user can pull it futher if required
     * @param fileName
     * */
    public void showMap(String fileName){

        UI.clearGraphics();

        //moving the text divider to the far left
        UI.setDivider(0);

        UI.drawImage(fileName, 10, 10);
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
