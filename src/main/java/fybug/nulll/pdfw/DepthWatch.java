package fybug.nulll.pdfw;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

import fybug.nulll.pdfw.loopex.DepthDir;
import fybug.nulll.pdfw.loopex.DepthFile;
import fybug.nulll.pdfw.loopex.SendDir;
import fybug.nulll.pdfw.loopex.SendFile;

// todo test

/**
 * <h2>深度路径监控服务.</h2>
 * 监控一组路径，每一个路径的子路径都会被监控，后续新建的路径也会被监控
 * 每一个路径对应一个 {@link DepthLoop}
 *
 * @author fybug
 * @version 0.0.1
 * @since PDFileWatch 0.0.1
 */
public
class DepthWatch extends WaServer<DepthLoop> {
    // 路径映射
    final ConcurrentMap<WatchKey, String> pathmap = new ConcurrentHashMap<>();
    final ConcurrentMap<String, WatchKey> keysmap = new ConcurrentHashMap<>();

    //----------------------------------------------------------------------------------------------

    DepthWatch(ExecutorService executorService) throws IOException { super(executorService); }

    //----------------------------------------------------------------------------------------------

    /**
     * 监听目录
     *
     * @param path 要监听的目录
     *
     * @return 处理程序
     *
     * @throws IOException 无法注册或监听服务被关闭
     */
    @NotNull
    public final
    DepthLoop watchDir(@NotNull Path path) throws IOException {
        var loop = new DepthLoop(this, path);
        forpath(loop, path, WaServer.KINDS_ALL);
        return loop;
    }

    /**
     * 监听目录下的目录变更
     * <p>
     * 删除事件无法判断类型
     *
     * @param path 要监听的目录
     *
     * @return 处理程序
     *
     * @throws IOException 无法注册或监听服务被关闭
     * @see SendDir
     */
    @NotNull
    public final
    DepthLoop checkDir(@NotNull Path path) throws IOException {
        var loop = new DepthDir(this, path);
        forpath(loop, path, WaServer.KINDS_ALL);
        return loop;
    }

    /**
     * 监听目录下的文件变更
     * <p>
     * 删除事件无法判断类型
     *
     * @param path 要监听的目录
     *
     * @return 处理程序
     *
     * @throws IOException 无法注册或监听服务被关闭
     * @see SendFile
     */
    @NotNull
    public final
    DepthLoop checkFil(@NotNull Path path) throws IOException {
        var loop = new DepthFile(this, path);
        forpath(loop, path, WaServer.KINDS_ALL);
        return loop;
    }

    //----------------------------------------------------------------------------------------------

    // 监听全部子目录
    void forpath(DepthLoop loop, Path path, WatchEvent.Kind<Path>... kinds) throws IOException {
        try {
            Files.walk(path.toAbsolutePath())
                 // 过滤出目录
                 .filter(v -> Files.isDirectory(v))
                 // 绑定监听
                 .forEach(v -> {
                     try {
                         // 绑定监听
                         var key = watchPath(v, kinds);
                         // 记录 Key
                         loop.binKey(key);
                         // 记录路径
                         pathmap.put(key, v.toString());
                         keysmap.put(v.toString(), key);
                         // 记录处理程序
                         sendKey(key, loop);
                     } catch ( IOException e ) {
                         throw new errors(e.getMessage());
                     }
                 });
        } catch ( errors error ) {
            throw new IOException(error.getMessage());
        }
    }

    /** 移除当前处理程序 */
    void removeLoop(WatchKey key) {
        keymap.remove(key);
        if (pathmap.containsKey(key))
            keysmap.remove(pathmap.remove(key));
    }

    //----------------------------------------------------------------------------------------------

    @Override
    protected
    void close0() {
        super.close0();
        pathmap.clear();
        keysmap.clear();
    }

    //----------------------------------------------------------------------------------------------

    private static
    class errors extends RuntimeException {
        public
        errors(String message) { super(message); }
    }
}
