package com.nivelle.guide.distributed.leader;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2019/07/06
 */
public interface LeaderElectionAware {

    void onElectionEvent(LeaderElectionSupport.EventType eventType);

}
