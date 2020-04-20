package fybug.nulll.pdfw;
import org.jetbrains.annotations.NotNull;

import fybug.nulll.pdfw.watch.DepthWatch;
import fybug.nulll.pdfw.watch.SendWatch;
import lombok.experimental.UtilityClass;

/**
 * <h2>主类.</h2>
 * 可以在这里找到所有的监控服务。<br/>
 * 提供所有监控服务的快速构造索引。
 *
 * @author fybug
 * @version 0.0.1
 * @since PDFileWatch 0.0.1
 */
@UtilityClass
public
class PDFileWatch {

    /**
     * 获取文件监控服务构造工具
     *
     * @see SendWatch
     */
    @NotNull
    public
    SendWatch.Build sendWatch() { return SendWatch.build();}

    //--------------------------------------

    /**
     * 获取深度文件监控服务构造工具
     *
     * @see DepthWatch
     */
    public
    DepthWatch.Build depthWatch() { return DepthWatch.build(); }
}
