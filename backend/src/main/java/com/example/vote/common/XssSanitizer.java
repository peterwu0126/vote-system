package com.example.vote.common;

import org.owasp.encoder.Encode;

/**
 * XSS 防護工具 (共用層)
 * 對使用者輸入的文字進行 HTML escape，防止儲存型/反射型 XSS 攻擊。
 * 搭配 DTO 上的 Bean Validation (@Pattern/@Size) 雙重防護。
 */
public final class XssSanitizer {

    private XssSanitizer() {
    }

    /**
     * 將字串進行 HTML escape，使 <, >, ", ' 等特殊字元失去 HTML/JS 語義。
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return Encode.forHtml(input.trim());
    }
}
