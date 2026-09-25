# AGENTS.md

本文件是本仓库的协作约定，适用于在本项目中工作的所有人（以及所有 AI 编码助手）。
凡是与本文件冲突的习惯性做法，以本文件为准。

## 一、语言约定（最高优先级）

### 1.1 自然语言输出一律使用简体中文

**包括模型的思考过程。** 模型在推理时只要用自然语言表达想法——分析问题、权衡方案、拆解步骤、
自我检查、判断风险——**都必须用简体中文书写**，不得输出英文的推理段落、英文的分析说明或
中英夹杂的思考句。英文是本领域模型的默认表达语言，本仓库明确要求把它转换并输出为中文。

必须使用中文的范围：

| 场景 | 要求 |
| --- | --- |
| 对用户的回答、解释、总结、报错说明 | 简体中文 |
| 模型内部推理 / 思考（reasoning、thinking 中的自然语言部分） | 简体中文 |
| 代码注释与 Javadoc | 简体中文 |
| 异常消息、日志文本、`main` 中的打印输出 | 简体中文 |
| Git 提交信息、分支说明、TODO 备注 | 简体中文 |

允许保留英文的场合（仅限这些）：

- 代码本身：关键字、类名、方法名、变量名、包名、`import`；
- JDK 与第三方 API 名称，例如 `LocalDateTime`、`DateTimeFormatter`、`Arrays.sort`；
- 不翻译或不宜翻译的专有名词与缩写，例如 JVM、GC、UTF-8、O(n log n)、`int`、`null`、Lambda；
- 已有的文件名、路径、命令行参数与命令本身。

写作要求：中文要写得自然，不要用英文句式直译出「翻译腔」；除上述专有名词外，
不要在同一句话里中英混杂。

### 1.2 排版的细节

- 中文与英文或数字之间加一个半角空格，例如「使用 `LocalDate` 表示日期」；
- 正文中出现的代码、类型名、方法名一律用反引号包裹；
- 代码块与命令行之内不适用中文标点，其他地方使用中文全角标点。

## 二、项目概览

这是一个**面向学习与演示**的 Java 示例工程，重点不是功能多少，而是把每个知识点讲清楚：
代码里保留了大量中文 Javadoc，用来解释「为什么这样设计」。

- 构建方式：**IntelliJ IDEA 原生模块**（`Demo.iml`），没有 Maven / Gradle，也没有任何第三方依赖；
- JDK 版本：**Java 8**（`Project SDK = 1.8`，`languageLevel = JDK_1_8`）；
- 源码编码：**UTF-8（无 BOM）**，中文注释是项目的一部分，务必不要破坏编码；
- 编译输出：`out/`，已加入 `.gitignore`，属于生成物，不要手工编辑，也不要提交。

目录结构：

```text
src/
  Main.java                                程序入口：欢迎语 + 计数输出
  com/zccheng/demo/
    B.java                                 空类骨架（练习用，内容可自由替换）
    PartDemo.java                          装箱 / 分组逻辑演示
    DateUtils.java                         日期时间工具类（基于 java.time）
    algorithm/                             排序算法合集
      SortAlgorithm.java                   排序统一接口（函数式接口 + 默认方法）
      AbstractSortAlgorithm.java           抽象父类：参数校验、元素交换、有序性判断
      SortTestSupport.java                 测试支持：用例集合、随机压测、断言、数据生成
      SortDemo.java                        总览入口：正确性验证 + 性能对比
      BubbleSort.java ... RadixSort.java   各算法实现，每个类都自带 main 自测
out/                                       编译产物（已忽略，勿改勿提交）
```

## 三、构建与运行

因为项目没有构建工具，一律直接用 JDK 命令行工具操作。

```powershell
# 编译全部源码（-encoding UTF-8 必须带上，否则中文注释会报「编码 GBK 的不可映射字符」）
javac -encoding UTF-8 -d out\production\Demo (Get-ChildItem -Recurse -Path src -Filter *.java | ForEach-Object { $_.FullName })

# 运行入口类
java -cp out\production\Demo Main

# 运行某个类的自测 main，例如排序算法
java -cp out\production\Demo com.zccheng.demo.algorithm.BubbleSort

# 运行排序算法总览：正确性验证 + 随机数据 / 近乎有序数据的性能对比
java -cp out\production\Demo com.zccheng.demo.algorithm.SortDemo
```

注意事项：

- **`-encoding UTF-8` 不是可选项。** 本项目源码含大量中文，在中文 Windows 上用默认平台编码
  编译会触发「编码 GBK 的不可映射字符」，注释乱码后 `javap` 与 IDE 提示都会出错；
- 在 PowerShell 中给 `java` 传 `-D` 开头的系统属性时要加引号，例如
  `java "-Dfile.encoding=UTF-8" -cp out\production\Demo Main`，否则参数会被 Shell 拆坏；
