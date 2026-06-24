package com.example.vote.controller;

import com.example.vote.common.ApiResponse;
import com.example.vote.dto.ItemCreateRequest;
import com.example.vote.dto.ItemUpdateRequest;
import com.example.vote.dto.VotingItemDTO;
import com.example.vote.service.VotingItemService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 投票項目管理 Controller (展示層)
 *
 * 後台編輯投票項目功能：
 *  - GET    /api/items       查詢所有投票項目
 *  - GET    /api/items/{id}  查詢單一投票項目
 *  - POST   /api/items       新增投票項目
 *  - PATCH  /api/items/{id}  部分更新投票項目 (目前僅 itemName 可寫)
 *  - DELETE /api/items/{id}  刪除投票項目(軟刪除)
 *
 * 註：使用 PATCH 而非 PUT，因為請求僅攜帶欲修改的欄位 (itemName)，
 * 並非該資源的完整表述；PUT 語意上應為整個資源的覆蓋取代 (idempotent replace)。
 */
@Validated
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class VotingItemController {

    private final VotingItemService votingItemService;

    @GetMapping
    public ApiResponse<List<VotingItemDTO>> getAllItems() {
        return ApiResponse.success(votingItemService.getAllItems());
    }

    @GetMapping("/{itemId}")
    public ApiResponse<VotingItemDTO> getItem(
            @PathVariable @Positive(message = "項目編號必須為正整數") Long itemId) {
        return ApiResponse.success(votingItemService.getItem(itemId));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<VotingItemDTO> createItem(@Valid @RequestBody ItemCreateRequest request) {
        VotingItemDTO created = votingItemService.createItem(request.getItemName());
        return ApiResponse.success("投票項目新增成功", created);
    }

    @PatchMapping("/{itemId}")
    public ApiResponse<VotingItemDTO> updateItem(
            @PathVariable @Positive(message = "項目編號必須為正整數") Long itemId,
            @Valid @RequestBody ItemUpdateRequest request) {
        VotingItemDTO updated = votingItemService.updateItem(itemId, request.getItemName());
        return ApiResponse.success("投票項目更新成功", updated);
    }

    @DeleteMapping("/{itemId}")
    public ApiResponse<Void> deleteItem(
            @PathVariable @Positive(message = "項目編號必須為正整數") Long itemId) {
        votingItemService.deleteItem(itemId);
        return ApiResponse.success("投票項目刪除成功", null);
    }
}
