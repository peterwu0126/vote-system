package com.example.vote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 新增投票項目 - 請求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemCreateRequest {

    @NotBlank(message = "投票項目名稱不可為空")
    @Size(max = 100, message = "投票項目名稱長度不可超過 100 字元")
    private String itemName;
}
