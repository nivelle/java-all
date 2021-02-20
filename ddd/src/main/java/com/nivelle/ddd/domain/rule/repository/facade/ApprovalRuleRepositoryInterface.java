package com.nivelle.ddd.domain.rule.repository.facade;

import com.nivelle.ddd.domain.rule.entity.ApprovalRule;

public interface ApprovalRuleRepositoryInterface {

    int getLeaderMaxLevel(ApprovalRule rule);
}
