package com.example.shortlink.util;

import com.example.shortlink.common.BusinessException;
import com.example.shortlink.config.AppProperties;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

@Component
public class LongUrlValidator {

    private static final int MAX_LENGTH = 2048;

    private final List<String> blockedHosts;

    public LongUrlValidator(AppProperties properties) {
        this.blockedHosts = properties.getUrl().getBlockedHosts().stream()
                .map(host -> host.toLowerCase(Locale.ROOT))
                .toList();
    }

    public String validate(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw BusinessException.badRequest("长链接不能为空");
        }

        String url = rawUrl.trim();
        if (url.length() > MAX_LENGTH) {
            throw BusinessException.badRequest("长链接不能超过 2048 个字符");
        }

        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException ex) {
            throw BusinessException.badRequest("长链接格式不正确");
        }

        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (scheme == null || host == null
                || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))
                || uri.getUserInfo() != null) {
            throw BusinessException.badRequest("仅支持合法的 HTTP/HTTPS 链接");
        }

        String normalizedHost = host.toLowerCase(Locale.ROOT);
        boolean blocked = blockedHosts.stream()
                .anyMatch(item -> normalizedHost.equals(item)
                        || normalizedHost.endsWith("." + item));
        if (blocked) {
            throw BusinessException.badRequest("该域名不允许生成短链");
        }
        return url;
    }
}
