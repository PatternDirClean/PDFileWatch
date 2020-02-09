package fybug.nulll.pdfw;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import fybug.nulll.pdfw.funciton.StateBack;

import static fybug.nulll.pdfw.LoopState.WATCH_CLOSE;
import static fybug.nulll.pdfw.LoopState.WATCH_DOME;
import static fybug.nulll.pdfw.LoopState.WATCH_NEXT;

// todo test

/**
 * <h2>DepthWatch 对接处理程序.</h2>
 * 可记录每次触发的目录的父目录
 * 新建目录自动监听
 * <p>
 * 如需解除监听，请直接调用该对象的 {@link #close()}
 *
 * @author fybug
 * @version 0.0.1
 * @since PDFileWatch 0.0.1
 */
public
class DepthLoop extends Loop<DepthWatch, DepthLoop> {
    // 和该处理程序关联的 key
    private final List<WatchKey> keyList = new CopyOnWriteArrayList<>();
    // 根目录
    final private String rootPath;

    //----------------------------------------------------------------------------------------------

    protected
    DepthLoop(DepthWatch ws, Path rootpath) {
        super(ws);
        this.rootPath = rootpath.toAbsolutePath().toString();
        // 新增自动监听
        addCall(StandardWatchEventKinds.ENTRY_CREATE, (event, pa) -> {
            var path = Path.of(pa, event.context().toString());
            // 文件就注册
            if (Files.isDirectory(path)) {
                try {
                    parent.forpath(this, path, WaServer.KINDS_ALL);
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
            }
            return WATCH_NEXT;
        });
        // 自动解除监听
        addCall(StandardWatchEventKinds.ENTRY_DELETE, (event, pa) -> {
            var path = Path.of(pa, event.context().toString());
            var key = parhToKey(path.toString());
            // 移除
            if (key != null) {
                keyList.remove(key);
                key.cancel();
                parent.removeLoop(key);
            }
            return WATCH_NEXT;
        });
    }

    //----------------------------------------------------------------------------------------------

    void binKey(WatchKey key) {keyList.add(key);}

    protected
    String keyToPath(WatchKey key) {return parent.pathmap.get(key);}

    protected
    WatchKey parhToKey(String path) {
        if (parent.keysmap.containsKey(path))
            return parent.keysmap.get(path);
        return null;
    }

    //----------------------------------------------------------------------------------------------

    @Override
    protected
    LoopState runcall(WatchKey key, WatchEvent<?> event, Stream<StateBack> stream) {
        var state = new AtomicReference<>(LoopState.WATCH_NEXT);
        // 运行处理事件
        stream.anyMatch(v -> {
            var st = v.apply(event, keyToPath(key));
            state.set(st);
            return st == WATCH_DOME || st == WATCH_CLOSE;
        });
        return state.get();
    }

    // get Path
    //----------------------------------------------------------------------------------------------

    @Override
    public @NotNull
    String toPath() { return rootPath; }

    // Close
    //----------------------------------------------------------------------------------------------

    @Override
    protected
    void close0() {
        keyList.forEach(v -> {
            // close Watch
            v.cancel();
            // 移除自身
            parent.removeLoop(v);
        });
        keyList.clear();
    }
}
