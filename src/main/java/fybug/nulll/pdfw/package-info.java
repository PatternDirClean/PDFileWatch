/**
 * <h2>PDFileWatch 文件监控.</h2>
 * <p>
 * 作用于 {@link java.nio.file.WatchService} 的文件监控库
 * 使用 回调链以及 {@link fybug.nulll.pdfw.loopex.LoopState} 进行数据过滤和处理
 * 使用 {@link fybug.nulll.pdfw.PDFileWatch} -> {@link fybug.nulll.pdfw.WaServer} -> {@link fybug.nulll.pdfw.Loop}
 * 的结构，使用 {@link fybug.nulll.pdfw.loopex.LoopState} 表明当前的处理状态
 *
 * @author fybug
 * @version 0.0.1
 * @since JDK 13
 * todo icon
 * todo readme.md
 * todo wiki
 */
package fybug.nulll.pdfw;