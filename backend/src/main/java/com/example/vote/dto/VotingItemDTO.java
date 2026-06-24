package com.example.vote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 投票項目 DTO - 用於回傳項目資訊(含目前票數)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VotingItemDTO {

    private Long itemId;
    private String itemName;
    private Integer voteCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
