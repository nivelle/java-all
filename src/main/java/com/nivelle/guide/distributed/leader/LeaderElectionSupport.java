package com.nivelle.guide.distributed.leader;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.*;

/**
 *
 * @author fuxinzhong
 * @date 2019/07/06
 */

@Slf4j
public class LeaderElectionSupport implements Watcher {


    private ZooKeeper zooKeeper;

    private State state;
    private Set<LeaderElectionAware> listeners;

    private String rootNodeName;
    private LeaderOffer leaderOffer;
    private String hostName;

    public LeaderElectionSupport() {
        state = State.STOP;
        listeners = Collections.synchronizedSet(new HashSet<LeaderElectionAware>());
    }

    /**
     * <p>
     * Start the election process. This method will create a leader offer,
     * determine its status, and either become the leader or become ready. If an
     * instance of {@link org.apache.zookeeper.ZooKeeper} has not yet been configured by the user, a
     * new instance is created using the connectString and sessionTime specified.
     * </p>
     * <p>
     * Any (anticipated) failures result in a failed event being sent to all
     * listeners.
     * </p>
     */
    public synchronized void start() {
        state = State.START;
        dispatchEvent(EventType.START);

        log.info("Starting leader election support");

        if (zooKeeper == null) {
            throw new IllegalStateException(
                    "No instance of zookeeper provided. Hint: use setZooKeeper()");
        }

        if (hostName == null) {
            throw new IllegalStateException(
                    "No hostname provided. Hint: use setHostName()");
        }

        try {
            makeOffer();
            determineElectionStatus();
        } catch (KeeperException e) {
            becomeFailed(e);
            return;
        } catch (InterruptedException e) {
            becomeFailed(e);
            return;
        }
    }

    /**
     * Stops all election services, revokes any outstanding leader offers, and
     * disconnects from ZooKeeper.
     */
    public synchronized void stop() {
        state = State.STOP;
        dispatchEvent(EventType.STOP_START);

        log.info("Stopping leader election support");

        if (leaderOffer != null) {
            try {
                zooKeeper.delete(leaderOffer.getNodePath(), -1);
                log.info("Removed leader offer {}", leaderOffer.getNodePath());
            } catch (InterruptedException e) {
                becomeFailed(e);
            } catch (KeeperException e) {
                becomeFailed(e);
            }
        }

        dispatchEvent(EventType.STOP_COMPLETE);
    }

    private void makeOffer() throws KeeperException, InterruptedException {
        state = State.OFFER;
        dispatchEvent(EventType.OFFER_START);

        leaderOffer = new LeaderOffer();

        leaderOffer.setHostName(hostName);
        leaderOffer.setNodePath(zooKeeper.create(rootNodeName + "/" + "n_",
                hostName.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL));

        log.debug("Created leader offer {}", leaderOffer);

        dispatchEvent(EventType.OFFER_COMPLETE);
    }

    private void determineElectionStatus() throws KeeperException,
            InterruptedException {

        state = State.DETERMINE;
        dispatchEvent(EventType.DETERMINE_START);

        String[] components = leaderOffer.getNodePath().split("/");

        leaderOffer.setId(Integer.valueOf(components[components.length - 1]
                .substring("n_".length())));

        List<LeaderOffer> leaderOffers = toLeaderOffers(zooKeeper.getChildren(
                rootNodeName, false));

        /*
         * For each leader offer, find out where we fit in. If we're first, we
         * become the leader. If we're not elected the leader, attempt to stat the
         * offer just less than us. If they exist, watch for their failure, but if
         * they don't, become the leader.
         */
        for (int i = 0; i < leaderOffers.size(); i++) {
            LeaderOffer leaderOffer = leaderOffers.get(i);

            if (leaderOffer.getId().equals(this.leaderOffer.getId())) {
                log.debug("There are {} leader offers. I am {} in line.",
                        leaderOffers.size(), i);

                dispatchEvent(EventType.DETERMINE_COMPLETE);

                if (i == 0) {
                    becomeLeader();
                } else {
                    becomeReady(leaderOffers.get(i - 1));
                }

                /* Once we've figured out where we are, we're done. */
                break;
            }
        }
    }

    private void becomeReady(LeaderOffer neighborLeaderOffer)
            throws KeeperException, InterruptedException {
        dispatchEvent(EventType.READY_START);

        log.info("{} not elected leader. Watching node:{}",
                leaderOffer.getNodePath(), neighborLeaderOffer.getNodePath());

        /*
         * Make sure to pass an explicit Watcher because we could be sharing this
         * zooKeeper instance with someone else.
         */
        Stat stat = zooKeeper.exists(neighborLeaderOffer.getNodePath(), this);

        if (stat != null) {
            log.debug(
                    "We're behind {} in line and they're alive. Keeping an eye on them.",
                    neighborLeaderOffer.getNodePath());
            state = State.READY;
            dispatchEvent(EventType.READY_COMPLETE);
        } else {
            /*
             * If the stat fails, the node has gone missing between the call to
             * getChildren() and exists(). We need to try and become the leader.
             */
            log
                    .info(
                            "We were behind {} but it looks like they died. Back to determination.",
                            neighborLeaderOffer.getNodePath());
            determineElectionStatus();
        }

    }

