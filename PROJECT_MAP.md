# Exhibit Flow PROJECT_MAP

> 三端项目速查地图。找文件查「模块文件地图」，改需求查「改动影响链」，调 Bug 查「症状定位表」，理业务查「场景剧本」。

---

## 一、项目总览

| 端 | 路径 | 技术栈 | 端口 |
|----|------|--------|------|
| **后端** | `devolop-master` | Spring Boot 3.5 + MyBatis-Plus + Spring Security + Redis + MySQL | `9999` |
| **管理后台** | `exhibit-flow-vue` | Vue 3 + TypeScript + Element Plus + Pinia + Vite | `5173` |
| **移动端 H5** | `exhibit-flow-h5` | Vue 3 + TypeScript + Vant + Pinia + Vite | `3100` |

**后端主模块**：`pig-boot`（业务） + `pig-upms`（系统基础模块）  
**业务包前缀**：`com.pig4cloud.pig.eh`  
**接口前缀**：`/admin`（应用上下文） + `/eh/*`（展厅业务）  
**认证方式**：OAuth2 Bearer Token（前后端请求头 `Authorization: Bearer <token>`）

---

## 二、业务术语词典

| 业务说法 | 代码实体 | 数据库表 | 关键字段 |
|---------|---------|---------|---------|
| 展厅申请 | `EhApply` | `eh_apply` | `subject`, `startTime`, `endTime`, `visitorCompany`, `status` |
| 申请来访客户 | `EhApplyVisitor` | `eh_apply_visitor` | `applyId`, `visitorCompany`, `industry`（医疗/教育/交通/水利/党政/执法/互联网/工业/农商/金融/其他）, `visitorCount` |
| 审批节点 | `EhApprovalNode` | `eh_approval_node` | `applyId`, `nodeLevel`, `approver`, `action`, `status` |
| 参观留存 | `EhVisitRecord` | `eh_visit_record` | `applyId`, `actualVisitTime`, `actualVisitCount`, `opportunityCode`, `status` |
| 现场照片 | `EhVisitPhoto` | `eh_visit_photo` | `visitId`, `fileUrl`, `fileName`, `fileSize` |
| 商机跟进 | `EhOpportunity` | `eh_opportunity` | `visitId`, `applyId`, `opportunityCode`, `status` |
| 改期取消流水 | `EhChangeLog` | `eh_change_log` | `applyId`, `changeType`, `oldStartTime`, `newStartTime`, `reason` |

---

## 三、模块文件地图

### 后端（`devolop-master/pig-boot/src/main/java/com/pig4cloud/pig/eh`）

| 模块 | Controller | Service | Mapper | 表 |
|------|-----------|---------|--------|-----|
| 申请 | `controller/EhApplyController` | `service/EhApplyService` | `mapper/EhApplyMapper` | `eh_apply` |
| 申请来访客户 | `controller/EhApplyVisitorController` | `service/EhApplyVisitorService` | `mapper/EhApplyVisitorMapper` | `eh_apply_visitor` |
| 审批节点 | `controller/EhApprovalNodeController` | `service/EhApprovalNodeService` | `mapper/EhApprovalNodeMapper` | `eh_approval_node` |
| 参观留存 | `controller/EhVisitRecordController` | `service/EhVisitRecordService` | `mapper/EhVisitRecordMapper` | `eh_visit_record` |
| 现场照片 | `controller/EhVisitPhotoController` | `service/EhVisitPhotoService` | `mapper/EhVisitPhotoMapper` | `eh_visit_photo` |
| 商机跟进 | `controller/EhOpportunityController` | `service/EhOpportunityService` | `mapper/EhOpportunityMapper` | `eh_opportunity` |
| 改期取消流水 | `controller/EhChangeLogController` | `service/EhChangeLogService` | `mapper/EhChangeLogMapper` | `eh_change_log` |

对应 XML：`devolop-master/pig-boot/src/main/resources/mapper/eh/*Mapper.xml`

### 管理后台（`exhibit-flow-vue/src`）

