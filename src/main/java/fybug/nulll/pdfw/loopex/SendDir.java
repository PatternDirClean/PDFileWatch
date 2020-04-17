package fybug.nulll.pdfw.loopex;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;

import fybug.nulll.pdfw.watch.SendLoop;
import fybug.nulll.pdfw.watch.SendWatch;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * <h2>监控文件夹.</h2>
 * 该处理程序用于监听当前目录下文件夹的变化
 *
 * @author fybug
 * @version 0.0.1
 * @see SendLoop
 * @since loopex 0.0.1
 */
public
class SendDir extends SendLoop {
    public
    SendDir(WatchKey key, SendWatch ws, Path path) { super(key, ws, path); }

    //----------------------------------------------------------------------------------------------

    @Override
    protected
    boolean runCheck(WatchKey key, WatchEvent<?> event, WatchEvent.Kind<?> kind) {
        if (!super.runCheck(key, event, kind))
            return false;
        if (kind == ENTRY_CREATE || kind == ENTRY_MODIFY) {
            // 当前路径
            var paths = Path.of(getPath(), event.context().toString());
            // 不符合规则
            return Files.isDirectory(paths);
        }
        return true;
    }
}
