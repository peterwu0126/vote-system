package com.example.vote.service;

import com.example.vote.dto.VotingItemDTO;

import java.util.List;

/**
 * 投票項目業務邏輯介面 (業務層)
 */
public interface VotingItemService {

    List<VotingItemDTO> getAllItems();

    VotingItemDTO getItem(Long itemId);

    VotingItemDTO createItem(String itemName);

    VotingItemDTO updateItem(Long itemId, String itemName);

    void deleteItem(Long itemId);
}
