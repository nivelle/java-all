package com.nivelle.guide.distributed.leader;

import lombok.*;

import java.util.Comparator;

/**
 *
 * @author fuxinzhong
 * @date 2019/07/06
 */
@Setter
@Getter
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class LeaderOffer {

    private Integer id;
    private String nodePath;
    private String hostName;


    public static class IdComparator implements Comparator<LeaderOffer> {

        @Override
        public int compare(LeaderOffer o1, LeaderOffer o2) {
            return o1.getId().compareTo(o2.getId());
        }

    }
}