    private void becomeLeader() {
        state = State.ELECTED;
        dispatchEvent(EventType.ELECTED_START);

        log.info("Becoming leader with node:{}", leaderOffer.getNodePath());

        dispatchEvent(EventType.ELECTED_COMPLETE);
    }

    private void becomeFailed(Exception e) {
        log.error("Failed in state {} - Exception:{}", state, e);

        state = State.FAILED;
        dispatchEvent(EventType.FAILED);
    }

    /**
     * Fetch the (user supplied) hostname of the current leader. Note that by the
     * time this method returns, state could have changed so do not depend on this
     * to be strongly consistent. This method has to read all leader offers from
     * ZooKeeper to deterime who the leader is (i.e. there is no caching) so
     * consider the performance implications of frequent invocation. If there are
     * no leader offers this method returns null.
     *
     * @return hostname of the current leader
     * @throws org.apache.zookeeper.KeeperException
     * @throws InterruptedException
     */
    public String getLeaderHostName() throws KeeperException,
            InterruptedException {

        List<LeaderOffer> leaderOffers = toLeaderOffers(zooKeeper.getChildren(
                rootNodeName, false));

        if (leaderOffers.size() > 0) {
            return leaderOffers.get(0).getHostName();
        }

        return null;
    }

    private List<LeaderOffer> toLeaderOffers(List<String> strings)
            throws KeeperException, InterruptedException {

        List<LeaderOffer> leaderOffers = new ArrayList<LeaderOffer>(strings.size());

        /*
         * Turn each child of rootNodeName into a leader offer. This is a tuple of
         * the sequence number and the node name.
         */
        for (String offer : strings) {
            String hostName = new String(zooKeeper.getData(
                    rootNodeName + "/" + offer, false, null));

            leaderOffers.add(new LeaderOffer(Integer.valueOf(offer.substring("n_"
                    .length())), rootNodeName + "/" + offer, hostName));
        }

        /*
         * We sort leader offers by sequence number (which may not be zero-based or
         * contiguous) and keep their paths handy for setting watches.
         */
        Collections.sort(leaderOffers, new LeaderOffer.IdComparator());

        return leaderOffers;
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getType().equals(Event.EventType.NodeDeleted)) {
            if (!event.getPath().equals(leaderOffer.getNodePath())
                    && state != State.STOP) {
                log.debug(
                        "Node {} deleted. Need to run through the election process.",
                        event.getPath());
                try {
                    determineElectionStatus();
                } catch (KeeperException e) {
                    becomeFailed(e);
                } catch (InterruptedException e) {
                    becomeFailed(e);
                }
            }
        }
    }

    private void dispatchEvent(EventType eventType) {
        log.debug("Dispatching event:{}", eventType);

        synchronized (listeners) {
            if (listeners.size() > 0) {
                for (LeaderElectionAware observer : listeners) {
                    observer.onElectionEvent(eventType);
                }
            }
        }
    }

    /**
     * Adds {@code listener} to the list of listeners who will receive events.
     *
     * @param listener
     */
    public void addListener(LeaderElectionAware listener) {
        listeners.add(listener);
    }

    /**
     * Remove {@code listener} from the list of listeners who receive events.
     *
     * @param listener
     */
    public void removeListener(LeaderElectionAware listener) {
        listeners.remove(listener);
    }

    @Override
    public String toString() {
        return "{ state:" + state + " leaderOffer:" + leaderOffer + " zooKeeper:"
                + zooKeeper + " hostName:" + hostName + " listeners:" + listeners
                + " }";
    }

    /**
     * <p>
     * Gets the ZooKeeper root node to use for this service.
     * </p>
     * <p>
     * For instance, a root node of {@code /mycompany/myservice} would be the
     * parent of all leader offers for this service. Obviously all processes that
     * wish to contend for leader status need to use the same root node. Note: We
     * assume this node already exists.
     * </p>
     *
     * @return a znode path
     */
    public String getRootNodeName() {
        return rootNodeName;
    }

    /**
     * <p>
     * Sets the ZooKeeper root node to use for this service.
     * </p>
     * <p>
     * For instance, a root node of {@code /mycompany/myservice} would be the
     * parent of all leader offers for this service. Obviously all processes that
     * wish to contend for leader status need to use the same root node. Note: We
     * assume this node already exists.
     * </p>
     */
    public void setRootNodeName(String rootNodeName) {
        this.rootNodeName = rootNodeName;
    }

    /**
     * The {@link org.apache.zookeeper.ZooKeeper} instance to use for all operations. Provided this
     * overrides any connectString or sessionTimeout set.
     */
    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }

    public void setZooKeeper(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    /**
     * The hostname of this process. Mostly used as a convenience for logging and
     * to respond to {@link #getLeaderHostName()} requests.
     */
    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * The type of event.
     */
    public static enum EventType {
        START, OFFER_START, OFFER_COMPLETE, DETERMINE_START, DETERMINE_COMPLETE, ELECTED_START, ELECTED_COMPLETE, READY_START, READY_COMPLETE, FAILED, STOP_START, STOP_COMPLETE,
    }

    /**
     * The internal state of the election support service.
     */
    public static enum State {
        START, OFFER, DETERMINE, ELECTED, READY, FAILED, STOP
    }

}
