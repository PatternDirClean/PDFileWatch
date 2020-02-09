package fybug.nulll.pdfw;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import fybug.nulll.pdfw.funciton.StateBack;

import static fybug.nulll.pdfw.LoopState.WATCH_CLOSE;
import static fybug.nulll.pdfw.LoopState.WATCH_DOME;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * <h2>SendWatch 对接处理程序.</h2>
 * 可记录每次触发的目录的父目录
 * 在子目录已处理的情况下不传递给父目录
 * <p>
 * 如需解除监听，请直接调用该对象的 {@link #close()}
 *
 * @author fybug
 * @version 0.0.1
 * @since PDFileWatch 0.0.1
 */
public
class SendLoop extends Loop<SendWatch, SendLoop> {
    // 当前注册的 key
    private final WatchKey KEY;
    // 当前路径
    private final String path;

    //----------------------------------------------------------------------------------------------

    protected
    SendLoop(WatchKey key, SendWatch ws, Path path) {
        super(ws);
        KEY = key;
        this.path = path.toAbsolutePath().toString();
    }

    //----------------------------------------------------------------------------------------------

    @Override
    protected
    boolean runCheck(WatchKey key, WatchEvent<?> event, WatchEvent.Kind<?> kind) {
        if (!super.runCheck(key, event, kind))
            return false;

        // 当前触发的目标
        var context = event.context();
        var path = getPath();
        // 父路径
        var parpath = Path.of(path).getParent();

        /* 检查父目录是否被监听 */
        if (parent.WathcPath.contains(parpath.toString()) && context instanceof Path)
            // 记录当前目录，父目录 -> 当前目录名
            parent.watchSeet.put(parpath.toString(), new File(path).getName());

        /* 检查目录是否被触发过 */
        if (kind == ENTRY_MODIFY && parent.watchSeet.containsKey(path))
            // 检查子目录
            return !parent.watchSeet.remove(path).equals(context.toString());
        return true;
    }

    @Override
    protected
    LoopState runcall(WatchKey key, WatchEvent<?> event, Stream<StateBack> stream) {
        var state = new AtomicReference<>(LoopState.WATCH_NEXT);
        // 运行处理事件
        stream.anyMatch(v -> {
            var st = v.apply(event, getPath());
            state.set(st);
            return st == WATCH_DOME || st == WATCH_CLOSE;
        });
        return state.get();
    }

    // get Path
    //----------------------------------------------------------------------------------------------

    @Override
    public @NotNull
    String toPath() { return path; }

    // Close
    //----------------------------------------------------------------------------------------------

    @Override
    protected
    void close0() {
        // close Watch
        KEY.cancel();
        // 移除自身
        parent.removeLoop(KEY);
    }
}
