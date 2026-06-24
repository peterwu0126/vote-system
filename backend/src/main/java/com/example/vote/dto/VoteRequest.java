package com.example.vote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 投票請求 DTO - 支援多選 (itemIds 為一個以上的項目編號)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteRequest {

    @NotBlank(message = "投票人姓名不可為空")
    @Size(max = 50, message = "投票人姓名長度不可超過 50 字元")
    // 限制僅允許中英文、數字與空白，從輸入層面降低 XSS / 特殊字元注入風險
    @Pattern(regexp = "^[\\p{L}0-9 _-]{1,50}$", message = "投票人姓名包含不允許的字元")
    private String voterName;

    @NotEmpty(message = "請至少選擇一個投票項目")
    private List<Long> itemIds;
}