- 程序打印的是中文，一旦输出被**管道、重定向或日志文件**接管，就可能出现乱码：
  此时用 `java "-Dfile.encoding=UTF-8" ...` 启动，并保证读取端也按 UTF-8 解码
  （在 PowerShell 里可先执行 `[Console]::OutputEncoding = [System.Text.Encoding]::UTF8`）；
- 编译产物统一写到 `out\production\Demo`，临时试验可以写到系统临时目录，**不要污染 `src/`**。

## 四、代码风格

本仓库的注释密度明显高于一般业务项目，这是有意为之，新增代码请保持同样的讲解风格。

- 缩进 4 个空格，不使用 Tab；保持文件原有的换行符风格；
- 每个类、每个 public 方法都要写**中文 Javadoc**：写清楚作用、参数（`@param`）、
  异常（`@throws`）、返回值，以及**为什么这样设计**（例如为什么用默认方法、为什么用 `LocalTime.MAX`）；
- 类的 Javadoc 建议用列表说明「设计取舍」与「使用示例」，参考 `SortAlgorithm`、`SortDemo`、`DateUtils`；
- 工具类写成 `final` + 私有构造 + 全静态方法，参考 `DateUtils`、`SortTestSupport`：

  ```java
  public final class XxxUtils {

      private XxxUtils() {
      }
  }
  ```

- 长方法要拆成职责单一的小方法，并用 `// ------------------ 分组标题 ------------------`
  这类分隔线把「对外方法 / 内部实现」分开，参考 `PartDemo`、`DateUtils`；
- 常量用 `private static final` 全大写命名；只有确实要对外暴露的（例如格式串）才用 `public static final`；
- 参数校验：`null` 用 `Objects.requireNonNull(值, "参数 xxx 不能为 null")`，
  业务性非法输入抛 `IllegalArgumentException`，**异常消息用中文**；
- 工具方法与算法实现**不修改调用方传入的参数**，除非方法名与 Javadoc 明确说明是原地修改
  （例如 `SortAlgorithm#sort(int[])`）；
- 不使用 IDE 自动生成的空模板注释，也不要写「这是一个 getter」这类没有信息量的注释。

## 五、测试约定

项目**不引入 JUnit 等测试框架**（没有依赖管理，加 jar 会让编译方式变复杂），沿用现有的自测方式：

- 每个可独立验证的类都提供一个 `main` 自测入口，参考 `BubbleSort#main`：
  先打印示例对比，再跑标准用例，最后跑随机压测；
- 断言失败直接抛 `AssertionError`，消息里带上**中文说明 + 输入 / 期望 / 实际**，
  让错误一眼能定位，参考 `SortTestSupport#verify`；
- 期望结果用 JDK 自带实现做对照（排序用 `Arrays.sort`），避免自己再写一份「标准答案」；
- 涉及随机的测试必须**固定随机种子**，保证问题可复现；
- 改动后至少把相关类的 `main` 跑一遍，确认没有异常、输出中包含「通过」字样，再交付；
- 一次性的验证代码写在临时目录，验证完立即删除，**不要留在 `src/` 里**。

## 六、Java 8 约束

项目锁定在 Java 8，代码必须能在 JDK 8 下编译通过。禁止使用：

- 语法：`var`、文本块（`"""`）、`record`、`sealed`、`instanceof` 模式匹配；
- 集合工厂：`List.of`、`Map.of`、`Set.of`、`Map.entry`；
- 字符串与可选：`String#isBlank`、`String#strip`、`String#repeat`、`Optional#isEmpty`、
  `Objects#requireNonNullElse`；
- 其它常见的新版本 API：`Stream#toList`、`LocalDate#datesUntil`、
  `DateTimeFormatter` 的新增方法，以及 Java 9+ 才出现的重载。

时间处理统一使用 **`java.time`**（`LocalDate` / `LocalDateTime` / `Duration` / `DateTimeFormatter`），
新增代码不要引入 `java.util.Date`、`Calendar`、`SimpleDateFormat`。

若确实需要更高版本的语法或 API，先向用户说明理由并取得同意，不要自行放宽版本要求。

## 七、Git 约定

- 分支使用 `codex/` 前缀，除非用户另有指定；
- 提交信息用简体中文，一句话说明改了什么，例如「补充日期时间工具类」；
- 不要提交 `out/`、`.idea/`、个人 IDE 配置以及任何临时文件（这些已在 `.gitignore` 中）；
- 提交前先执行 `git status --short` 自查，**不要顺手把与本任务无关的改动一起提交**，
  用户可能正在并行编辑其它文件（例如 `B.java`、`src/Main.java`），动手前先确认改动范围。

## 八、变更纪律

- 改动尽量小而聚焦，只解决当前问题，不顺手重构无关代码；
- 新增公共方法要同步补上 Javadoc 与使用示例，必要时在 `main` 里加一条演示；
- 不确定的地方，在回答里明确写出所做的假设，不要静默改变既有行为；
- 交付时简要说明：改了哪些文件、关键取舍是什么、用什么方式验证过。
