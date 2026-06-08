# Skill: 前端问题排查

## 适用场景
页面不显示、接口报错、状态异常、白屏

## 排查步骤

### Step 1: 浏览器控制台（F12）

```
# 查看 Console 错误
# 过滤关键词：Error, Warning, mention

# 查找我们添加的调试日志
[MentionAutocomplete] Visible, loading agents...
[MentionAutocomplete] Received agents:
```

### Step 2: Network 请求检查

```
# 检查请求路径
# 确认无双 /api/api/ 重复前缀

# 检查请求头
Authorization: Bearer {token}

# 检查响应状态
200 - 成功
401 - 未授权（检查 token）
500 - 服务器错误（检查后端日志）
```

### Step 3: 组件状态检查

使用 React DevTools：

```
# 检查 useState 状态
- conversations
- currentConv
- messages
- showMention

# 检查 useEffect 是否正确触发
# 依赖数组是否完整
```

### Step 4: WebSocket 检查

```typescript
// 在 socket.ts 中添加日志
console.log('[WebSocket] connecting...')
console.log('[WebSocket] message:', data)

// 检查连接状态
ws.readyState === WebSocket.OPEN
```

## 常见问题与解决方案

### 问题 1: @ 列表不显示

**可能原因**：
1. `showMention` 状态未正确设置为 true
2. API 请求失败（检查 Network）
3. 组件被渲染但位置错误（在屏幕外）

**排查命令**：
```typescript
// 在 handleInputChange 中添加
console.log('[Chat] showMention:', showMention)
console.log('[Chat] position:', mentionPosition)
```

### 问题 2: API 请求 401

**可能原因**：
1. Token 未正确存储
2. Token 过期
3. 请求头未正确添加

**解决方案**：
```typescript
// 检查 axios 拦截器
const token = localStorage.getItem('token')
if (token) {
  config.headers.Authorization = `Bearer ${token}`
}
```

### 问题 3: 白屏

**可能原因**：
1. TypeScript 编译错误
2. 组件渲染报错
3. 循环依赖

**排查命令**：
```bash
# 重新构建
cd frontend && npm run build

# 检查编译错误
npx tsc --noEmit
```

## 常用调试技巧

### 临时添加日志

```typescript
// React 组件
useEffect(() => {
  console.log('[Component] state changed:', state)
}, [state])

// Event handler
const handleClick = () => {
  console.log('[handleClick] before', state)
  setState(newState)
  console.log('[handleClick] after', state)
}
```

### 使用 React DevTools

1. 安装 React DevTools 扩展
2. 打开开发者工具 → Components
3. 选中组件查看 props 和 state
4. 修改 state 测试渲染

## 快速检查清单

- [ ] 浏览器控制台无红色错误
- [ ] Network 中 /api/agents 请求成功（200）
- [ ] Token 正确存储在 localStorage
- [ ] WebSocket 连接成功
- [ ] 组件状态正确
