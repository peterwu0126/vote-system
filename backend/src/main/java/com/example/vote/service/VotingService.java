package com.example.vote.service;

import com.example.vote.dto.VoteRequest;
import com.example.vote.dto.VotingItemDTO;
import com.example.vote.dto.VotingRecordDTO;

import java.util.List;

/**
 * 投票業務邏輯介面 (業務層)
 */
public interface VotingService {

    /**
     * 執行投票，支援多選。
     * 同一次請求中對多個投票項目的異動必須全部成功，否則全部回滾。
     *
     * @return 本次被投票項目的最新狀態(含累積票數)，供 201 Created 回應內容使用
     */
    List<VotingItemDTO> vote(VoteRequest request);

    List<VotingRecordDTO> getRecords(Long itemId);
}
