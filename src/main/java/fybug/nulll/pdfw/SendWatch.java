package fybug.nulll.pdfw;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;

import fybug.nulll.pdfw.loopex.FileSend;
import fybug.nulll.pdfw.loopex.SendDir;
import fybug.nulll.pdfw.loopex.SendFile;

/**
 * <h2>单向路径监控服务.</h2>
 * 监控一组路径，每个路径对应一个 {@link SendLoop}
 * <p>
 * 父目录被监控时，子目录被修改的事件不会往上传
 *
 * @author fybug
 * @version 0.0.1
 * @since PDFileWatch 0.0.1
 */
public
class SendWatch extends WaServer<SendLoop> {
    // 当前监听的路径
    final Set<String> WathcPath = new CopyOnWriteArraySet<>();
    // 触发过的路径，父目录 -> 子目录
    final Map<String, String> watchSeet = new ConcurrentHashMap<>();

    //----------------------------------------------------------------------------------------------

    SendWatch(ExecutorService executorService) throws IOException {super(executorService);}

    //----------------------------------------------------------------------------------------------

    /**
     * 监听目录
     *
     * @param path 要监听的目录
     * @param kind 要监听的类型
     *
     * @return 处理程序
     *
     * @throws IOException 无法注册或监听服务被关闭
     */
    @SafeVarargs
    @NotNull
    public final
    SendLoop watchDir(@NotNull Path path, @NotNull WatchEvent.Kind<Path>... kind) throws IOException
    {
        // 注册后的 key
        var key = watchPath(path.toAbsolutePath(), kind);
        return sendKey(key, new SendLoop(key, this, path));
    }

    /**
     * 监听文件
     *
     * @param path 要监听的文件
     * @param kind 要监听的类型
     *
     * @return 处理程序
     *
     * @throws IOException 无法注册或监听服务被关闭
     * @see FileSend
     */
    @SafeVarargs
    @NotNull
    public final
    SendLoop watchFil(@NotNull Path path, @NotNull WatchEvent.Kind<Path>... kind) throws IOException
    {
        // 注册后的 key
        var key = watchPath(path.toAbsolutePath().getParent(), kind);
        return sendKey(key, new FileSend(key, this, path));
    }

    /**
     * 监听目录下的目录变更
     * <p>
     * 删除事件无法判断类型
     *
     * @param path 要监听的目录
     * @param kind 要监听的类型
     *
     * @return 处理程序
     *
     * @throws IOException 无法注册或监听服务被关闭
     * @see SendDir
     */
    @SafeVarargs
    @NotNull
    public final
    SendLoop checkDir(@NotNull Path path, @NotNull WatchEvent.Kind<Path>... kind) throws IOException
    {
        // 注册后的 key
        var key = watchPath(path.toAbsolutePath(), kind);
        return sendKey(key, new SendDir(key, this, path));
    }

    /**
     * 监听目录下的文件变更
     * <p>
     * 删除事件无法判断类型
     *
     * @param path 要监听的目录
     * @param kind 要监听的类型
     *
     * @return 处理程序
     *
     * @throws IOException 无法注册或监听服务被关闭
     * @see SendFile
     */
    @SafeVarargs
    @NotNull
    public final
    SendLoop checkFil(@NotNull Path path, @NotNull WatchEvent.Kind<Path>... kind) throws IOException
    {
        // 注册后的 key
        var key = watchPath(path.toAbsolutePath(), kind);
        return sendKey(key, new SendFile(key, this, path));
    }

    //------------------------------

    @Override
    protected
    WatchKey watchPath(@NotNull Path path, WatchEvent.Kind<Path>[] kind) throws IOException {
        WathcPath.add(path.toString());
        return super.watchPath(path, kind);
    }

    /** 移除当前处理程序 */
    void removeLoop(WatchKey key) {
        var l = keymap.remove(key);
        WathcPath.remove(l.getPath());
        watchSeet.remove(l.getPath());
    }

    //----------------------------------------------------------------------------------------------

    @Override
    protected
    void close0() {
        super.close0();
        WathcPath.clear();
        watchSeet.clear();
    }
}
