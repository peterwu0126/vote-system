package com.example.vote.repository;

import com.example.vote.dto.VotingItemDTO;

import java.util.List;
import java.util.Optional;

/**
 * 投票項目 Repository (資料層)
 * 所有方法皆透過 Stored Procedure 存取資料庫。
 */
public interface VotingItemRepository {

    List<VotingItemDTO> findAll();

    Optional<VotingItemDTO> findById(Long itemId);

    Long create(String itemName);

    int update(Long itemId, String itemName);

    int softDelete(Long itemId);
}
