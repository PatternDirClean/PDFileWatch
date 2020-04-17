package fybug.nulll.pdfw.watch;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.stream.Stream;

import fybug.nulll.pdfw.Loop;
import fybug.nulll.pdfw.StateBack;
import fybug.nulll.pdfw.loopex.LoopState;

import static fybug.nulll.pdfw.loopex.LoopState.WATCH_CLOSE;
import static fybug.nulll.pdfw.loopex.LoopState.WATCH_DOME;

/**
 * <h2>{@link SendWatch} 对接处理程序.</h2>
 * 可记录每次触发的目录的父目录<br/>
 * 在子目录已处理的情况下不传递给父目录
 * <br/><br/>
 * 如需解除监听，请直接调用该对象的 {@link #close()}
 *
 * @author fybug
 * @version 0.0.1
 * @since PDFileWatch 0.0.1
 */ // todo build
public
class SendLoop extends Loop<SendWatch, SendLoop> {
    // 当前注册的 key
    private final WatchKey KEY;
    // 当前路径
    private final String path;

    //----------------------------------------------------------------------------------------------

    public
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

        // 检查父目录是否被监听
        return parent.checkParent(getPath(), Path.of(path).getParent(), event.context(), kind);
    }

    @Override
    protected
    LoopState runcall(WatchKey key, WatchEvent<?> event, Stream<StateBack> stream) {
        var state = new LoopState[]{LoopState.WATCH_NEXT};
        // 运行处理事件
        stream.anyMatch(v -> {
            var st = v.apply(event, getPath());
            state[0] = st;
            return st == WATCH_DOME || st == WATCH_CLOSE;
        });
        return state[0];
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public @NotNull
    String toPath() { return path; }

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
