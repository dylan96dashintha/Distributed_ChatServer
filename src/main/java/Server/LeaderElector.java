package Server;

import java.io.IOException;
import Messaging.Sender;
import org.json.JSONObject;

import java.util.ArrayList;

public class LeaderElector {
    private static ArrayList<Server> Servers = new ArrayList<Server>();
    private static Server currentLeader;
    private static Server currentServer;
    private static ArrayList<Server> availableServers = new ArrayList<Server>();
    private static ServerState currentServerState; 


    private LeaderElector() {
	}

    //need review
    public static Server getLeader(Server currentServer){
        selectNewLeader();
        return currentLeader;
    }



    private static void selectNewLeader(){
        availableServers.clear();
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
        //thread2
            //processElectionResponse
        //thread1 after time out
        Server maxServer;
        if(availableServers.size()>0){
             //select server with maximum id
             String maxServerName = availableServers.get(0).getServerName();
             
             for (Server server: availableServers){
                 if((server.getServerName()).compareTo(maxServerName)>0){
                    maxServerName = server.getServerName();
                     maxServer = server;
                 }
             }
        }
        else{
            maxServer = currentServer;
        }
        JSONObject nominateMsg = LeaderElector.createElectionMessage("nomination", LeaderElector.currentServer);
        try {
            sendElectionMessage(maxServer, nominateMsg);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //nominate that server
        availableServers.clear();//reset availableServers
        LeaderElector.currentLeader = maxServer;
        // leaderStatus = true; //set laeder and leaderstatus
    }

    public static void processStartElectionMsg(JSONObject response){
        String senderServerName = response.getString("senderServerName");
        Server senderServer = LeaderElector.currentServerState.getServerByName(senderServerName);
        JSONObject answerElectionMsg = LeaderElector.createElectionMessage("answer_election", LeaderElector.currentServer);
        try {
            sendElectionMessage(senderServer,answerElectionMsg);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void processAnswerElectionMsg(JSONObject response){
        String senderServerName = response.getString("senderServerName");
        Server senderServer = LeaderElector.currentServerState.getServerByName(senderServerName);
        availableServers.add(senderServer);    
    }

    public static void processNominationMsg(JSONObject response){
        LeaderElector.currentLeader = currentServer;
        JSONObject informCoordinatorMsg = LeaderElector.createElectionMessage("inform_coordinator", LeaderElector.currentServer);
        for (Server server: availableServers){
            try {
                sendElectionMessage(server, informCoordinatorMsg);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void processInformCoordinatorMsg(JSONObject response){
        String senderServerName = response.getString("senderServerName");
        Server senderServer = LeaderElector.currentServerState.getServerByName(senderServerName);
        LeaderElector.currentLeader = senderServer;   
    }

    public static void getNomination(){
        LeaderElector.currentLeader = currentServer;
        sendCoordinatorResponse(currentServer);
        leaderStatus = true;
    }

    public static void getCoordinatorResponse(Server newLeader){
        LeaderElector.currentLeader = newLeader;
        leaderStatus = true;
    }

    public static void sendCoordinatorResponse(Server newLeader) {
        for (Server server: Servers){
            // sendCoordinatorResponse
        }

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
        msg.put("type", "election").put("electionMsgType",electionMsgType).put("senderServerName",currentServer.getServerName());
        return msg;
    }

}
