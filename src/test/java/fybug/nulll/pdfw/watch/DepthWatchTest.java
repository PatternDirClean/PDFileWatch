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

import static fybug.nulll.pdfw.funciton.NextState.nextstate;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
public
class DepthWatchTest {
    private DepthWatch watch;

    @Before
    public
    void setUp() throws Exception {
        watch = PDFileWatch.depthWatch().build();
        Files.createDirectories(RunTest.tmpD);
        RunTest.out = new StringWriter();
        RunTest.outMark = new StringWriter();
    }

    @After
    public
    void tearDown() throws Exception {
        watch.close();
        Files.deleteIfExists(RunTest.tmpD);
        System.out.println("out: " + RunTest.out.toString());
        System.out.println("outMark: " + RunTest.outMark.toString());
        System.out.println();
        System.out.println();
        // 校验
        Assert.assertEquals(RunTest.out.toString(), RunTest.outMark.toString());
    }

    @Test
    public
    void watchDir() throws IOException {
        watch.watchDir(RunTest.tmpD)
             .addCall(ENTRY_CREATE, nextstate((even, path) -> RunTest.out.write(
                     "C:" + even.context().toString() + ",F:tmpD\n")))
             .addCall(ENTRY_MODIFY, nextstate((even, path) -> RunTest.out.write(
                     "M:" + even.context().toString() + ",F:tmpD\n")))
             .addCall(ENTRY_DELETE, nextstate((even, path) -> RunTest.out.write(
                     "D:" + even.context().toString() + ",F:tmpD\n")));

        Files.createDirectories(RunTest.subD);
        RunTest.writeMark("C:sub,F:tmpD\n");

        watch.watchDir(RunTest.subD)
             .addCall(ENTRY_CREATE, nextstate((even, path) -> RunTest.out.write(
                     "C:" + even.context().toString() + ",F:subD\n")))
             .addCall(ENTRY_MODIFY, nextstate((even, path) -> RunTest.out.write(
                     "M:" + even.context().toString() + ",F:subD\n")))
             .addCall(ENTRY_DELETE, nextstate((even, path) -> RunTest.out.write(
                     "D:" + even.context().toString() + ",F:subD\n")));

        Files.createFile(RunTest.tmpF);
        RunTest.writeMark("C:tmp.a,F:tmpD\n");

        Files.createFile(RunTest.subF);
        RunTest.writeMark("C:tmp.w,F:subD\n");
        RunTest.writeMark("M:sub,F:tmpD\n");
        Files.createFile(RunTest.subF2);
        RunTest.writeMark("C:tmp.as,F:subD\n");
        RunTest.writeMark("M:sub,F:tmpD\n");

        /*----------------------------*/

        Files.deleteIfExists(RunTest.tmpF);
        RunTest.writeMark("D:tmp.a,F:tmpD\n");

        Files.deleteIfExists(RunTest.subF);
        RunTest.writeMark("D:tmp.w,F:subD\n");
        Files.deleteIfExists(RunTest.subF2);
        RunTest.writeMark("D:tmp.as,F:subD\n");
        RunTest.writeMark("M:sub,F:tmpD\n");

        Files.deleteIfExists(RunTest.subD);
        RunTest.writeMark("D:sub,F:tmpD\n");
    }

    @Test
    public
    void checkDir() throws IOException {
        watch.checkDir(RunTest.tmpD)
             .addCall(ENTRY_CREATE, nextstate((even, path) -> RunTest.out.write(
                     "C:" + even.context().toString() + ",F:tmpD\n")))
             .addCall(ENTRY_MODIFY, nextstate((even, path) -> RunTest.out.write(
                     "M:" + even.context().toString() + ",F:tmpD\n")))
             .addCall(ENTRY_DELETE, nextstate((even, path) -> RunTest.out.write(
                     "D:" + even.context().toString() + ",F:tmpD\n")));

        Files.createDirectories(RunTest.subD);
        RunTest.writeMark("C:sub,F:tmpD\n");

        watch.watchDir(RunTest.subD)
             .addCall(ENTRY_CREATE, nextstate((even, path) -> RunTest.out.write(
                     "C:" + even.context().toString() + ",F:subD\n")))
             .addCall(ENTRY_MODIFY, nextstate((even, path) -> RunTest.out.write(
                     "M:" + even.context().toString() + ",F:subD\n")))
             .addCall(ENTRY_DELETE, nextstate((even, path) -> RunTest.out.write(
                     "D:" + even.context().toString() + ",F:subD\n")));

        Files.createFile(RunTest.subF);
        RunTest.writeMark("C:tmp.w,F:subD\n");
        RunTest.writeMark("M:sub,F:tmpD\n");
        Files.createFile(RunTest.subF2);
        RunTest.writeMark("C:tmp.as,F:subD\n");
        RunTest.writeMark("M:sub,F:tmpD\n");

        /*----------------------------*/

        Files.deleteIfExists(RunTest.tmpF);

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
        watch.checkFil(RunTest.tmpD)
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