package fybug.nulll.pdfw.loopex;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;

import fybug.nulll.pdfw.SendLoop;
import fybug.nulll.pdfw.SendWatch;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * <h2>文件监控.</h2>
 * 该处理程序用于监听一个指定的文件
 *
 * @author fybug
 * @version 0.0.1
 * @see SendLoop
 * @since loopex 0.0.1
 */
public
class FileSend extends SendLoop {
    // 当前文件的名称
    private final String filename;

    //----------------------------------------------------------------------------------------------

    public
    FileSend(WatchKey key, SendWatch ws, Path path) {
        super(key, ws, path);
        filename = path.toFile().getName();
    }

    //----------------------------------------------------------------------------------------------

    @Override
    protected
    boolean runCheck(WatchKey key, WatchEvent<?> event, WatchEvent.Kind<?> kind) {
        var files = event.context().toString();

        /* 检查事件 */
        if (kind == ENTRY_CREATE || kind == ENTRY_MODIFY) {
            // 检查是否为文件
            if (!files.equals(filename) || !new File(getPath(), filename).isFile())
                return false;
        } else if (!files.equals(filename))
            return false;

        return super.runCheck(key, event, kind);
    }

    //----------------------------------------------------------------------------------------------

    @Override
    protected
    String getPath() { return Path.of(super.getPath()).getParent().toString(); }
}
