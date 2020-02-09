package fybug.nulll.pdfw;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import fybug.nulll.pdfw.funciton.NextState;
import fybug.nulll.pdfw.funciton.StateBack;

import static fybug.nulll.pdfw.LoopState.WATCH_CLOSE;
import static fybug.nulll.pdfw.LoopState.WATCH_DOME;

/**
 * <h2>处理程序.</h2>
 * 用于处理监听到的路径变化，并使用链式回调处理
 * 返回 {@link LoopState} 声明当前状态
 * 可使用 {@link NextState} 默认声明为 {@link LoopState#WATCH_NEXT}
 *
 * @author fybug
 * @version 0.0.1
 * @see LoopState
 * @see StateBack
 * @see NextState
 * @since PDFileWatch 0.0.1
 */
public abstract
class Loop<W extends WaServer, T extends Loop<W, ?>> implements Closeable {
    /** 拥有此处理对象的监听对象 */
    protected final W parent;

    // 回调链映射
    private final Map<WatchEvent.Kind<Path>, List<StateBack>> CALL_BACK = new HashMap<>(3, 1.0f);
    // 默认回调链
    private final List<StateBack> DEFA_CALL_BACK = new ArrayList<>();

    // 是否关闭
    private final AtomicBoolean close = new AtomicBoolean(false);

    //----------------------------------------------------------------------------------------------

    /** 注册当前事件和 key */
    protected
    Loop(W ws) { parent = ws; }

    //----------------------------------------------------------------------------------------------

    /**
     * 追加处理回调
     *
     * @param kind     处理的事件
     * @param runnable 处理回调
     *
     * @return this
     */
    @NotNull
    public
    T addCall(@NotNull WatchEvent.Kind<Path> kind, @NotNull StateBack... runnable) {
        if (!isClose())
            checkMap(kind).addAll(Arrays.asList(runnable));
        return (T) this;
    }

    /**
     * 追加默认回调
     *
     * @param runnable 处理回调
     *
     * @return this
     */
    @NotNull
    public
    T addDefaCall(@NotNull StateBack... runnable) {
        if (!isClose())
            DEFA_CALL_BACK.addAll(Arrays.asList(runnable));
        return (T) this;
    }

    //---------------------------------------

    // 检查当前映射
    private
    List<StateBack> checkMap(WatchEvent.Kind<Path> kind) {
        List<StateBack> list;

        // 检查是否已加载
        if (!CALL_BACK.containsKey(kind))
            CALL_BACK.put(kind, list = new ArrayList<>());
        else
            list = CALL_BACK.get(kind);

        return list;
    }

    //----------------------------------------------------------------------------------------------

    /** 校验 */
    protected
    boolean runCheck(WatchKey key, WatchEvent<?> event, WatchEvent.Kind<?> kind)
    { return event.count() < 2; }

    /** 运行回调 */
    protected abstract
    LoopState runcall(WatchKey key, WatchEvent<?> event, Stream<StateBack> stream);

    /** 开始处理 */
    LoopState runback(WatchKey key, WatchEvent<?> event) {
        if (isClose())
            return WATCH_CLOSE;

        // 当前事件类型
        var kind = event.kind();
        if (!runCheck(key, event, kind))
            return WATCH_DOME;

        // 当前状态
        var state =
                // 运行处理事件
                runcall(key, event, CALL_BACK.getOrDefault(kind, DEFA_CALL_BACK).stream());
        if (state == WATCH_CLOSE)
            close();

        return state;
    }

    // get Path
    //----------------------------------------------------------------------------------------------

    /** 获取监听的路径 */
    protected
    String getPath() {return toPath();}

    /**
     * 获取目标路径
     *
     * @return path
     */
    @NotNull
    public abstract
    String toPath();

    // Close
    //----------------------------------------------------------------------------------------------

    /** 检查是否关闭 */
    public
    boolean isClose() {return close.get();}

    @Override
    public final
    void close() {
        if (!isClose()) {
            // gc callback
            CALL_BACK.values().forEach(List::clear);
            CALL_BACK.clear();
            DEFA_CALL_BACK.clear();
            // sub close
            close0();
        }
    }

    /** 关闭操作 */
    protected abstract
    void close0();
}
