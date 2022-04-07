package fybug.nulll.pdfw.watch;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;

import fybug.nulll.pdfw.PDFileWatch;
import fybug.nulll.pdfw.RunTest;
import fybug.nulll.pdfw.WaServer;

import static fybug.nulll.pdfw.funciton.NextState.nextstate;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
public
class SendWatchTest {
    private SendWatch sendWatch;

    @Before
    public
    void setUp() throws Exception {
        // 启动监控服务
        sendWatch = PDFileWatch.sendWatch().build();
        // 初始化目录和流
        Files.createDirectories(RunTest.tmpD);
        RunTest.out = new StringWriter();
        RunTest.outMark = new StringWriter();
    }

    @After
    public
    void tearDown() throws Exception {
        // 关闭服务
        sendWatch.close();
        // 删除目录
        Files.deleteIfExists(RunTest.tmpD);
        // 校验
        Assert.assertEquals(RunTest.out.toString(), RunTest.outMark.toString());
    }

    @Test
    public
    void watchDir() throws IOException {
        // 注册一个目录监控，只监控 tmpD 目录
        sendWatch.watchDir(RunTest.tmpD, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
                 // 创建时输出
                 .addCall(ENTRY_CREATE, nextstate((even, path) -> RunTest.out.write(
                         "C:" + even.context().toString() + ",F:tmpD\n")))
                 // 修改时输出
                 .addCall(ENTRY_MODIFY, nextstate((even, path) -> RunTest.out.write(
                         "M:" + even.context().toString() + ",F:tmpD\n")))
                 // 删除时输出
                 .addCall(ENTRY_DELETE, nextstate((even, path) -> RunTest.out.write(
                         "D:" + even.context().toString() + ",F:tmpD\n")));

        // 创建 subD 目录，其父目录包含 tmpD
        Files.createDirectories(RunTest.subD);
        // 正确输出：subD 创建事件触发
        RunTest.writeMark("C:sub,F:tmpD\n");

        // 注册一个目录监控，只监控 subD 目录
        sendWatch.watchDir(RunTest.subD, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
                 // 创建时输出
                 .addCall(ENTRY_CREATE, nextstate((even, path) -> RunTest.out.write(
                         "C:" + even.context().toString() + ",F:subD\n")))
                 // 修改时输出
                 .addCall(ENTRY_MODIFY, nextstate((even, path) -> RunTest.out.write(
                         "M:" + even.context().toString() + ",F:subD\n")))
                 // 删除时输出
                 .addCall(ENTRY_DELETE, nextstate((even, path) -> RunTest.out.write(
                         "D:" + even.context().toString() + ",F:subD\n")));

        // 创建 tmpF 文件
        Files.createFile(RunTest.tmpF);
        // 正确输出：tmpD 创建事件触发
        RunTest.writeMark("C:tmp.a,F:tmpD\n");

        // 创建 subF 文件
        Files.createFile(RunTest.subF);
        // 正确输出：subD 创建事件触发
        RunTest.writeMark("C:tmp.w,F:subD\n");
        // 创建 subF2 文件
        Files.createFile(RunTest.subF2);
        // 正确输出：subD 创建事件触发
        RunTest.writeMark("C:tmp.as,F:subD\n");

        /*----------------------------*/

        // 删除 tmpF 文件
        Files.deleteIfExists(RunTest.tmpF);
        // 正确输出：tmpD 删除事件触发
        RunTest.writeMark("D:tmp.a,F:tmpD\n");

        // 删除 subF 文件
        Files.deleteIfExists(RunTest.subF);
        // 正确输出：subD 删除事件触发
        RunTest.writeMark("D:tmp.w,F:subD\n");
        // 删除 subF2 文件
        Files.deleteIfExists(RunTest.subF2);
        // 正确输出：subD 删除事件触发
        RunTest.writeMark("D:tmp.as,F:subD\n");

        // 删除 subD 目录
        Files.deleteIfExists(RunTest.subD);
        // 正确输出：tmpD 删除事件触发
        RunTest.writeMark("D:sub,F:tmpD\n");
    }

    @Test
    public
    void watchFil() throws IOException {
        sendWatch.watchFil(RunTest.tmpF, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
                 .addCall(ENTRY_CREATE, nextstate((even, path) -> RunTest.out.write(
                         "C:" + even.context().toString() + ",F:tmpF\n")))
                 .addCall(ENTRY_MODIFY, nextstate((even, path) -> RunTest.out.write(
                         "M:" + even.context().toString() + ",F:tmpF\n")))
                 .addCall(ENTRY_DELETE, nextstate((even, path) -> RunTest.out.write(
                         "D:" + even.context().toString() + ",F:tmpF\n")));

        Files.createDirectories(RunTest.subD);

        Files.createFile(RunTest.tmpF);
        RunTest.writeMark("C:tmp.a,F:tmpF\n");

        Files.createFile(RunTest.subF);

        Files.writeString(RunTest.tmpF, "a", WRITE);
        RunTest.writeMark("M:tmp.a,F:tmpF\n");

        /*----------------------------*/

        Files.deleteIfExists(RunTest.tmpF);
        RunTest.writeMark("D:tmp.a,F:tmpF\n");

        Files.deleteIfExists(RunTest.subF);
        Files.deleteIfExists(RunTest.subD);
    }

    @Test
    public
    void checkDir() throws IOException {
        sendWatch.checkDir(RunTest.tmpD, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
                 .addCall(ENTRY_CREATE, nextstate((even, path) -> RunTest.out.write(
                         "C:" + even.context().toString() + ",F:tmpD\n")))
                 .addCall(ENTRY_MODIFY, nextstate((even, path) -> RunTest.out.write(
                         "M:" + even.context().toString() + ",F:tmpD\n")))
                 .addCall(ENTRY_DELETE, nextstate((even, path) -> RunTest.out.write(
                         "D:" + even.context().toString() + ",F:tmpD\n")));

        Files.createDirectories(RunTest.subD);
        RunTest.writeMark("C:sub,F:tmpD\n");

        sendWatch.checkDir(RunTest.subD, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
                 .addCall(ENTRY_CREATE, nextstate((even, path) -> RunTest.out.write(
                         "C:" + even.context().toString() + ",F:subD\n")))
                 .addCall(ENTRY_MODIFY, nextstate((even, path) -> RunTest.out.write(
                         "M:" + even.context().toString() + ",F:subD\n")))
                 .addCall(ENTRY_DELETE, nextstate((even, path) -> RunTest.out.write(
                         "D:" + even.context().toString() + ",F:subD\n")));

        RunTest.writeMark("");
        Files.createFile(RunTest.tmpF);
        Files.createFile(RunTest.subF);
        Files.createFile(RunTest.subF2);

        /*----------------------------*/

        Files.deleteIfExists(RunTest.tmpF);
        RunTest.writeMark("D:tmp.a,F:tmpD\n");

        Files.deleteIfExists(RunTest.subF);
        RunTest.writeMark("D:tmp.w,F:subD\n");
        Files.deleteIfExists(RunTest.subF2);
        RunTest.writeMark("D:tmp.as,F:subD\n");

        Files.deleteIfExists(RunTest.subD);
        RunTest.writeMark("D:sub,F:tmpD\n");
    }

    @Test
    public
    void checkFil() throws IOException {
        sendWatch.checkFil(RunTest.tmpD, WaServer.KINDS_ALL)
                 .addCall(ENTRY_CREATE, nextstate((even, path) -> RunTest.out.write(
                         "C:" + even.context().toString() + ",F:tmpF\n")))
                 .addCall(ENTRY_MODIFY, nextstate((even, path) -> RunTest.out.write(
                         "M:" + even.context().toString() + ",F:tmpF\n")))
                 .addCall(ENTRY_DELETE, nextstate((even, path) -> RunTest.out.write(
                         "D:" + even.context().toString() + ",F:tmpF\n")));

        Files.createDirectories(RunTest.subD);

        Files.createFile(RunTest.tmpF);
        RunTest.writeMark("C:tmp.a,F:tmpF\n");

        Files.createFile(RunTest.subF);

        Files.writeString(RunTest.tmpF, "a", WRITE);
        RunTest.writeMark("M:tmp.a,F:tmpF\n");

        /*----------------------------*/

        Files.deleteIfExists(RunTest.tmpF);
        RunTest.writeMark("D:tmp.a,F:tmpF\n");

        Files.deleteIfExists(RunTest.subF);
        Files.deleteIfExists(RunTest.subD);
        RunTest.writeMark("D:sub,F:tmpF\n");
    }
}