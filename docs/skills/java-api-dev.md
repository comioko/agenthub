# Skill: Java 后端 API 开发

## 适用场景
需要新增或修改后端 REST API

## 输入
- API 路径
- 请求/响应格式
- 业务逻辑描述

## 输出
Controller + Service + Repository 完整实现

## 执行步骤

### 1. 定义 Controller

```java
@RestController
@RequestMapping("/api/{resource}")
@RequiredArgsConstructor
public class {Resource}Controller {

    private final {Resource}Service {resource}Service;

    @GetMapping
    public ResponseEntity<List<{Entity}>> getAll() {
        return ResponseEntity.ok({resource}Service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<{Entity}> getById(@PathVariable Long id) {
        return ResponseEntity.ok({resource}Service.getById(id));
    }

    @PostMapping
    public ResponseEntity<{Entity}> create(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid {Entity}Request request) {
        return ResponseEntity.ok({resource}Service.create(userId, request));
    }
}
```

### 2. 定义 Service

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class {Resource}Service {

    private final {Resource}Repository repository;

    public {Entity} create(Long userId, {Entity}Request request) {
        {Entity} entity = new {Entity}();
        // copy properties
        repository.insert(entity);
        return entity;
    }
}
```

### 3. 定义 Repository

```java
@Mapper
public interface {Resource}Repository extends BaseMapper<{Entity}> {
}
```

### 4. 数据模型变更

如需新增表或字段：

1. 更新 `schema.sql`
2. 创建迁移脚本 `V{version}__{description}.sql`
3. 同步更新前端 TypeScript 类型

### 5. 添加安全配置

如需新路径权限，修改 `SecurityConfig.java`：

```java
.requestMatchers("/api/new-path").permitAll()  // 或 .authenticated()
```

## 注意事项

1. **使用 `@AuthenticationPrincipal Long userId` 获取当前用户**
2. **所有异常用 try-catch 包装，返回统一错误格式**
3. **涉及事务的方法加 `@Transactional`**
4. **接口路径遵循 RESTful 规范**
5. **更新 `api-response-spec.md`** 如有变更
