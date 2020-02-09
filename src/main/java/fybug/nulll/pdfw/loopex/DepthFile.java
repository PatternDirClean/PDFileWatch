package fybug.nulll.pdfw.loopex;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;

import fybug.nulll.pdfw.DepthLoop;
import fybug.nulll.pdfw.DepthWatch;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * <h2>监控文件.</h2>
 * 该处理程序用于监听目录下文件的变化
 *
 * @author fybug
 * @version 0.0.1
 * @see DepthLoop
 * @since loopex 0.0.1
 */
public
class DepthFile extends DepthLoop {
    public
    DepthFile(DepthWatch ws, Path rootpath) { super(ws, rootpath); }

    //----------------------------------------------------------------------------------------------

    @Override
    protected
    boolean runCheck(WatchKey key, WatchEvent<?> event, WatchEvent.Kind<?> kind) {
        if (!super.runCheck(key, event, kind))
            return false;
        if (kind == ENTRY_CREATE || kind == ENTRY_MODIFY) {
            // 当前路径
            var paths = Path.of(keyToPath(key), event.context().toString());
            // 不符合规则
            return paths.toFile().isFile();
        }
        return true;
    }
}
