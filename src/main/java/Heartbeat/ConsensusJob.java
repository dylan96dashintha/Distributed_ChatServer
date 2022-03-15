package Heartbeat;

import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import Gossiping.Gossiping;
import Messaging.Sender;
import Messaging.ServerMessage;
import Server.Server;
import Server.ServerState;

public class ConsensusJob implements Job{
	private static final Logger logger = LogManager.getLogger(ConsensusJob.class);
	
	private ServerState serverState = ServerState.getServerState();
    private Server leaderState = serverState.getLeaderServer();
    private ServerMessage serverMessage = ServerMessage.getInstance();
    private Gossiping gossiping = new Gossiping();
	
    @Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
    	if (!serverState.onGoingConsensus().get()) {
            // This is a leader based Consensus.
            // If no leader elected at the moment then no consensus task to perform.
            if (leaderState.isLeaderElected()) { //TODO : isLeaderElected yet to be implemented
                serverState.onGoingConsensus().set(true);
                performConsensus(context); // critical region
                serverState.onGoingConsensus().set(false);
            }
        } else {
        	logger.info("There seems to be on going consensus(ex: leader election) at the moment!");
//            System.out.println("[SKIP] There seems to be on going consensus at the moment, skip.");
        }
		
	}
    
    private void performConsensus(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String consensusVoteDuration = dataMap.get("consensusVoteDuration").toString();

        String suspectServerId = null;

        // Initialize vote set
        serverState.getVoteSet().put("YES", 0);
        serverState.getVoteSet().put("NO", 0);

        String leaderServerId = leaderState.getServerName();
        String myServerId = serverState.getServerName();

        // if I am leader, and suspect someone, I want to start voting to KICK him!
        if (myServerId.equals(leaderServerId)) {

            // find the next suspect to vote and break the loop
            for (String serverId : serverState.getSuspectList().keySet()) {
                if (serverState.getSuspectList().get(serverId).equals("SUSPECTED")) {
                    suspectServerId = serverId;
                    break;
                }
            }

            ArrayList<Server> serverList = new ArrayList<>();
            for (String serverid : serverState.getServersHashmap().keySet()) {
                if (!serverid.equals(serverState.getServerName()) && serverState.getSuspectList().get(serverid).equals("NOT_SUSPECTED")) {
                    serverList.add(serverState.getServersHashmap().get(serverid));
                }
            }

            //got a suspect
            if (suspectServerId != null) {

                serverState.getVoteSet().put("YES", 1); // I suspect it already, so I vote yes.
                JSONObject startVoteMessage = new JSONObject();
                startVoteMessage = serverMessage.startVoteMessage(serverState.getServerName(), suspectServerId);
                try {
                    gossiping.sendServerBroadcast(startVoteMessage, serverList);
                    logger.debug("Leader calling for vote to kick suspect-server: " + startVoteMessage);
                } catch (Exception e) {
                	logger.debug("Leader calling for vote to kick suspect-server is failed");
                }

                //wait for consensus vote duration period
                try {
                    Thread.sleep(Integer.parseInt(consensusVoteDuration) * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                logger.debug((String.format("INFO : Consensus votes to kick server [%s]: %s", suspectServerId, serverState.getVoteSet())));

                if (serverState.getVoteSet().get("YES") > serverState.getVoteSet().get("NO")) {

                    JSONObject notifyServerDownMessage = new JSONObject();
                    notifyServerDownMessage = serverMessage.notifyServerDownMessage(suspectServerId);
                    try {
                        gossiping.sendServerBroadcast(notifyServerDownMessage, serverList);
                        logger.debug(" Notify server " + suspectServerId + " down. Removing...");
//                        serverState.removeServer(suspectServerId);
                        leaderState.removeRemoteChatRoomsClientsByServerId(suspectServerId); //TODO: removeRemoteChatRoomsClientsByServerId yet to be implemented
                        serverState.removeServerInCountList(suspectServerId);
                        serverState.removeServerInSuspectList(suspectServerId);

                    } catch (Exception e) {
                        logger.error( suspectServerId + "Removing is failed");
                    }

                    logger.debug("INFO : Number of servers in group: " + serverState.getServersHashmap().size());
                }
            }
        }
    }
    
    public static void startVoteMessageHandler(JSONObject j_object){
    	
    	ServerState serverState = ServerState.getServerState();
        ServerMessage serverMessage = ServerMessage.getInstance();

        String suspectServerId = (String) j_object.get("suspectServerId");
        String serverId = (String)j_object.get("serverId");
        String mySeverId = serverState.getServerName();

        if (serverState.getSuspectList().containsKey(suspectServerId)) {
            if (serverState.getSuspectList().get(suspectServerId).equals("SUSPECTED")) {

                JSONObject answerVoteMessage = new JSONObject();
                answerVoteMessage = serverMessage.answerVoteMessage(suspectServerId, "YES", mySeverId);
                try {
                	Sender.sendRespond(serverState.getServersHashmap().get(serverState.getLeaderServer()).getServerSocketConnection(), answerVoteMessage);
                    logger.debug(String.format("Voting on suspected server: [%s] vote: YES", suspectServerId));
                } catch (Exception e) {
                    logger.error("Voting on suspected server is failed");
                }

            } else {

                JSONObject answerVoteMessage = new JSONObject();
                answerVoteMessage = serverMessage.answerVoteMessage(suspectServerId, "NO", mySeverId);
                try {
                	Sender.sendRespond(serverState.getServersHashmap().get(serverState.getLeaderServer()).getServerSocketConnection(), answerVoteMessage);
                	logger.debug(String.format("Voting on suspected server: [%s] vote: NO", suspectServerId));
                } catch (Exception e) {
                	logger.debug("Voting on suspected server is failed");
                }
            }
        }

    }
    
    public static void answerVoteHandler(JSONObject j_object){

        ServerState serverState = ServerState.getServerState();

        String suspectServerId = (String)j_object.get("suspectServerId");
        String vote = (String) j_object.get("vote");
        String votedBy = (String)j_object.get("votedBy");

        Integer voteCount = serverState.getVoteSet().get(vote);

        logger.info(String.format("Receiving voting to kick [%s]: [%s] voted by server: [%s]", suspectServerId, vote, votedBy));

        if (voteCount == null) {
            serverState.getVoteSet().put(vote, 1);
        } else {
            serverState.getVoteSet().put(vote, voteCount + 1);
        }

    }
    
    public static void notifyServerDownMessageHandler(JSONObject j_object){

        ServerState serverState = ServerState.getServerState();
        Server leaderState = serverState.getLeaderServer();

        String serverId = (String)j_object.get("serverId");

        logger.debug("Server down notification received. Removing server: " + serverId);

//        serverState.removeServer(serverId);
        leaderState.removeRemoteChatRoomsClientsByServerId(serverId); //TODO: removeRemoteChatRoomsClientsByServerId yet to be implemented
        serverState.removeServerInCountList(serverId);
        serverState.removeServerInSuspectList(serverId);
    }

}
