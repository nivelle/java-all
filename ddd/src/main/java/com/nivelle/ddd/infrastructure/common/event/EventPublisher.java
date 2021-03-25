package com.nivelle.ddd.infrastructure.common.event;

import com.nivelle.ddd.domain.leave.event.LeaveEvent;
import org.springframework.stereotype.Service;

@Service
public class EventPublisher {

    public void publish(LeaveEvent event) {
        //send to MQ
        //mq.send(event);
    }
}