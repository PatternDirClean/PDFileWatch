# PDFileWatch

![java library](https://img.shields.io/badge/type-Libary-gr.svg "type")
![JDK 13](https://img.shields.io/badge/JDK-13-green.svg "SDK")
![Gradle 6.0.1](https://img.shields.io/badge/Gradle-6.0.1-04303b.svg "tool")
![Apache 2](https://img.shields.io/badge/license-Apache%202-blue.svg "License")

-- [Java Doc](https://apidoc.gitee.com/PatternDirClean/PDFileWatch) --

-------------------------------------------------------------------------------

## 简介

文件系统监听工具，用于监听文件系统的变化，并使用回调处理。<br/>
可进行深度监听，回调使用链式处理。

## 使用方法

### 示例
监听单个文件
```java
public class Main {
    public static void main(String[] arg) {
        SendWatch sendWatch;
        SendLoop loop;
        
        try {
            sendWatch = PDFileWatch.sendWatch().build();
        
            // 注册，监听单个文件路径的所有事件
            loop = sendWatch.watchFil(Path.of("a.tmp"), WaServer.KINDS_ALL);
        
            // 监听创建事件回调
            loop.addCall(StandardWatchEventKinds.ENTRY_CREATE, (event, path) -> LoopState.WATCH_NEXT);
        
            // 监听默认回调
            loop.addDefaCall((event, path) -> LoopState.WATCH_NEXT);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
}
```

### 导入
请导入其 `jar` 文件,文件在 **发行版** 或项目的 **jar** 文件夹下可以找到

**发行版中可以看到全部版本<br/>项目下的 jar 文件夹是当前最新的每夜版**

依赖的同系列的项目
- [PDConcurrent](https://gitee.com/PatternDirClean/PDConcurrent)

可通过 **WIKI**、**java doc** 或者 **测试类** 查看示例

## 分支说明
**dev-master**：当前的开发分支，可以拿到最新的每夜版 jar

**releases**：当前发布分支，稳定版的源码

-------------------------------------------------------------------------------

### 提供bug反馈或建议

- [码云Gitee](https://gitee.com/PatternDirClean/PDFileWatch/issues)
- [Github](https://github.com/PatternDirClean/PDFileWatch/issues)