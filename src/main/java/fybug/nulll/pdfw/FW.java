package fybug.nulll.pdfw;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <h2>文件监控启动工具.</h2>
 * 使用该类可以创建监控服务
 *
 * @author fybug
 * @version 0.0.1
 * @since PDFileWatch 0.0.1
 */
public
class FW {
    /**
     * 创建文件监控服务
     *
     * @param executorService 使用的线程池
     *
     * @return 服务
     *
     * @see SendWatch
     */
    @NotNull
    public static
    SendWatch sendWatch(@NotNull ExecutorService executorService) throws IOException
    { return new SendWatch(executorService); }

    /**
     * 创建文件监控服务
     * 使用单线程线程池
     *
     * @return 服务
     *
     * @see SendWatch
     */
    @NotNull
    public static
    SendWatch sendWatch() throws IOException
    { return new SendWatch(Executors.newSingleThreadExecutor());}

    //--------------------------------------

    /**
     * 创建文件监控服务
     *
     * @param executorService 使用的线程池
     *
     * @return 服务
     *
     * @see DepthWatch
     */
    public static
    DepthWatch depthWatch(@NotNull ExecutorService executorService) throws IOException
    {return new DepthWatch(executorService);}

    /**
     * 创建文件监控服务
     * 使用单线程线程池
     *
     * @return 服务
     *
     * @see DepthWatch
     */
    public static
    DepthWatch depthWatch() throws IOException
    {return new DepthWatch(Executors.newSingleThreadExecutor());}
}
