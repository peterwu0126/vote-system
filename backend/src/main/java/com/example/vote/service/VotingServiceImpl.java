package com.example.vote.service;

import com.example.vote.common.BusinessException;
import com.example.vote.common.XssSanitizer;
import com.example.vote.dto.VoteRequest;
import com.example.vote.dto.VotingItemDTO;
import com.example.vote.dto.VotingRecordDTO;
import com.example.vote.repository.VotingItemRepository;
import com.example.vote.repository.VotingRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 投票業務邏輯實作 (業務層)
 *
 * ★ 多選投票 + Transaction 範例 ★
 * 一次投票請求可能勾選多個投票項目 (itemIds)。
 * 每個項目的投票都會異動 voting_record (新增) 與 voting_item (vote_count + 1) 兩張表，
 * 因此對多個項目逐一呼叫 sp_vote_cast 時，必須包在同一個 @Transactional 範圍內：
 * 只要其中任何一個項目寫入失敗 (例如項目不存在/已被刪除)，
 * 整批投票都必須回滾，避免「使用者明明選了 3 個項目，卻只成功 2 個」的資料不一致情況。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VotingServiceImpl implements VotingService {

    private final VotingRecordRepository votingRecordRepository;
    private final VotingItemRepository votingItemRepository;

    @Override
    @Transactional // 預設：任何 Exception 皆會觸發 rollback
    public List<VotingItemDTO> vote(VoteRequest request) {
        String voterName = XssSanitizer.sanitize(request.getVoterName());

        // 去除重複的項目編號，避免同一人對同一項目重複投票造成票數異常膨脹
        Set<Long> uniqueItemIds = new LinkedHashSet<>(request.getItemIds());

        if (uniqueItemIds.isEmpty()) {
            throw new BusinessException("請至少選擇一個投票項目");
        }

        for (Long itemId : uniqueItemIds) {
            try {
                votingRecordRepository.castVote(voterName, itemId);
            } catch (DataAccessException ex) {
                // sp_vote_cast 在項目不存在/已刪除時會 SIGNAL SQLSTATE '45000'
                // 攔截後轉換為業務例外，並讓 @Transactional 觸發整批回滾
                log.warn("投票失敗 itemId={}, voter={}, reason={}", itemId, voterName, ex.getMessage());
                throw new BusinessException("投票項目 (ID: " + itemId + ") 投票失敗，整批投票已取消");
            }
        }

        // 201 Created 的回應內容應呈現本次異動後的資源狀態，
        // 故查回剛剛被投票項目的最新累積票數一併回傳。
        List<VotingItemDTO> updatedItems = new ArrayList<>();
        for (Long itemId : uniqueItemIds) {
            votingItemRepository.findById(itemId).ifPresent(updatedItems::add);
        }
        return updatedItems;
    }

    @Override
    @Transactional
    public List<VotingRecordDTO> getRecords(Long itemId) {
        return votingRecordRepository.findAll(itemId);
    }
}
