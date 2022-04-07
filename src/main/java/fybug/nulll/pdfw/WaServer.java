package fybug.nulll.pdfw;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Condition;

import fybug.nulll.pdconcurrent.ReLock;
import fybug.nulll.pdconcurrent.SyLock;
import fybug.nulll.pdfw.loopex.LoopState;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * <h2>路径监听服务.</h2>
 * 该服务提供 {@link WatchService} 的监听服务，使用 {@link WatchKey} 对应监听的反馈<br/>
 * 监听到指定事件将会分发给对应监听键 {@link WatchKey} 的 {@link Loop} 进行处理<br/>
 * 可以使用线程池进行监听<br/>
 * 在该服务中声明需要监控哪些目录
 * <br/><br/>
 * 如需监听全部事件，请使用 {@link #KINDS_ALL}
 * <br/><br/>
 * 使用 {@link #watchPath(Path, WatchEvent.Kind[])} 注册需要监听的路径和事件，返回本次注册的监听键<br/>
 * 使用 {@link #sendKey(WatchKey, Loop)} 给监听键注册对应的 {@link Loop} 处理
 *
 * @author fybug
 * @version 0.0.1
 * @see Loop
 * @see WatchService
 * @since PDFileWatch 0.0.1
 */
@SuppressWarnings( "unchecked" )
public abstract
class WaServer<L extends Loop<?, ?>> implements Closeable {
    /**
     * 监听全部事件
     *
     * @see StandardWatchEventKinds#ENTRY_CREATE
     * @see StandardWatchEventKinds#ENTRY_MODIFY
     * @see StandardWatchEventKinds#ENTRY_DELETE
     */
    public final static WatchEvent.Kind<Path>[] KINDS_ALL =
            new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE};

    /** 锁 */
    protected final ReLock LOCK = SyLock.newReLock();
    /** 状态管理 */
    protected final Condition WAIT = LOCK.newCondition();

    // 监听服务
    private final WatchService watchService;
    // 线程池
    private final Optional<ExecutorService> polp;
    // 当前线程
    private volatile Thread thread;

    // 处理对象映射
    protected final Map<WatchKey, L> keymap = new HashMap<>();

    // 是否关闭
    private boolean isClose = false;

    //----------------------------------------------------------------------------------------------

    /** 构造并注册监听服务和线程池 */
    protected
    WaServer(ExecutorService executorService) throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        polp = Optional.ofNullable(executorService);
        run();
    }

    //----------------------------------------------------------------------------------------------

    /** 注册监听的目录 */
    protected
    WatchKey watchPath(@NotNull Path path, WatchEvent.Kind<Path>[] kinds) throws IOException
    { return path.register(watchService, kinds); }

    /** 注册处理程序 */
    protected
    L sendKey(WatchKey key, L sendLoop) {
        keymap.put(key, sendLoop);
        return sendLoop;
    }

    //----------------------------------------------------------------------------------------------

    // 启动监听线程
    private
    void run() {
        Runnable run = () -> {
            // 当前线程
            thread = Thread.currentThread();
            WatchKey key;

            /* 开始监听 */
            try ( var watchService = this.watchService ) {
                /* 直到中断 */
                while( LOCK.read(() -> !thread.isInterrupted() && !isClose) ){
                    // 当前监听的 key，自动阻塞到可用
                    key = watchService.take();
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
                LOCK.write(() -> {
                    isClose = true;
                    WAIT.signalAll();
                });
            }
        };

        // 开始执行
        polp.ifPresentOrElse(p -> p.submit(run), () -> {
            thread = new Thread(run);
            thread.start();
        });
    }

    /** 触发后的处理 */
    private
    void state(WatchKey key) {
        // 当前路径的处理程序
        var loop = LOCK.read(() -> keymap.get(key));
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
        LOCK.write(() -> {
            if (isClose)
                return;

            // 中断线程
            thread.interrupt();
            // 等待结束
            try {
                WAIT.await();
            } catch ( InterruptedException e ) {
                e.printStackTrace();
            }
            // sub close
            close0();
        });
    }

    /** 关闭操作 */
    protected
    void close0() {
        // clear
        for ( Object v : keymap.values().toArray() )
            ((L) v).close();
        keymap.clear();
    }

    // Close check
    //----------------------------

    /** 检查是否关闭 */
    protected final
    void checkClose() throws IOException {
        if (isClose)
            throw new IOException("watchService is close");
    }

    /** 检查是否关闭 */
    public final
    boolean isClose() { return LOCK.read(() -> isClose); }
}
