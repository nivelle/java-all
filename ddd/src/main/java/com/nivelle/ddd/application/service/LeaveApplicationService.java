package com.nivelle.ddd.application.service;

import com.nivelle.ddd.domain.leave.entity.Leave;
import com.nivelle.ddd.domain.leave.entity.valueobject.Approver;
import com.nivelle.ddd.domain.leave.service.LeaveDomainService;
import com.nivelle.ddd.domain.person.entity.Person;
import com.nivelle.ddd.domain.person.service.PersonDomainService;
import com.nivelle.ddd.domain.rule.service.ApprovalRuleDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaveApplicationService {

    @Autowired
    LeaveDomainService leaveDomainService;
    @Autowired
    PersonDomainService personDomainService;

    @Autowired
    ApprovalRuleDomainService approvalRuleDomainService;

    /**
     * 创建一个请假申请并为审批人生成任务
     *
     * @param leave
     */
    public void createLeaveInfo(Leave leave) {
        //get approval leader max level by rule
        int leaderMaxLevel = approvalRuleDomainService.getLeaderMaxLevel(leave.getApplicant().getPersonType(), leave.getType().toString(), leave.getDuration());
        //find next approver
        Person approver = personDomainService.findFirstApprover(leave.getApplicant().getPersonId(), leaderMaxLevel);
        leaveDomainService.createLeave(leave, leaderMaxLevel, Approver.fromPerson(approver));
    }

    /**
     * 更新请假单基本信息
     *
     * @param leave
     */
    public void updateLeaveInfo(Leave leave) {
        leaveDomainService.updateLeaveInfo(leave);
    }

    /**
     * 提交审批，更新请假单信息
     *
     * @param leave
     */
    public void submitApproval(Leave leave) {
        //find next approver
        Person approver = personDomainService.findNextApprover(leave.getApprover().getPersonId(), leave.getLeaderMaxLevel());
        leaveDomainService.submitApproval(leave, Approver.fromPerson(approver));
    }

    public Leave getLeaveInfo(String leaveId) {
        return leaveDomainService.getLeaveInfo(leaveId);
    }

    public List<Leave> queryLeaveInfosByApplicant(String applicantId) {
        return leaveDomainService.queryLeaveInfosByApplicant(applicantId);
    }

    public List<Leave> queryLeaveInfosByApprover(String approverId) {
        return leaveDomainService.queryLeaveInfosByApprover(approverId);
    }
}