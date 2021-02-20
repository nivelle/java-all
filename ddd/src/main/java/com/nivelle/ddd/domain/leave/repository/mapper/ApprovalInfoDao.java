package com.nivelle.ddd.domain.leave.repository.mapper;

import com.nivelle.ddd.domain.leave.repository.po.ApprovalInfoPO;
import com.nivelle.ddd.domain.leave.repository.po.LeavePO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalInfoDao extends JpaRepository<ApprovalInfoPO, String> {

    List<LeavePO> queryByApplicantId(String applicantId);

    List<ApprovalInfoPO> queryByLeaveId(String leaveId);

}
