package com.nivelle.ddd.domain.leave.entity;

import com.nivelle.ddd.domain.leave.entity.valueobject.ApprovalType;
import com.nivelle.ddd.domain.leave.entity.valueobject.Approver;
import lombok.Data;

@Data
public class ApprovalInfo {

    String approvalInfoId;
    Approver approver;
    ApprovalType approvalType;
    String msg;
    long time;

}
