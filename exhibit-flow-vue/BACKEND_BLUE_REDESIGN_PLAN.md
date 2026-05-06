# 政企蓝办公风整改总规划（后台管理端）

## 1. 目标与原则
- 目标风格：浅蓝政企办公风（高可读、低饱和、稳重）。
- 统一策略：先改“全局主题令牌 + 布局层”，再逐页完成“组件细节 + 场景色”。
- 兼容要求：不改业务逻辑，只做视觉层整改；保留现有交互路径。

## 2. 页面全量清单（不遗漏）

### 2.1 业务主页面（EH）
1. 首页日历总览：`src/views/eh/admin/calendar.vue`
2. 首页数据看板：`src/views/eh/admin/dashboard.vue`
3. 申请管理列表：`src/views/eh/apply/index.vue`
4. 申请管理表单：`src/views/eh/apply/form.vue`
5. 申请管理日历：`src/views/eh/apply/calendar.vue`
6. 审批节点列表：`src/views/eh/approval/index.vue`
7. 审批节点表单：`src/views/eh/approval/form.vue`
8. 改期取消日志列表：`src/views/eh/changelog/index.vue`
9. 改期取消日志表单：`src/views/eh/changelog/form.vue`
10. 参观留存列表：`src/views/eh/visit/index.vue`
11. 参观留存表单：`src/views/eh/visit/form.vue`
12. 参观留存照片页：`src/views/eh/visit/photo.vue`
13. 现场照片管理列表：`src/views/eh/visit-photo/index.vue`
14. 现场照片管理表单：`src/views/eh/visit-photo/form.vue`
15. 权限管理客户列表：`src/views/eh/base/customer.vue`
16. 权限管理字典页：`src/views/eh/base/dict.vue`
17. 机会管理列表：`src/views/eh/opportunity/index.vue`
18. 机会管理表单：`src/views/eh/opportunity/form.vue`
19. 报表总览：`src/views/eh/report/dashboard.vue`
20. 报表覆盖率：`src/views/eh/report/coverage.vue`
21. 报表分布：`src/views/eh/report/distribution.vue`
22. 报表漏斗：`src/views/eh/report/funnel.vue`

### 2.2 布局与框架页面
23. 顶部导航：`src/layout/navBars/index.vue`
24. 标签页：`src/layout/navBars/tagsView/tagsView.vue`
25. 侧栏导航：`src/layout/navMenu/vertical.vue`
26. Logo区：`src/layout/logo/index.vue`
27. 主布局容器：`src/layout/main/defaults.vue`
28. 登录页：`src/views/login/index.vue`（若项目存在同名页，纳入统一）

## 3. 分阶段整改任务

### 阶段A（已开始）全局主题落蓝
- 改全局主题色变量：背景、边框、文字层级、主色、主色悬浮、主色浅底。
- 改 EH 组件令牌：按钮、输入框、状态色、侧栏基色。
- 改默认主题配置：primary / menu / tags 的默认值。

### 阶段B 布局层统一
- 统一左侧菜单激活态（浅蓝底 + 深蓝字）。
- 统一顶部标签激活态（去重黑底，换蓝色强调）。
- 统一面包屑与标题区层级，减少视觉噪声。

### 阶段C 逐页组件补齐
- 列表页：筛选 chip、状态 badge、主按钮、选中卡片边框。
- 表单页：输入焦点、日期时间控件、步骤条当前态。
- 看板报表：图表主色组、导出弹窗表头、统计卡强调色。
- 图片页：预览弹窗、下载按钮、遮罩与工具条风格。

### 阶段D 收口验收
- 页面逐个走查（清单 1~28）。
- 明暗对比与可读性检查。
- 打包构建与线上发布验证。
