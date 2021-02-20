package com.nivelle.ddd.domain.leave.repository.mapper;

import com.nivelle.ddd.domain.leave.repository.po.LeaveEventPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveEventDao extends JpaRepository<LeaveEventPO, String> {
}
