package fybug.nulll.pdfw.funciton;
import org.jetbrains.annotations.NotNull;

import java.nio.file.WatchEvent;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

import fybug.nulll.pdfw.LoopState;

import static fybug.nulll.pdfw.LoopState.WATCH_NEXT;

/**
 * <h2>异步处理接口.</h2>
 * 使用线程池进行处理
 * 自动返回 {@link LoopState#WATCH_NEXT}.
 *
 * @author fybug
 * @version 0.0.1
 * @see BiConsumer
 * @see StateBack
 * @see ExecutorService
 * @since funciton 0.0.1
 */
public final
class AsnyState implements StateBack {
    // 全局默认线程池
    private final static ExecutorService DEFA_POLP = Executors.newCachedThreadPool();
    // 回调
    private final BiConsumer<WatchEvent<?>, String>[] fun;
    // 线程池
    private final ExecutorService POLP;

    private
    AsnyState(BiConsumer<WatchEvent<?>, String>[] consumers, ExecutorService po) {
        fun = consumers;
        POLP = po;
    }

    /*--------------------------------------------------------------------------------------------*/

    @Override
    public
    LoopState apply(WatchEvent<?> event, String path) {
        POLP.submit(() -> Arrays.stream(fun).forEach(v -> v.accept(event, path)));
        return WATCH_NEXT;
    }

    /*--------------------------------------------------------------------------------------------*/

    /** 创建并传入回调链 */
    @SafeVarargs
    @NotNull
    public static
    AsnyState asnystate(@NotNull BiConsumer<WatchEvent<?>, String>... consumers)
    { return asnystate(DEFA_POLP, consumers); }

    /** 创建并传入回调链和池 */
    @SafeVarargs
    @NotNull
    public static
    AsnyState asnystate(@NotNull ExecutorService po,
                        @NotNull BiConsumer<WatchEvent<?>, String>... consumers)
    { return new AsnyState(consumers.clone(), po); }
}
