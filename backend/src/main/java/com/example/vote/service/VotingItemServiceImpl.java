package com.example.vote.service;

import com.example.vote.common.BusinessException;
import com.example.vote.common.XssSanitizer;
import com.example.vote.dto.VotingItemDTO;
import com.example.vote.repository.VotingItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 投票項目業務邏輯實作 (業務層)
 */
@Service
@RequiredArgsConstructor
public class VotingItemServiceImpl implements VotingItemService {

    private final VotingItemRepository votingItemRepository;

    @Override
    @Transactional
    public List<VotingItemDTO> getAllItems() {
        return votingItemRepository.findAll();
    }

    @Override
    @Transactional
    public VotingItemDTO getItem(Long itemId) {
        return votingItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException("找不到指定的投票項目 (ID: " + itemId + ")"));
    }

    @Override
    @Transactional
    public VotingItemDTO createItem(String itemName) {
        // 二次清洗，防止 XSS 字元落入資料庫（DTO 上的 @Pattern/@Size 為第一道防線）
        String safeName = XssSanitizer.sanitize(itemName);

        Long newId = votingItemRepository.create(safeName);
        return votingItemRepository.findById(newId)
                .orElseThrow(() -> new BusinessException("投票項目建立失敗"));
    }

    @Override
    @Transactional
    public VotingItemDTO updateItem(Long itemId, String itemName) {
        String safeName = XssSanitizer.sanitize(itemName);

        // 先確認項目存在，避免對不存在的資料進行更新後卻得到誤導性的成功回應
        votingItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException("找不到指定的投票項目 (ID: " + itemId + ")"));

        int affected = votingItemRepository.update(itemId, safeName);
        if (affected == 0) {
            throw new BusinessException("更新投票項目失敗");
        }
        return votingItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException("更新後查無投票項目"));
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId) {
        votingItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException("找不到指定的投票項目 (ID: " + itemId + ")"));

        int affected = votingItemRepository.softDelete(itemId);
        if (affected == 0) {
            throw new BusinessException("刪除投票項目失敗");
        }
    }
}
