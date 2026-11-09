# 开源原神 mod 深度分析与可复用实现方案

> 调研日期：2026-11-09
> 分析对象：SevenElements（元素战斗系统）、Genshin-Nature 社区续作（内容向）

---

## 一、SevenElements —— 元素反应系统的工程标杆

仓库：https://github.com/xrickastley/SevenElements （Fabric 1.21/1.21.1，分支 `1.21-1.21.1`）
目标：忠实复刻原神的元素战斗系统，包括**元素量理论（Elemental Gauge Theory）**和**附着冷却（Internal Cooldown, ICD）**。

### 技术栈

| 依赖 | 用途 |
|---|---|
| Fabric API 0.102.0+1.21 | 基础 API |
| Cardinal Components API (CCA) 6.1.2 | 实体级元素附着数据存储与同步 |
| Cloth Config + ModMenu | 配置界面 |
| 自定义 Registry | 元素反应注册表（`SevenElementsRegistries.ELEMENTAL_REACTION`） |

### 核心架构（`element/` 包）

**1. Element 枚举 + ElementSettings 建造者**

每个元素（火/水/风/雷/草/冰/岩，外加冻结、燃烧、激化等"复合元素"和仅作属性标识的物理）用流式 `ElementSettings` 配置：

- `setPriority()` —— 元素优先级，控制附着/反应/渲染顺序
- `setDecayRate(Function)` —— 每 tick 衰减的元素量（GU），支持条件函数（如草元素在燃烧共存时衰减翻倍：`max(0.4, 自然衰减×2)`，已换算成 GU/tick）
- `canBeAura()` —— 能否作为"附着元素"（风/岩不能）
- `hasAuraTax()` —— 附着税（Aura Tax）
- `bypassesCooldown()` —— 复合元素绕过 ICD
- `linkToElement()` —— 量值联动（冻结→冰、燃烧→火、激化→草，支持条件谓词 `linkGaugeDecayIf`）
- `particleRenderer` —— 附着期间实体的粒子特效

**2. ElementalReaction 抽象类 + Settings 建造者**

每个反应声明：

- `auraElement` / `triggeringElement`（附着元素/触发元素）+ 各自优先级
- `reversable` —— 是否可逆（可逆时双方都可作附着元素）
- `reactionCoefficient` —— 量值消耗系数
- `reactionMultiplier` —— 反应强度倍率
- `Type` 枚举 —— 反应类型决定**元素精通加成公式**：`scale * EM / (EM + halfMax)`
  - TRANSFORMATIVE(剧变): 16 / 2000
  - AMPLIFYING(增幅): 2.78 / 1400
  - ADDITIVE(附加): 5 / 1200
  - SHIELD(护盾): 4.44 / 1400
- 触发链控制：`endsReactionTrigger`、`preventsReactionsAfter(...)`、`preventsPriorityUpgrade`、`applyResultAsAura`（剩余量转回附着）

**3. 触发流程**（`ElementalReaction.trigger()`）

```
isTriggerable() 检查双元素是否共存且非空
  → 若触发元素当前是附着方则交换
  → reducedGauge = 附着元素.reduceGauge(系数 × 触发元素当前量)
  → 触发元素.reduceGauge(reducedGauge)
  → onReaction() 抽象钩子（各反应子类实现伤害/效果）
  → S2C 数据包显示反应文字（ShowElementalReactionS2CPayload）
  → 触发进度判据 + ReactionTriggered 事件 + 音效
```

**4. 反应覆盖度**（`element/reaction/`，40+ 个类）

蒸发、融化（火/冰双向）、超载、超导、感电、扩散（4 元素）、结晶（4 元素+冻）、燃烧、绽放/超绽放/烈绽放、原激化/蔓激化/超激化、冻结、碎冰——基本覆盖原神全反应表，含草元素三相体系。

### 可复用要点

- 用 CCA 组件存实体元素附着（`ElementComponent`），天然解决同步与持久化
- 元素用枚举+建造者，反应用自定义 Registry——便于其他 mod 扩展注册新反应
- 事件总线（`ReactionTriggered.EVENT`）让外部 mod 监听反应
- 游戏规则（GameRule）控制等级倍率，方便整合包调整

### ⚠️ 许可证陷阱

README 明确写了 **"Copyright © 2025 xrickastley. All Rights Reserved."**——这是**源码可见（source-available）但不是开源**，不能直接复制代码进自己的项目。可以学习架构、自己重写实现，或先联系作者取得授权。

---

## 二、Genshin-Nature（Primogem-Studio 社区续作）—— 内容向参考

仓库：https://github.com/Primogem-Studio/Genshin-Nature （原 Modrinth Genshin Nature 的非官方续作）

### 项目形态

- **MCreator 工程**：核心是 `genshin_nature.mcreator`（3.3MB 工作区文件）+ `elements/*.mod.json`（每个游戏元素一个 JSON 定义）+ `models/`（实体模型）
- 内容覆盖：深渊法师（火/水/雷/冰）、生物与植物、圣遗物、武器升阶、神之眼（Vision）系统（`Addcryovision`/`Addpyrovision` 等元素获取逻辑）、相遇之缘（Acquaintfate）等物品
- 许可证：**MIT** —— 可以自由使用、修改、再分发

### 可复用要点

- 若你的 mod 偏"内容量大、逻辑轻"，MCreator 生成 + 手写关键逻辑是快速路线；`elements/*.mod.json` 可读性尚可，实体生成条件（`*spawning.mod.json`）、受伤事件（`*EntityIsHurt.mod.json`）等都是数据驱动的
- 缺点是 MCreator 工程重构成本高、版本升级痛苦，深度玩法（如元素反应）不适合用它做

---

## 三、对自己 mod 的实现方案建议

按目标分两条路线：

### 路线 A：深度玩法（推荐）—— 以 SevenElements 架构为蓝本自行实现

1. **数据层**：CCA（Fabric）或 Attachment API（NeoForge）存实体元素附着；量值（GU）、衰减、ICD 全部走组件 tick
2. **元素定义**：枚举 + 建造者（照抄设计模式而非代码）：优先级、衰减率、能否作附着、量值联动
3. **反应框架**：抽象 `ElementalReaction` + 自定义 Registry + 触发优先级队列；反应类型决定精通加成公式
4. **表现层**：S2C payload 弹反应文字，粒子附着渲染，伤害数字染色
5. **扩展性**：公开事件 + 注册表，让附属 mod 能加新元素/反应

### 路线 B：内容向快速产出

- 用 MCreator 打底（生物、物品、结构），核心玩法逻辑手写 Mixin/事件
- 参考 Genshin-Nature 的 elements JSON 组织方式做数据驱动

### 版权红线

- 不直接提取原神游戏资源（模型/贴图/音频）；SevenElements 的做法是找人重制模型（README 致谢里有专门的 Entity Models 作者）
- 项目里加上 "Not affiliated with HoYoverse" 免责声明
- 不要直接复制 SevenElements 代码（All Rights Reserved）；Genshin-Nature 续作是 MIT，可以放心参考
