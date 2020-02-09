package fybug.nulll.pdfw.funciton;
import java.nio.file.WatchEvent;
import java.util.function.BiFunction;

import fybug.nulll.pdfw.SendLoop;
import fybug.nulll.pdfw.LoopState;

/**
 * <h2>返回状态的回调.</h2>
 *
 * @author fybug
 * @version 0.0.1
 * @see SendLoop#addCall(WatchEvent.Kind, StateBack...)
 * @see LoopState
 * @since funciton 0.0.1
 */
public
interface StateBack extends BiFunction<WatchEvent<?>, String, LoopState> {}
