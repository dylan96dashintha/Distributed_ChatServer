import java.io.*;  
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner; 
import org.json.JSONObject;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.json.JSONException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ClientHandler.ClientHandler;
import Connection.ClientServerConnection;
import Connection.Server2ServerConnection;
import Heartbeat.ConsensusJob;
import Heartbeat.GossipJob;
import Heartbeat.Heartbeat;
import Messaging.Sender;
import Server.ServerState;
import model.Constants;


public class App 
{
	private static final Logger logger = LogManager.getLogger(App.class);
	
    public static void main( String[] args )
    {  	
    	 
    	//execute with jar    	
    	String serverName = args[0];
    	String confFilePath = args[1];
//    	
    	//execute with eclips   	
//    	String serverName = "s1";
//    	String confFilePath = "conf.txt"; 	    	
    	

//    	Read configure file
    	logger.info("Starting Server "+ serverName);
    	boolean iterate = true;
    	ArrayList<String> configuration = new ArrayList<String>();
    	while(iterate) {
    		logger.debug("Running Loop");
    		try {
    			File file = new File(confFilePath);
    			Scanner reader= new Scanner(file);
    			while (reader.hasNextLine()) {
    		        String[] data = reader.nextLine().split("\t");
    		        configuration.add(new JSONObject()
    		        			.put("server-name", data[0])
    		        			.put("address", data[1])
    		        			.put("client-port", data[2])
    		        			.put("server-port", data[3])
    		        			.toString());
    		     }
    			reader.close();
    			iterate = false;
    			logger.debug("File reading finished");
    		} catch (FileNotFoundException e1) {
    			iterate = false;
    			logger.info("No such file in " + confFilePath + " file path");
    			System.exit(0);
    		}    
    	}
    	logger.debug("Configuration file: "+ configuration.toString());
    	//initialize server
    	ServerState currentServer = ServerState.getServerState().initializeServer(serverName, configuration);	
	
    	//create server connection    	
    	Thread server2serverListingThread = new Thread() {
    		public void run() {
    			ServerSocket serverSocket = null;
    			Socket socket = null;
    			try {
    				serverSocket = new ServerSocket();
    				SocketAddress socketAddress = new InetSocketAddress("0.0.0.0",
    						ServerState.getServerState().getServerPort());
    				serverSocket.bind(socketAddress);
    				logger.info("Server " + ServerState.getServerState().getServerName()
    						+ " Listening for other servers, Address: " + ServerState.getServerState().getServerAddress()
    						+ ", Port: " + ServerState.getServerState().getServerPort());

    			} catch (IOException e) {
    				logger.error(e.getMessage());
    			}
    			
    			while (true) {
    				try {
    					socket = serverSocket.accept();
    					Server2ServerConnection servr2ServerConnection = new Server2ServerConnection(socket);
    					servr2ServerConnection.start();
    				} catch (IOException e) {

    					logger.error(e.getMessage());
    					logger.error("Server Stop Listening");

    				}
    			}
    		}
    	};
    	
    	server2serverListingThread.start();
    	
    	//Create client connection
    	ServerSocket serverSocket = null;
    	Socket socket = null;
    	try {
    		serverSocket = new ServerSocket();
    		SocketAddress socketAddress = new InetSocketAddress("0.0.0.0", currentServer.getClientPort());    		
    		serverSocket.bind(socketAddress);
    		logger.debug("Server "+ currentServer.getServerName() +" Listening for Clients, Address: "+ currentServer.getServerAddress()+ ", Port: "+ currentServer.getClientPort());
    	}catch (IOException e) {
    		logger.error(e.getMessage());		
    	}
    	
	    	
    	boolean isListening = true;
    	
    	//start leader election if needed
    	Runnable heartbeat = new Heartbeat("Heartbeat");
        new Thread(heartbeat).start();
    	
        //start heartbeat process
    	if(isListening) {
    		logger.info("Failure Detection is running GOSSIP mode");
	    	startGossipJob();
	    	startConsensusJob();
    	}
    	
//    	listening for clients
    	while (isListening) {
	       try {
               socket = serverSocket.accept();
               ClientServerConnection clientServerConnection = new ClientServerConnection(socket);
       			clientServerConnection.start();
           } catch (IOException e) {
        	   isListening = false;
        	   logger.error(e.getMessage());
        	   logger.error("Server Stop Listening");
        	   
           }
    	}

    	}
    
    private static void startGossipJob() {
        try {

            JobDetail gossipJob = JobBuilder.newJob(GossipJob.class)
                    .withIdentity(Constants.GOSSIP_JOB, "group1").build();

            gossipJob.getJobDataMap().put("aliveErrorFactor", Constants.ALIVE_ERROR_FACTOR);

            Trigger gossipTrigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity(Constants.GOSSIP_JOB_TRIGGER, "group1")
                    .withSchedule(
                            SimpleScheduleBuilder.simpleSchedule()
                                    .withIntervalInSeconds(Constants.ALIVE_INTERVAL).repeatForever())
                    .build();

            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(gossipJob, gossipTrigger);

        } catch (SchedulerException e) {
            logger.error("Error in starting gossibJobing(Heartbeat)");
        }
    }
    
    private static void startConsensusJob() {
        try {

            JobDetail consensusJob = JobBuilder.newJob(ConsensusJob.class)
                    .withIdentity(Constants.CONSENSUS_JOB, "group1").build();

            consensusJob.getJobDataMap().put("consensusVoteDuration", Constants.CONSENSUS_VOTE_DURATION);

            Trigger consensusTrigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity(Constants.CONSENSUS_JOB_TRIGGER, "group1")
                    .withSchedule(
                            SimpleScheduleBuilder.simpleSchedule()
                                    .withIntervalInSeconds(Constants.CONSENSUS_INTERVAL).repeatForever())
                    .build();

            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(consensusJob, consensusTrigger);

        } catch (SchedulerException e) {
            logger.error("Error in starting consensusJob(Heartbeat)");
        }
    }
}
