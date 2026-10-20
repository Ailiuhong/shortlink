package com.example.shortlink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateShortLinkRequest(
        @NotBlank(message = "长链接不能为空")
        @Size(max = 2048, message = "长链接不能超过 2048 个字符")
        String longUrl
) {
}
