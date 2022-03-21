package Server;

import java.io.IOException;
import Messaging.Sender;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import Heartbeat.Heartbeat;

import java.util.ArrayList;

public class LeaderElector {
    private static ArrayList<Server> Servers = new ArrayList<Server>();
    private static Server currentLeader;
    private static Server currentServer;
    private static ArrayList<Server> availableServers = new ArrayList<Server>();
    private static ServerState currentServerState; 


    private static final Logger logger = LogManager.getLogger(LeaderElector.class);
    
    private LeaderElector() {
	}

    //need review
    public static Server getLeader(){
    	logger.debug("START LEADER ELECTION");
        currentServerState = ServerState.getServerState();

        Servers.clear();
        Servers.addAll(currentServerState.getServersHashmap().values());

        String serverName = currentServerState.getServerName();
        currentServer = currentServerState.getServerByName(serverName);
        
        selectNewLeader();
        return currentLeader;
    }



    private static void selectNewLeader(){
        LeaderElector.availableServers.clear();
        JSONObject startElectionMsg = LeaderElector.createElectionMessage("start_election", LeaderElector.currentServer);
        for (Server server: Servers){
            if ((server.getServerName()).compareTo(currentServer.getServerName())>0){
                try {
                    sendElectionMessage(server,startElectionMsg);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        
        try{
            Thread.sleep(500);
        }
        catch(InterruptedException ex){
            Thread.currentThread().interrupt();
        }

        //thread1 after time out
        Server maxServer = LeaderElector.getMaxAvailablServer();
        JSONObject nominateMsg = LeaderElector.createElectionMessage("nomination", LeaderElector.currentServer);
        try {
        	if (!(maxServer.getServerName().equals(currentServer.getServerName()))) {
        		sendElectionMessage(maxServer, nominateMsg);
        	}else {
        		informIamCoordinatorMsg();
        	}
        } catch (IOException e) {
            // TODO Auto-generated catch block
        	logger.error("CAN'T SEND :"+e.getMessage());
//            e.printStackTrace();
        }
        //nominate that server
        LeaderElector.availableServers.clear();//reset LeaderElector.availableServers
        LeaderElector.currentLeader = maxServer;
        // leaderStatus = true; //set laeder and leaderstatus
    }

    public static void processStartElectionMsg(JSONObject response){
    	logger.debug("START: processStartElectionMsg()");
        String senderServerName = response.getString("senderServerName");
        Server senderServer = ServerState.getServerState().getServerByName(senderServerName);
        JSONObject answerElectionMsg = LeaderElector.createElectionMessage("answer_election", LeaderElector.currentServer);
        logger.debug("processStartElectionMsg() "+ answerElectionMsg.toString());
        try {
            sendElectionMessage(senderServer,answerElectionMsg);
        } catch (IOException e) {
            // TODO Auto-generated catch block
        	logger.error("ERROR: processStartElectionMsg()");
            e.printStackTrace();
        }
        logger.debug("END: processStartElectionMsg()");
    }

    public static void processAnswerElectionMsg(JSONObject response){
    	logger.debug("START: processAnswerElectionMsg()");
        String senderServerName = response.getString("senderServerName");
        Server senderServer = ServerState.getServerState().getServerByName(senderServerName);
        LeaderElector.availableServers.add(senderServer);    
    	logger.debug("END: processAnswerElectionMsg()");
    }

    public static void processNominationMsg(JSONObject response){
        LeaderElector.currentLeader = currentServer;
        informIamCoordinatorMsg();
    }

    public static void processInformCoordinatorMsg(JSONObject response){
        String senderServerName = response.getString("senderServerName");
        Server senderServer = ServerState.getServerState().getServerByName(senderServerName);
        LeaderElector.currentLeader = senderServer;   
        ServerState.getServerState().setLeaderServer(senderServer);
    }

    public static void serverRecovery(){
        JSONObject IamUpMsg = LeaderElector.createElectionMessage("IamUp", LeaderElector.currentServer);
        LeaderElector.availableServers.clear();
        for (Server server: LeaderElector.availableServers){
            try {
                sendElectionMessage(server, IamUpMsg);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        try{
            Thread.sleep(500);
        }
        catch(InterruptedException ex){
            Thread.currentThread().interrupt();
        }

        Server maxServer = getMaxAvailablServer();
        LeaderElector.currentLeader = maxServer;
        if(maxServer == currentServer){
            informIamCoordinatorMsg();
        }
    }

    public static void processIamUpMsg(JSONObject response){
        String senderServerName = response.getString("senderServerName");
        Server senderServer = ServerState.getServerState().getServerByName(senderServerName);
        JSONObject viewMsg = LeaderElector.createElectionMessage("view", LeaderElector.currentServer);
        try {
            sendElectionMessage(senderServer, viewMsg);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void processViewMsg(JSONObject response){
        String senderServerName = response.getString("senderServerName");
        Server senderServer = ServerState.getServerState().getServerByName(senderServerName);
        LeaderElector.availableServers.add(senderServer);  
    }


    // Message Types
    // 1. start_election
    // 2. answer_election
    // 3. nomination
    // 4. inform_coordinator
    // 5. IamUp
    // 6. view

    private static void sendElectionMessage(Server server, JSONObject obj) throws IOException{
        Sender.sendRespond(server.getServerSocketConnection(), obj);
    }

    private static JSONObject createElectionMessage(String electionMsgType, Server currentServer){
        JSONObject msg = new JSONObject();
        msg.put("type", "election").put("electionMsgType",electionMsgType).put("senderServerName",ServerState.getServerState().getServerName());
        return msg;
    }

    private static Server getMaxAvailablServer(){
        Server maxServer = currentServer;
        String maxServerName = currentServer.getServerName();
        logger.debug("CURRENT "+ maxServer.getServerName());
        if(LeaderElector.availableServers.size()>0){
            //select server with maximum id
            for (Server server: LeaderElector.availableServers){
                if((server.getServerName()).compareTo(maxServerName)>0){
                    maxServerName = server.getServerName();
                    maxServer = server;
                    logger.debug("OTHER"+server.toString());
                }
            }
        }
        return maxServer;
    }

    private static void informIamCoordinatorMsg(){
        JSONObject informCoordinatorMsg = LeaderElector.createElectionMessage("inform_coordinator", LeaderElector.currentServer);
        for (Server server: LeaderElector.availableServers){
            try {
                sendElectionMessage(server, informCoordinatorMsg);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void setServers(ArrayList<Server> Servers){
        LeaderElector.Servers = Servers;
    }

    public static void setcurrentLeader(Server leader){
        LeaderElector.currentLeader = leader;
    }

    public static void setcurrentServer(Server currentServer){
        LeaderElector.currentServer = currentServer;
    }

    public static void setcurrentServerState(ServerState currentServerState){
        LeaderElector.currentServerState = currentServerState;
    }

}
