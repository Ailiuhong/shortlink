# ShortLink

基于 Spring Boot 3 的轻量短链服务，核心链路为：

```text
创建：长链接 -> 参数校验 -> 频控 -> MySQL 自增 ID -> Base62 -> 唯一索引兜底
跳转：Caffeine -> Redis -> MySQL -> 302
统计：跳转成功后发布 Spring Event -> 线程池异步写访问日志
```

## 项目结构

```text
shortlink
├── pom.xml
├── sql/schema.sql
└── src
    ├── main
    │   ├── java/com/example/shortlink
    │   │   ├── ShortLinkApplication.java
    │   │   ├── cache
    │   │   │   ├── CachedLink.java
    │   │   │   ├── CacheLookup.java
    │   │   │   └── ShortLinkCache.java
    │   │   ├── common
    │   │   │   ├── ApiError.java
    │   │   │   ├── BusinessException.java
    │   │   │   └── GlobalExceptionHandler.java
    │   │   ├── config
    │   │   │   ├── AppProperties.java
    │   │   │   ├── AsyncConfig.java
    │   │   │   └── CacheConfig.java
    │   │   ├── controller/ShortLinkController.java
    │   │   ├── dto
    │   │   │   ├── AccessStatsResponse.java
    │   │   │   ├── CreateShortLinkRequest.java
    │   │   │   └── ShortLinkResponse.java
    │   │   ├── entity
    │   │   │   ├── AccessLog.java
    │   │   │   └── ShortLink.java
    │   │   ├── event
    │   │   │   ├── AccessLogListener.java
    │   │   │   └── LinkAccessEvent.java
    │   │   ├── mapper
    │   │   │   ├── AccessLogMapper.java
    │   │   │   └── ShortLinkMapper.java
    │   │   ├── security/RedisRateLimiter.java
    │   │   ├── service
    │   │   │   ├── AccessLogService.java
    │   │   │   └── ShortLinkService.java
    │   │   └── util
    │   │       ├── Base62Codec.java
    │   │       └── LongUrlValidator.java
    │   └── resources/application.yml
    └── test/java/com/example/shortlink/util/Base62CodecTest.java
```

## 启动

1. 创建数据库并执行 `sql/schema.sql`。
2. 修改 `application.yml` 或设置 `DB_USERNAME`、`DB_PASSWORD`、`REDIS_HOST` 等环境变量。
3. 启动 Redis 和项目：

```bash
mvn spring-boot:run
```

## 接口

创建短链：

```bash
curl -X POST http://localhost:8080/api/links \
  -H "Content-Type: application/json" \
  -d "{\"longUrl\":\"https://example.com/articles/1001\"}"
```

跳转（`302`）：

```text
GET http://localhost:8080/{shortCode}
```

访问统计：

```text
GET http://localhost:8080/api/links/{shortCode}/stats
```

## 设计说明

- `short_code` 使用 `ascii_bin` 排序规则，保证 Base62 的大小写敏感性。
- 缓存组件显式区分“未命中”和“已缓存空值”，空值在 Redis 中使用短 TTL，防止缓存穿透。
- Redis 异常时缓存层失败开放，跳转继续回源 MySQL，不影响核心读链路。
- 访问日志由 Spring Event 异步落库，适合单机项目；日志量增大后可替换为消息队列和预聚合表。
