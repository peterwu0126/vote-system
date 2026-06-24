package com.example.vote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 投票紀錄 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VotingRecordDTO {

    private Long recordId;
    private String voterName;
    private Long itemId;
    private String itemName;
    private LocalDateTime votedAt;
}
