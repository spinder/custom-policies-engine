package org.hawkular.alerts.engine.impl;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.hawkular.alerts.engine.impl.PartitionManagerImpl.PartitionEntry;
import org.hawkular.commons.log.MsgLogger;
import org.hawkular.commons.log.MsgLogging;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Testing https://en.wikipedia.org/wiki/Consistent_hashing as a method to partition triggers
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class DistributionTest {

    private static final MsgLogger logger = MsgLogging.getMsgLogger(DistributionTest.class);

    public static List<PartitionEntry> generateTriggers(int numTenants, int numTriggers) {
        List<PartitionEntry> triggers = new ArrayList<>();
        for (int i = 0; i < numTenants; i++) {
            for (int j = 0; j < numTriggers; j++) {
                triggers.add(new PartitionEntry("tenant_" + i, "trigger_" + j));
            }
        }
        return triggers;
    }

    public static int comparePartitions(Map oldPartition, Map newPartition) {
        int changes = 0;
        for (Object oldKey : oldPartition.keySet()) {
            if (!newPartition.get(oldKey).equals(oldPartition.get(oldKey))) {
                changes++;
            }
        }
        return changes;
    }

    public static void print(String s) {
        logger.debug(s);
    }

    @Test
    public void consistentHashTest() {

        HashFunction md5 = Hashing.md5();
        List<PartitionEntry> triggers = generateTriggers(3, 1000);

        Map<Integer, Integer> newPartition;
        Map<Integer, Integer> oldPartition;

        print("initial - test 2 servers");
        newPartition = new HashMap<>();
        for (PartitionEntry trigger : triggers) {
            newPartition.put(trigger.hashCode(), Hashing.consistentHash(md5.hashInt(trigger.hashCode()), 2));
        }

        for (int buckets = 3; buckets < 10; buckets++) {

            print("test " + buckets + " servers");
            oldPartition = newPartition;
            newPartition = new HashMap<>();
            for (PartitionEntry trigger : triggers) {
                newPartition.put(trigger.hashCode(), Hashing.consistentHash(md5.hashInt(trigger.hashCode()), buckets));
            }

            int changes = comparePartitions(oldPartition, newPartition);
            print("Changes from " + (buckets - 1) + "  to " + buckets + " servers: " + changes + " of " +
                    oldPartition.size());
            print("" + (((float)changes / (float)oldPartition.size()) * 100) + " % moved");
            print("K(" + oldPartition.size() +")/n(" + buckets + "): " + ((float)oldPartition.size() / (float)buckets));

        }

        for (int buckets = 10; buckets > 3; buckets--) {

            print("test " + buckets + " servers");
            oldPartition = newPartition;
            newPartition = new HashMap<>();
            for (PartitionEntry trigger : triggers) {
                newPartition.put(trigger.hashCode(), Hashing.consistentHash(md5.hashInt(trigger.hashCode()), buckets));
            }

            int changes = comparePartitions(oldPartition, newPartition);
            print("Changes from " + (buckets) + "  to " + (buckets - 1) + " servers: " + changes + " of " +
                    oldPartition.size());
            print("" + (((float)changes / (float)oldPartition.size()) * 100) + " % moved");
            print("K(" + oldPartition.size() +")/n(" + buckets + "): " + ((float)oldPartition.size() / (float)buckets));

        }
    }

    @Test
    public void mappedServersTest() {

        Map<Integer, String> servers = new HashMap<>();
        servers.put(0, "server0");
        servers.put(1, "server1");

        HashFunction md5 = Hashing.md5();
        List<PartitionEntry> triggers = generateTriggers(3, 1000);

        Map<Integer, String> newPartition;
        Map<Integer, String> oldPartition;

        print("initial - test 2 servers " + servers.toString());
        newPartition = new HashMap<>();
        for (PartitionEntry trigger : triggers) {
            newPartition.put(trigger.hashCode(),
                    servers.get(Hashing.consistentHash(md5.hashInt(trigger.hashCode()), 2)));
        }

        for (int buckets = 3; buckets < 10; buckets++) {

            servers.put(buckets - 1, "server" + (buckets -1));
            print("test " + buckets + " servers " + servers.toString());

            oldPartition = newPartition;
            newPartition = new HashMap<>();
            for (PartitionEntry trigger : triggers) {
                newPartition.put(trigger.hashCode(),
                        servers.get(Hashing.consistentHash(md5.hashInt(trigger.hashCode()), buckets)));
            }

            int changes = comparePartitions(oldPartition, newPartition);
            print("Changes from " + (buckets - 1) + "  to " + buckets + " servers: " + changes + " of " +
                    oldPartition.size());
            print("" + (((float)changes / (float)oldPartition.size()) * 100) + " % moved");
            print("K(" + oldPartition.size() +")/n(" + buckets + "): " + ((float)oldPartition.size() / (float)buckets));

        }
    }

    @Test
    public void bucketsServers() {

        PartitionManagerImpl pm = new PartitionManagerImpl();

        List<PartitionEntry> entries = generateTriggers(3, 1000);

        Map<Integer, Integer> buckets = new HashMap<>();
        buckets.put(0, 1000);
        buckets.put(1, 1001);


        print("initial - test 2 servers " + buckets.toString());

        Map<PartitionEntry, Integer> newPartition;
        Map<PartitionEntry, Integer> oldPartition;

        newPartition = pm.calculatePartition(entries, buckets);

        for (int newbucket = 3; newbucket < 10; newbucket++) {
            buckets.put(newbucket - 1, newbucket + 1000);
            oldPartition = newPartition;
            newPartition = pm.calculatePartition(entries, buckets);
            int changes = comparePartitions(oldPartition, newPartition);
            print("Changes from " + (newbucket - 1) + "  to " + newbucket + " servers: " + changes + " of " +
                    oldPartition.size());
            print("" + (((float)changes / (float)oldPartition.size()) * 100) + " % moved");
            print("K(" + oldPartition.size() +")/n(" + buckets + "): "
                    + ((float)oldPartition.size() / (float)newbucket));
        }
    }


}
