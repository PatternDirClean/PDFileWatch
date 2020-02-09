package fybug.nulll.pdfw;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * <h2>路径监听服务.</h2>
 * 使用线程池监听 {@link WatchService}
 * 在该服务中声明需要监控哪些目录
 * <p>
 * 如需监听全部事件，请使用 {@link #KINDS_ALL}
 *
 * @author fybug
 * @version 0.0.1
 * @since PDFileWatch 0.0.1
 */
public abstract
class WaServer<L extends Loop<?, ?>> implements Closeable {
    /** 监听全部事件 */
    public final static WatchEvent.Kind<Path>[] KINDS_ALL =
            new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE};
    // 监听服务
    private final WatchService watchService;
    // 线程池
    private final ExecutorService polp;
    // 当前线程
    private volatile Thread thread;

    // 处理对象映射
    protected final Map<WatchKey, L> keymap = new ConcurrentHashMap<>();

    // 是否关闭
    private final AtomicBoolean isClose = new AtomicBoolean(false);

    //----------------------------------------------------------------------------------------------

    /** 构造并注册监听服务和线程池 */
    protected
    WaServer(ExecutorService executorService) throws IOException {
        this.watchService = FileSystems.getDefault().newWatchService();
        polp = executorService;
        run();
    }

    //----------------------------------------------------------------------------------------------

    /** 注册监听的目录 */
    protected
    WatchKey watchPath(@NotNull Path path, WatchEvent.Kind<Path>[] kinds) throws IOException {
        checkClose();
        return path.register(watchService, kinds);
    }

    // 记录处理程序
    protected
    L sendKey(WatchKey key, L sendLoop) {
        keymap.put(key, sendLoop);
        return sendLoop;
    }

    // run task
    //----------------------------------------------------------------------------------------------

    // 启动监听线程
    private
    void run() {
        polp.submit(() -> {
            // 当前线程
            thread = Thread.currentThread();
            /* 开始监听 */
            try ( var watchService = this.watchService ) {
                /* 直到中断 */
                while( !thread.isInterrupted() ){
                    // 当前监听的 key
                    var key = watchService.take();
                    // 处理
                    state(key);
                    // 重置监听
                    key.reset();
                }
            } catch ( InterruptedException e ) {
                // ignored
            } catch ( IOException e ) {
                e.printStackTrace();
            } finally {
                isClose.set(true);
            }
        });
    }

    /** 触发后的处理 */
    protected
    void state(WatchKey key) {
        // 当前路径的处理程序
        var loop = keymap.get(key);
        /* 处理当前事件队列 */
        for ( WatchEvent<?> event : key.pollEvents() )
            // 调取回调
            if (loop.runback(key, event) == LoopState.WATCH_CLOSE)
                break;
    }

    // Close
    //----------------------------------------------------------------------------------------------

    @Override
    public final
    void close() {
        if (!isClose()) {
            // 等待线程载入
            while( thread == null )
                ;
            // 中断线程
            thread.interrupt();
            // 等待结束
            while( !isClose() )
                ;

            // sub close
            close0();
        }
    }

    /** 关闭操作 */
    protected
    void close0() {
        // clear
        keymap.forEach((k, v) -> v.close());
        keymap.clear();
    }

    // Close check
    //----------------------------

    /** 检查是否关闭 */
    protected final
    void checkClose() throws IOException {
        if (isClose())
            throw new IOException("watchService is close");
    }

    /** 检查是否关闭 */
    public final
    boolean isClose() { return isClose.get(); }
}
