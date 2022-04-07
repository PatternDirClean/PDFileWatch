# PDFileWatch

![java library](https://img.shields.io/badge/type-Libary-gr.svg "type")
![JDK 13](https://img.shields.io/badge/JDK-13-green.svg "SDK")
![Gradle 6.0.1](https://img.shields.io/badge/Gradle-6.0.1-04303b.svg "tool")
![Apache 2](https://img.shields.io/badge/license-Apache%202-blue.svg "License")

-- [Java Doc](https://apidoc.gitee.com/PatternDirClean/PDFileWatch) --

-------------------------------------------------------------------------------

## 简介

java 文件监听服务库，用与快速创建监听文件和目录变化的服务

## 快速使用
监听单个文件
```java
public class Main {
    public static void main(String[] arg) {
        SendWatch sendWatch;
        SendLoop loop;

        try {
            // 构建监听服务
            sendWatch = PDFileWatch.sendWatch().build();

            // 注册，监听单个文件的所有事件，返回本次监听的实例
            // WaServer.KINDS_ALL 是一个数组，包含所有的 WatchEvent.Kind 事件，除了 OVERFLOW
            loop = sendWatch.watchFil(Path.of("a.tmp"), WaServer.KINDS_ALL);

            // 监听创建事件回调，允许多个回调按顺序触发，内部有一个回调链
            loop.addCall(StandardWatchEventKinds.ENTRY_CREATE, (event, path) -> {
                // 事件触发处理
                // 允许回调链向下继续触发
                return LoopState.WATCH_NEXT;
            });

            // 监听默认回调，如果当前触发的事件没有注册对应的回调进行处理，则会触发该回调链
            loop.addDefaCall((event, path) -> {
                // 事件触发回调
                // 允许继续触发
                return LoopState.WATCH_NEXT;
            });

            // 不需要继续监听时记得关闭当前监听实例
            loop.close();
            // 不需要进行任何文件监听时记得关掉该服务
            sendWatch.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
}
```
> 内部已有并发处理，启动服务后会有一个线程再后面负责监听系统传来的变化，所以在不需要时记得 `close()` 服务和监听对象
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