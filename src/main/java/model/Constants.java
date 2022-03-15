package model;

public abstract class Constants {
	
	private Constants() {} // no init

    public static final String CONSENSUS_JOB = "ConsensusJob".toUpperCase();
    public static final String CONSENSUS_JOB_TRIGGER = "ConsensusJobTrigger".toUpperCase();
    public static final String GOSSIP_JOB = "GossipJob".toUpperCase();
    public static final String GOSSIP_JOB_TRIGGER = "GossipJobTrigger".toUpperCase();
}
