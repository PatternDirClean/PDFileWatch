package fybug.nulll.pdfw;
import java.nio.file.WatchEvent;
import java.util.function.BiFunction;

import fybug.nulll.pdfw.funciton.AsnyState;
import fybug.nulll.pdfw.funciton.NextState;
import fybug.nulll.pdfw.loopex.LoopState;

/**
 * <h2>{@link Loop} 处理回调.</h2>
 * 可返回 {@link LoopState} 作为状态的回调<br/>
 * 可使用 {@link NextState} 默认声明为 {@link LoopState#WATCH_NEXT}<br/>
 * 可使用 {@link AsnyState} 采取异步运行回调，默认声明为 {@link LoopState#WATCH_NEXT}
 *
 * @author fybug
 * @version 0.0.1
 * @see LoopState
 * @since PDFileWatch 0.0.1
 */
public
interface StateBack extends BiFunction<WatchEvent<?>, String, LoopState> {}
