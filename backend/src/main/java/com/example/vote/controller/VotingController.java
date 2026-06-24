package com.example.vote.controller;

import com.example.vote.common.ApiResponse;
import com.example.vote.dto.VoteRequest;
import com.example.vote.dto.VotingItemDTO;
import com.example.vote.dto.VotingRecordDTO;
import com.example.vote.service.VotingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 使用者投票 Controller (展示層)
 *
 * 使用者投票功能：
 *  - POST /api/votes              執行投票 (支援多選 itemIds)
 *  - GET  /api/votes/records      查詢投票紀錄 (可選 itemId 篩選)
 */
@Validated
@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
public class VotingController {

    private final VotingService votingService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<List<VotingItemDTO>> vote(@Valid @RequestBody VoteRequest request) {
        List<VotingItemDTO> updatedItems = votingService.vote(request);
        return ApiResponse.success("投票成功", updatedItems);
    }

    @GetMapping("/records")
    public ApiResponse<List<VotingRecordDTO>> getRecords(
            @RequestParam(required = false) @Positive(message = "項目編號必須為正整數") Long itemId) {
        return ApiResponse.success(votingService.getRecords(itemId));
    }
}
