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
import java.util.stream.Stream;

import fybug.nulll.pdconcurrent.SyLock;
import fybug.nulll.pdfw.loopex.LoopState;

import static fybug.nulll.pdfw.loopex.LoopState.WATCH_CLOSE;
import static fybug.nulll.pdfw.loopex.LoopState.WATCH_DOME;

/**
 * <h2>处理程序.</h2>
 * 监听反馈，用于根据 {@link WatchEvent.Kind} 分发对应的回调链<br/>
 * 可以加入多个回调接口，按顺序进行触发<br/>
 * 可添加默认触发的回调，在没有声明回调的 {@link WatchEvent.Kind} 中使用<br/>
 * 根据回调接口返回的 {@link LoopState} 声明当前状态，只有 {@link LoopState#WATCH_NEXT} 状态才会继续处理<br/>
 * <br/><br/>
 * 使用 {@link #runback(WatchKey, WatchEvent)} 触发回调链<br/>
 * 使用 {@link #addCall(WatchEvent.Kind, StateBack...)} 添加回调<br/>
 * 使用 {@link #addDefaCall(StateBack...)} 添加默认回调
 *
 * @author fybug
 * @version 0.0.1
 * @see StateBack
 * @since PDFileWatch 0.0.1
 */
public abstract
class Loop<W extends WaServer<T>, T extends Loop<W, T>> implements Closeable {
    /** 拥有此处理对象的监听对象 */
    protected final W parent;

    /** 锁 */
    protected final SyLock LOCK = SyLock.newObjLock();

    // 回调链映射
    private final Map<WatchEvent.Kind<Path>, List<StateBack>> CALL_BACK = new HashMap<>(3, 1.0f);
    // 默认回调链
    private final List<StateBack> DEFA_CALL_BACK = new ArrayList<>();

    // 是否关闭
    private boolean close = false;

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
        LOCK.write(() -> {
            if (!isClose())
                checkMap(kind).addAll(Arrays.asList(runnable));
        });
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
        LOCK.write(() -> {
            if (!isClose())
                DEFA_CALL_BACK.addAll(Arrays.asList(runnable));
        });
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

    /** 校验事件是否重复 */
    protected
    boolean runCheck(WatchKey key, WatchEvent<?> event, WatchEvent.Kind<?> kind)
    { return event.count() < 2; }

    /** 运行回调 */
    protected abstract
    LoopState runcall(WatchKey key, WatchEvent<?> event, Stream<StateBack> stream);

    /** 开始处理 */
    final
    LoopState runback(WatchKey key, WatchEvent<?> event) {
        if (LOCK.read(() -> close))
            return WATCH_CLOSE;

        // 当前事件类型
        var kind = event.kind();
        if (!runCheck(key, event, kind))
            return WATCH_DOME;

        // 当前状态
        var state =
                // 运行处理事件
                runcall(key, event, LOCK.read(() -> CALL_BACK.getOrDefault(kind, DEFA_CALL_BACK)
                                                             .stream()));
        if (state == WATCH_CLOSE)
            close();
        return state;
    }

    //----------------------------------------------------------------------------------------------

    /** 获取监听的路径 */
    public
    String getPath() {return toPath();}

    /**
     * 获取目标路径
     *
     * @return path
     */
    @NotNull
    public abstract
    String toPath();

    //----------------------------------------------------------------------------------------------

    /** 检查是否关闭 */
    public
    boolean isClose() {return close;}

    @Override
    public final
    void close() {
        LOCK.write(() -> {
            if (!isClose()) {
                // gc callback
                CALL_BACK.values().forEach(List::clear);
                CALL_BACK.clear();
                DEFA_CALL_BACK.clear();
                // sub close
                close0();
            }
        });
    }

    /** 关闭操作 */
    protected abstract
    void close0();
}
