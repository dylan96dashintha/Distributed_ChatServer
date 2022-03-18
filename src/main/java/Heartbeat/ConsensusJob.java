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

    private ServerMessage serverMessage = ServerMessage.getInstance();
    private Gossiping gossiping = new Gossiping();
	
    @Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
    	if (!ServerState.getServerState().onGoingConsensus().get()) {
            // This is a leader based Consensus.
            // If no leader elected at the moment then no consensus task to perform.
            if (ServerState.getServerState().isLeaderElected()) {
            	ServerState.getServerState().onGoingConsensus().set(true);
                performConsensus(context); // critical region
                ServerState.getServerState().onGoingConsensus().set(false);
            }
        } else {
        	logger.info("There seems to be on going consensus at the moment!");
//            System.out.println("[SKIP] There seems to be on going consensus at the moment, skip.");
        }
		
	}
    
    private void performConsensus(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String consensusVoteDuration = dataMap.get("consensusVoteDuration").toString();

        String suspectServerId = null;

        // Initialize vote set
        ServerState.getServerState().getVoteSet().put("YES", 0);
        ServerState.getServerState().getVoteSet().put("NO", 0);

        String leaderServerId = ServerState.getServerState().getLeaderServer().getServerName();
        String myServerId = ServerState.getServerState().getServerName();

        // if I am leader, and suspect someone, I want to start voting to KICK him!
        if (myServerId.equals(leaderServerId)) {
            // find the next suspect to vote and break the loop
            for (String serverId : ServerState.getServerState().getSuspectList().keySet()) {
                if (ServerState.getServerState().getSuspectList().get(serverId).equals("SUSPECTED")) {
                    suspectServerId = serverId;
                    break;
                }
            }

            ArrayList<Server> serverList = new ArrayList<>();
            for (String serverid : ServerState.getServerState().getServersHashmap().keySet()) {
                if (!serverid.equals(ServerState.getServerState().getServerName()) && ServerState.getServerState().getSuspectList().get(serverid).equals("NOT_SUSPECTED")) {
                    serverList.add(ServerState.getServerState().getServersHashmap().get(serverid));
                }
            }

            //got a suspect
            if (suspectServerId != null) {

            	ServerState.getServerState().getVoteSet().put("YES", 1); // I suspect it already, so I vote yes.
                JSONObject startVoteMessage = new JSONObject();
                startVoteMessage = serverMessage.startVoteMessage(ServerState.getServerState().getServerName(), suspectServerId);
                try {
                    gossiping.sendServerBroadcast(startVoteMessage, serverList);
                    logger.info("Leader calling for vote to kick suspect-server: " + startVoteMessage);
                } catch (Exception e) {
                	logger.error("Leader calling for vote to kick suspect-server is failed");
                }

                //wait for consensus vote duration period
                try {
                    Thread.sleep(Integer.parseInt(consensusVoteDuration));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                logger.info((String.format("Consensus votes to kick server [%s]: %s", suspectServerId, ServerState.getServerState().getVoteSet())));

                if (ServerState.getServerState().getVoteSet().get("YES") > ServerState.getServerState().getVoteSet().get("NO")) {

                    JSONObject notifyServerDownMessage = new JSONObject();
                    notifyServerDownMessage = serverMessage.notifyServerDownMessage(suspectServerId);
                    try {
                        gossiping.sendServerBroadcast(notifyServerDownMessage, serverList);
                        logger.info(" Notify server " + suspectServerId + " down. Removing from ["+ ServerState.getServerState().getServerName() + "]");
                        ServerState.getServerState().removeSuspectServer(suspectServerId);

                    } catch (Exception e) {
                        logger.error( suspectServerId + "Removing is failed");
                    }

                    logger.info("Number of servers in group: " + ServerState.getServerState().getServersHashmap().size());
                }
            }
        }
    }
    
    public static void startVoteMessageHandler(JSONObject j_object){
    	
        ServerMessage serverMessage = ServerMessage.getInstance();

        String suspectServerId = (String) j_object.get("suspectServerId");
        String serverId = (String)j_object.get("serverId");
        String mySeverId = ServerState.getServerState().getServerName();

        if (ServerState.getServerState().getSuspectList().containsKey(suspectServerId)) {
            if (ServerState.getServerState().getSuspectList().get(suspectServerId).equals("SUSPECTED")) {

                JSONObject answerVoteMessage = new JSONObject();
                answerVoteMessage = serverMessage.answerVoteMessage(suspectServerId, "YES", mySeverId);
                try {
                	Sender.sendRespond(ServerState.getServerState().getServersHashmap().get(serverId).getServerSocketConnection(), answerVoteMessage);
                    logger.info(String.format("Voting on suspected server: [%s] vote: YES", suspectServerId));
                } catch (Exception e) {
                    logger.error("Voting on suspected server is failed");
                }

            } else {

                JSONObject answerVoteMessage = new JSONObject();
                answerVoteMessage = serverMessage.answerVoteMessage(suspectServerId, "NO", mySeverId);
                try {
                	Sender.sendRespond(ServerState.getServerState().getServersHashmap().get(serverId).getServerSocketConnection(), answerVoteMessage);
                	logger.info(String.format("Voting on suspected server: [%s] vote: NO", suspectServerId));
                } catch (Exception e) {
                	logger.error("Voting on suspected server is failed");
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

        String suspectServerId = (String)j_object.get("serverId");
        logger.info("Server down notification received. Removing "+ suspectServerId +"server from ["+ ServerState.getServerState().getServerName() + "]");
        ServerState.getServerState().removeSuspectServer(suspectServerId);
    }

}