| 功能 | Views | API 文件 |
|------|-------|---------|
| 申请管理 | `views/eh/apply/index.vue` | `api/eh/apply.ts` |
| 审批节点管理 | `views/eh/approval/index.vue` | `api/eh/approval.ts` |
| 参观留存管理 | `views/eh/visit/index.vue` | `api/eh/visit.ts` |
| 现场照片管理 | `views/eh/visit-photo/index.vue` | `api/eh/visit-photo.ts` |
| 商机跟进 | `views/eh/opportunity/index.vue` | `api/eh/opportunity.ts` |
| 改期取消流水 | `views/eh/changelog/index.vue` | `api/eh/changelog.ts` |
| 统计看板/报表 | `views/eh/admin/dashboard.vue`, `views/eh/report/*.vue` | `api/eh/admin-stats.ts`, `api/eh/calendar.ts` |
| 系统管理（Pig 基础） | `views/admin/**` | `api/admin/**` |

### 移动端 H5（`exhibit-flow-h5/src`）

| 功能 | Views | API 文件 |
|------|-------|---------|
| 登录 | `views/Login.vue` | `api/request.ts`（统一请求） |
| 首页 | `views/applicant/Home.vue` | `api/eh/apply.ts` |
| 我的申请 | `views/applicant/MyApps.vue` | `api/eh/apply.ts` |
| 新建申请 | `views/applicant/NewApp.vue` | `api/eh/apply.ts` |
| 申请详情 | `views/applicant/AppDetail.vue` | `api/eh/apply.ts` |
| 通知中心 | `views/applicant/Notifications.vue` | `api/eh/notice.ts` |

---

## 四、场景剧本（完整业务流）

### 场景A：H5 用户发起展厅申请

```text
[H5] views/applicant/NewApp.vue
  ↓ 提交申请 -> api/eh/apply.ts::submitApplication()
  ↓ HTTP POST /admin/eh/apply（USE_MOCK=false 时）
  ↓ [后端] EhApplyController.save()
      └─ EhApplyServiceImpl.save() + EhApplyMapper
  ↓ 写入表 eh_apply
  ↓ 返回申请单信息，H5 跳转申请详情页
```

### 场景B：管理端处理审批与排期

```text
[管理后台] views/eh/approval/index.vue
  ↓ api/eh/approval.ts
  ↓ GET/PUT /admin/eh/approval/*
  ↓ EhApprovalNodeController + EhApprovalNodeServiceImpl
  ↓ 表 eh_approval_node 节点状态更新

[管理后台] views/eh/apply/index.vue + api/eh/calendar.ts
  ↓ GET /admin/eh/apply/calendar（菜单已在初始化 SQL 中配置）
  ↓ 用于排期可视化
```

### 场景C：参观结束后留存与商机跟进

```text
[管理后台] views/eh/visit/index.vue
  ↓ api/eh/visit.ts
  ↓ EhVisitRecordController
  ↓ 表 eh_visit_record

[管理后台] views/eh/opportunity/index.vue
  ↓ api/eh/opportunity.ts
  ↓ EhOpportunityController
  ↓ 表 eh_opportunity
```

### 场景D：改期/取消留痕追溯

```text
[管理后台] views/eh/changelog/index.vue
  ↓ api/eh/changelog.ts
  ↓ EhChangeLogController
  ↓ 表 eh_change_log 保存变更前后时段与原因
```

---

## 五、API 快速索引

### H5 常用接口（`exhibit-flow-h5/src/api/eh`）

| 功能 | 方法 | 路径 |
|------|------|------|
| 我的申请列表 | GET | `/eh/apply/page` |
| 申请详情 | GET | `/eh/apply/details/{id}` |
| 新建申请 | POST | `/eh/apply` |
| 取消申请 | PUT | `/eh/apply/{id}/cancel` |
| 改期申请 | PUT | `/eh/apply/{id}/reschedule` |
| 时间冲突检测 | GET | `/eh/apply/conflict` |
| 通知列表 | GET | `/eh/notification/page` |
| 单条通知已读 | PUT | `/eh/notification/{id}/read` |
| 全部已读 | PUT | `/eh/notification/read-all` |

> 说明：H5 当前 `api/eh/apply.ts` 与 `api/eh/notice.ts` 内默认 `USE_MOCK=true`，线上联调前需关闭。

### 管理端常用接口（`exhibit-flow-vue/src/api/eh`）

