package com.nivelle.ddd.domain.leave.repository.persistence;

import com.nivelle.ddd.domain.leave.repository.facade.LeaveRepositoryInterface;
import com.nivelle.ddd.domain.leave.repository.mapper.ApprovalInfoDao;
import com.nivelle.ddd.domain.leave.repository.mapper.LeaveDao;
import com.nivelle.ddd.domain.leave.repository.mapper.LeaveEventDao;
import com.nivelle.ddd.domain.leave.repository.po.ApprovalInfoPO;
import com.nivelle.ddd.domain.leave.repository.po.LeaveEventPO;
import com.nivelle.ddd.domain.leave.repository.po.LeavePO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * persist entity and handle event in repository
 */
@Repository
public class LeaveRepositoryImpl implements LeaveRepositoryInterface {

    @Autowired
    LeaveDao leaveDao;
    @Autowired
    ApprovalInfoDao approvalInfoDao;
    @Autowired
    LeaveEventDao leaveEventDao;

    public void save(LeavePO leavePO) {
        //persist leave entity
        leaveDao.save(leavePO);
        //set leave_id for approvalInfoPO after save leavePO
        leavePO.getHistoryApprovalInfoPOList().stream().forEach(approvalInfoPO -> approvalInfoPO.setLeaveId(leavePO.getId()));
        approvalInfoDao.saveAll(leavePO.getHistoryApprovalInfoPOList());
    }

    public void saveEvent(LeaveEventPO leaveEventPO) {
        leaveEventDao.save(leaveEventPO);
    }

    @Override
    public LeavePO findById(String id) {
        return leaveDao.findById(id)
                .orElseThrow(() -> new RuntimeException("leave not found"));
    }

    @Override
    public List<LeavePO> queryByApplicantId(String applicantId) {
        List<LeavePO> leavePOList = leaveDao.queryByApplicantId(applicantId);
        leavePOList.stream()
                .forEach(leavePO -> {
                    List<ApprovalInfoPO> approvalInfoPOList = approvalInfoDao.queryByLeaveId(leavePO.getId());
                    leavePO.setHistoryApprovalInfoPOList(approvalInfoPOList);
                });
        return leavePOList;
    }

    @Override
    public List<LeavePO> queryByApproverId(String approverId) {
        List<LeavePO> leavePOList = leaveDao.queryByApproverId(approverId);
        leavePOList.stream()
                .forEach(leavePO -> {
                    List<ApprovalInfoPO> approvalInfoPOList = approvalInfoDao.queryByLeaveId(leavePO.getId());
                    leavePO.setHistoryApprovalInfoPOList(approvalInfoPOList);
                });
        return leavePOList;
    }

}
