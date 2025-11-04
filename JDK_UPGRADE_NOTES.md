# JDK 升级说明（11 → 17）

## ✅ 已完成的更改

### 1. Maven配置（backend/pom.xml）
```xml
<properties>
    <java.version>17</java.version>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
</properties>
```

### 2. 更新所有文档
- ✅ README.md
- ✅ QUICKSTART.md
- ✅ START_HERE.md
- ✅ backend/README.md
- ✅ docs/DEPLOY.md
- ✅ start-backend.bat

---

## 🔧 需要在你的环境中做的事

### Windows 系统

#### 方式一：使用安装包（推荐）

1. **下载JDK 17**
   - 访问：https://www.oracle.com/java/technologies/downloads/#java17
   - 或者使用OpenJDK：https://adoptium.net/zh-CN/temurin/releases/?version=17

2. **安装JDK 17**
   - 运行下载的安装包
   - 记住安装路径（例如：`C:\Program Files\Java\jdk-17`）

3. **配置环境变量**
   
   **方式A：图形界面**
   ```
   1. 右键"此电脑" → 属性 → 高级系统设置 → 环境变量
   2. 系统变量中找到 JAVA_HOME：
      - 如果存在：修改为新的JDK 17路径
      - 如果不存在：新建，值为：C:\Program Files\Java\jdk-17
   3. 系统变量中找到 Path：
      - 添加：%JAVA_HOME%\bin
   4. 点击确定保存
   ```

   **方式B：命令行（管理员PowerShell）**
   ```powershell
   # 设置JAVA_HOME
   [System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-17", "Machine")
   
   # 添加到Path（如果还没有）
   $path = [System.Environment]::GetEnvironmentVariable("Path", "Machine")
   [System.Environment]::SetEnvironmentVariable("Path", "$path;%JAVA_HOME%\bin", "Machine")
   ```

4. **验证安装**
   ```bash
   # 重新打开命令行窗口
   java -version
   # 应该显示：openjdk version "17.x.x" 或 java version "17.x.x"
   
   javac -version
   # 应该显示：javac 17.x.x
   ```

#### 方式二：使用包管理器

**使用 Chocolatey**
```powershell
# 如果还没安装Chocolatey，先安装：
# https://chocolatey.org/install

# 安装JDK 17
choco install openjdk17

# 验证
java -version
```

**使用 Scoop**
```powershell
# 如果还没安装Scoop，先安装：
# https://scoop.sh/

# 安装JDK 17
scoop bucket add java
scoop install openjdk17

# 验证
java -version
```

---

## 📝 JDK 17 的优势

相比JDK 11，JDK 17提供了：

1. **性能提升**
   - 更好的垃圾回收（G1 GC优化）
   - 启动速度更快
   - 内存占用更小

2. **新特性**
   - Sealed Classes（密封类）
   - Pattern Matching（模式匹配）
   - Text Blocks（文本块）
   - Record Classes（记录类）

3. **长期支持（LTS）**
   - JDK 17是LTS版本，支持到2029年
   - 更稳定，更适合生产环境

4. **安全性**
   - 更多的安全补丁
   - 更好的加密支持

---

## 🔄 迁移检查清单

- [x] 更新pom.xml中的Java版本
- [x] 更新所有文档中的JDK版本说明
- [x] 更新启动脚本中的提示信息
- [ ] 在本地安装JDK 17
- [ ] 配置JAVA_HOME环境变量
- [ ] 重启IDE（IDEA/Eclipse/VSCode）
- [ ] 测试编译：`mvn clean compile`
- [ ] 测试运行：`mvn spring-boot:run`

---

## 🆘 常见问题

### Q1: 我已经安装了多个JDK版本怎么办？

**A**: 使用JAVA_HOME环境变量指定要使用的版本

```bash
# 查看当前使用的版本
java -version

# 临时切换（仅当前会话）
set JAVA_HOME=C:\Program Files\Java\jdk-17

# 永久切换：修改系统环境变量JAVA_HOME
```

### Q2: IntelliJ IDEA 如何切换JDK？

**A**: 
1. File → Project Structure → Project
2. SDK: 选择JDK 17（如果没有，点Add SDK导入）
3. Language Level: 选择17

### Q3: Maven仍然使用旧的JDK？

**A**: 
```bash
# 检查Maven使用的JDK
mvn -version

# 如果不是JDK 17，确保：
# 1. JAVA_HOME指向JDK 17
# 2. 重新打开命令行窗口
# 3. 或者在Maven配置文件中指定
```

### Q4: 编译时报错：unsupported class file version

**A**: 这说明你的JDK版本太低
```bash
# 确保使用JDK 17
java -version

# 清理并重新编译
mvn clean compile
```

---

## 📚 推荐资源

- [JDK 17下载（Oracle）](https://www.oracle.com/java/technologies/downloads/#java17)
- [JDK 17下载（Adoptium）](https://adoptium.net/zh-CN/temurin/releases/?version=17)
- [JDK 17新特性](https://openjdk.org/projects/jdk/17/)
- [Java 17迁移指南](https://docs.oracle.com/en/java/javase/17/migrate/getting-started.html)

---

## ✅ 验证升级成功

升级完成后，运行以下命令验证：

```bash
# 1. 验证Java版本
java -version
# 输出应包含 "17.x.x"

# 2. 验证Maven能识别
mvn -version
# Java version应该是17.x.x

# 3. 测试编译项目
cd backend
mvn clean compile
# 应该成功编译，无错误

# 4. 测试运行项目
mvn spring-boot:run
# 应该成功启动
```

---

**升级完成后，项目就可以使用JDK 17的所有特性了！** 🎉