| 功能 | 方法 | 路径 |
|------|------|------|
| 申请分页/详情/增改删 | GET/POST/PUT/DELETE | `/admin/eh/apply/*` |
| 审批节点分页/详情/增改删 | GET/POST/PUT/DELETE | `/admin/eh/approval/*` |
| 参观留存分页/详情/增改删 | GET/POST/PUT/DELETE | `/admin/eh/visit/*` |
| 现场照片分页/详情/增改删 | GET/POST/PUT/DELETE | `/admin/eh/visit/photo/*` |
| 商机跟进分页/详情/增改删 | GET/POST/PUT/DELETE | `/admin/eh/opportunity/*` |
| 改期取消流水分页/详情/增改删 | GET/POST/PUT/DELETE | `/admin/eh/changelog/*` |
| 日历排期 | GET | `/admin/eh/apply/calendar` |
| 统计看板 | GET | `/admin/eh/stats/kpi`, `/monthly`, `/city`, `/industry`, `/strategic`, `/funnel` |

---

## 六、改动影响链

### 改 `eh_apply` 表字段（如 `status`, `start_time`）→ 同步改：

```text
后端：
  entity/EhApply.java
  mapper/EhApplyMapper.java + resources/mapper/eh/EhApplyMapper.xml
  controller/EhApplyController.java
前端：
  exhibit-flow-vue/src/api/eh/apply.ts
  exhibit-flow-vue/src/views/eh/apply/index.vue
  exhibit-flow-h5/src/api/eh/apply.ts
  exhibit-flow-h5/src/views/applicant/NewApp.vue
  exhibit-flow-h5/src/views/applicant/AppDetail.vue
```

### 改审批流逻辑（节点定义/状态）→ 同步改：

```text
eh_approval_node + EhApprovalNode.java
EhApprovalNodeController / EhApprovalNodeServiceImpl
exhibit-flow-vue/src/api/eh/approval.ts
exhibit-flow-vue/src/views/eh/approval/index.vue
exhibit-flow-h5/src/components/eh/ApprovalTimeline.vue
```

### 改留存与商机关联（`visitId`, `opportunityCode`）→ 同步改：

```text
EhVisitRecord.java / EhOpportunity.java
EhVisitRecordController / EhOpportunityController
exhibit-flow-vue/src/api/eh/visit.ts
exhibit-flow-vue/src/api/eh/opportunity.ts
views/eh/visit/index.vue + views/eh/opportunity/index.vue
```

---

## 七、症状定位表

| 症状 | 第一现场 | 排查重点 |
|------|---------|---------|
| H5 列表一直是演示数据 | `exhibit-flow-h5/src/api/eh/apply.ts` | `USE_MOCK` 是否仍为 `true` |
| 请求直接跳登录页 | `exhibit-flow-h5/src/api/request.ts` | 是否返回 `401`；Token 是否过期/丢失 |
| 管理端接口返回 code=1 | `exhibit-flow-vue/src/utils/request.ts` | 后端 `R.failed` / 业务校验失败信息 |
| 菜单能看到但按钮无权限 | `Eh*Controller` 的 `@HasPermission` + 前端 `v-auth` | 角色权限标识是否已分配 |
| 访问 `/admin/eh/*` 404 | `devolop-master/pig-boot/src/main/resources/application.yml` | `server.servlet.context-path=/admin` 与前端代理是否一致 |
| 日历菜单有入口但接口不可用 | `db/init/V20260423__eh_admin_menu.sql` + 后端 Controller | 菜单 SQL 已导入但后端对应接口是否实现 |

---

## 八、权限标识速查

| 模块 | 权限标识 |
|------|---------|
| 展厅申请 | `eh_apply_view/add/edit/del` |
| 申请来访客户 | `eh_apply_visitor_view/add/edit/del` |
| 审批节点 | `eh_approval_node_view/add/edit/del` |
| 参观留存 | `eh_visit_record_view/add/edit/del` |
| 现场照片 | `eh_visit_photo_view/add/edit/del` |
| 商机跟进 | `eh_opportunity_view/add/edit/del` |
| 改期取消流水 | `eh_change_log_view/add/edit/del` |

---

## 九、特殊机制备忘

```text
请求/鉴权机制：
  管理端 request.ts:
    - 自动带 Bearer Token
    - 支持报文加密 ENC_FLAG
    - URL 适配（单体/微服务）
  H5 request.ts:
    - baseURL 默认 /admin
    - 401 自动跳 /login

环境与代理：
  后端：9999 + context-path=/admin
  管理端：.env.development -> VITE_PORT=5173, 代理到 http://localhost:9999
  H5：vite.config.ts -> port=3100, 代理 /admin 到 http://localhost:9999

初始化依赖：
  先导入 pig 基础 SQL（sys_user/sys_role/sys_menu/sys_dict 等），
  再导入/创建 eh_* 业务表与菜单权限数据。
```
