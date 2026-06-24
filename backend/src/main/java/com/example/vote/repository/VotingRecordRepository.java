package com.example.vote.repository;

import com.example.vote.dto.VotingRecordDTO;

import java.util.List;

/**
 * 投票紀錄 Repository (資料層)
 * 所有方法皆透過 Stored Procedure 存取資料庫。
 */
public interface VotingRecordRepository {

    /**
     * 執行單一項目的投票（寫入紀錄並累加票數），為原子操作。
     * 由 Service 層在 @Transactional 中對多選的每個項目逐一呼叫。
     */
    void castVote(String voterName, Long itemId);

    List<VotingRecordDTO> findAll(Long itemId);
}
