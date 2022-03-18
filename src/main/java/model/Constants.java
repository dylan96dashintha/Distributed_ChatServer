package model;

public abstract class Constants {
	
	private Constants() {} // no init

    public static final String CONSENSUS_JOB = "ConsensusJob".toUpperCase();
    public static final String CONSENSUS_JOB_TRIGGER = "ConsensusJobTrigger".toUpperCase();
    public static final String GOSSIP_JOB = "GossipJob".toUpperCase();
    public static final String GOSSIP_JOB_TRIGGER = "GossipJobTrigger".toUpperCase();
    
    public static final long REQUEST_INTERVAL = 1500; //1500ms
    public static final long TIMEOUT_INTERVAL = 3000; //3000ms
    public static final int LEADER_DOWN_RECOGNITION_WAITING = 1500; //1500ms
    public static final int ALIVE_INTERVAL = 3; //3000ms
    public static final int ALIVE_ERROR_FACTOR = 5; //5times
    public static final int CONSENSUS_INTERVAL = 10; //10000ms
    public static final int CONSENSUS_VOTE_DURATION = 5000; //5000ms
}
