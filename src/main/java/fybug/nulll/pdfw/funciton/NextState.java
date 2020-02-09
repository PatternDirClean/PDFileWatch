package fybug.nulll.pdfw.funciton;
import org.jetbrains.annotations.NotNull;

import java.nio.file.WatchEvent;
import java.util.Arrays;
import java.util.function.BiConsumer;

import fybug.nulll.pdfw.LoopState;

/**
 * <h2>预处理接口.</h2>
 * 自动返回 {@link LoopState#WATCH_NEXT}
 *
 * @author fybug
 * @version 0.0.1
 * @see BiConsumer
 * @see StateBack
 * @since funciton 0.0.1
 */
public final
class NextState implements StateBack {
    // 回调
    private final BiConsumer<WatchEvent<?>, String>[] consumer;

    private
    NextState(BiConsumer<WatchEvent<?>, String>[] consumer) { this.consumer = consumer; }

    /*--------------------------------------------------------------------------------------------*/

    @Override
    public
    LoopState apply(WatchEvent<?> event, String path) {
        Arrays.stream(consumer).forEach(v -> v.accept(event, path));
        return LoopState.WATCH_NEXT;
    }

    /*--------------------------------------------------------------------------------------------*/

    /** 创建并传入回调链 */
    @SafeVarargs
    @NotNull
    public static
    NextState nextstate(@NotNull BiConsumer<WatchEvent<?>, String>... consumer)
    {return new NextState(consumer.clone());}
}
