package fybug.nulll.pdfw.loopex;
import java.nio.file.WatchEvent;

/**
 * <h2>回调状态.</h2>
 * 使用该枚举指定当前回调链的状态
 *
 * @author fybug
 * @version 0.0.1
 * @since PDFileWatch 0.0.1
 */
public
enum LoopState {
    /** 继续处理 */
    WATCH_NEXT,
    /** 中断处理，中断调用链 */
    WATCH_DOME,
    /** 对象关闭，中断调用链以及本次 {@link WatchEvent} 处理 */
    WATCH_CLOSE
}
