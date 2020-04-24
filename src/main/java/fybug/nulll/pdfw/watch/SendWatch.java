package fybug.nulll.pdfw.watch;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import fybug.nulll.pdfw.PDFileWatch;
import fybug.nulll.pdfw.WaServer;
import fybug.nulll.pdfw.loopex.FileSend;
import fybug.nulll.pdfw.loopex.LoopState;
import fybug.nulll.pdfw.loopex.SendDir;
import fybug.nulll.pdfw.loopex.SendFile;
import lombok.Setter;
import lombok.experimental.Accessors;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * <h2>单向路径监控服务.</h2>
 * 监控一组路径，每个路径对应一个 {@link SendLoop} <br/>
 * 只监控声明的路径，子路径不监控<br/>
 * 父目录被监控时，子目录被修改的事件不会往上传
 * <br/><br/>
 * <pre>使用示例：
 *     public static
 *     void main(String[] args) {
 *         SendWatch sendWatch;
 *         SendLoop loop;
 *
 *         try {
 *             sendWatch = PDFileWatch.sendWatch().build();
 *             // 注册，监听单个文件路径的所有事件
 *             loop = sendWatch.watchFil(Path.of("a.tmp"), WaServer.KINDS_ALL);
 *             // 监听创建事件回调
 *             loop.addCall(ENTRY_CREATE, (event, path) -> LoopState.WATCH_NEXT);
 *             // 监听默认回调
 *             loop.addDefaCall((event, path) -> LoopState.WATCH_NEXT);
 *         } catch ( IOException e ) {
 *             e.printStackTrace();
 *         }
 *     }
 * </pre>
 *
 * @author fybug
 * @version 0.0.1
 * @since watch 0.0.1
 */
public
class SendWatch extends WaServer<SendLoop> {

    // 当前监听的路径
    final Set<String> WathcPath = new HashSet<>();
    // 触发过的路径，父目录 -> 子目录
    final Map<String, String> watchSeet = new HashMap<>();

    //----------------------------------------------------------------------------------------------

    public
    SendWatch(ExecutorService executorService) throws IOException {super(executorService);}

    //----------------------------------------------------------------------------------------------

    /**
     * 监听目录
     * <p>
     * 实际监听父目录，只接受该路径的事件
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
    SendLoop watchDir(@NotNull Path path, WatchEvent.Kind<Path>... kind) throws IOException {
        return LOCK.trywrite(IOException.class, () -> {
            checkClose();
            // 注册后的 key
            var key = watchPath(path.toAbsolutePath(), kind);
            return sendKey(key, new SendLoop(key, this, path));
        });
    }

    /**
     * 监听文件
     * <p>
     * 实际监听父目录，只接受该路径的事件
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
    FileSend watchFil(@NotNull Path path, WatchEvent.Kind<Path>... kind) throws IOException {
        return (FileSend) LOCK.trywrite(IOException.class, () -> {
            checkClose();
            // 注册后的 key
            var key = watchPath(path.toAbsolutePath().getParent(), kind);
            return sendKey(key, new FileSend(key, this, path));
        });
    }

    /**
     * 监听该目录下的子目录的事件
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
    SendDir checkDir(@NotNull Path path, WatchEvent.Kind<Path>... kind) throws IOException {
        return (SendDir) LOCK.trywrite(IOException.class, () -> {
            checkClose();
            // 注册后的 key
            var key = watchPath(path.toAbsolutePath(), kind);
            return sendKey(key, new SendDir(key, this, path));
        });
    }

    /**
     * 监听目录下的子文件的事件
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
    SendFile checkFil(@NotNull Path path, WatchEvent.Kind<Path>... kind) throws IOException {
        return (SendFile) LOCK.trywrite(IOException.class, () -> {
            checkClose();
            // 注册后的 key
            var key = watchPath(path.toAbsolutePath(), kind);
            return sendKey(key, new SendFile(key, this, path));
        });
    }

    //------------------------------

    @Override
    protected
    WatchKey watchPath(@NotNull Path path, WatchEvent.Kind<Path>[] kind) throws IOException {
        var k = super.watchPath(path, kind);
        WathcPath.add(path.toString());
        return k;
    }

    /** 移除当前处理程序 */
    void removeLoop(WatchKey key) {
        LOCK.write(() -> {
            var l = keymap.remove(key);
            WathcPath.remove(l.getPath());
            watchSeet.remove(l.getPath());
        });
    }

    //----------------------------------------------------------------------------------------------

    /** 检查父目录是否被监听 */
    boolean checkParent(String path, Path parpath, Object context, WatchEvent.Kind<?> kind) {
        return LOCK.write(() -> {
            if (WathcPath.contains(parpath.toString()) && context instanceof Path)
                // 记录当前目录，父目录 -> 当前目录名
                watchSeet.put(parpath.toString(), new File(path).getName());

            /* 检查目录是否被触发过 */
            if (kind == ENTRY_MODIFY && watchSeet.containsKey(path))
                // 检查子目录
                return !watchSeet.remove(path).equals(context.toString());
            return true;
        });
    }

    //----------------------------------------------------------------------------------------------

    @Override
    protected
    void close0() {
        super.close0();
        WathcPath.clear();
        watchSeet.clear();
    }

    /*--------------------------------------------------------------------------------------------*/

    /** 获取构造工具 */
    @NotNull
    public static
    Build build() { return new Build(); }

    /**
     * <h2>监听服务构造工具.</h2>
     * 使用 {@link #pool(ExecutorService)} 修改监听用的线程池
     *
     * @author fybug
     * @version 0.0.1
     * @since SendWatch 0.0.1
     */
    @Accessors( fluent = true, chain = true )
    public static final
    class Build {
        /** 监听执行的线程池 */
        @Setter private ExecutorService pool = null;

        /** 构造监听服务 */
        @NotNull
        public
        SendWatch build() throws IOException {return new SendWatch(pool);}
    }
}
