import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;

import fybug.nulll.pdfw.FW;
import fybug.nulll.pdfw.SendWatch;

import static fybug.nulll.pdfw.funciton.NextState.nextstate;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public
class Watchto {
    private SendWatch sendWatch;

    @Before
    public
    void befo() throws IOException {
        sendWatch = FW.sendWatch();
        Files.createDirectories(RunTest.tmpD);
        RunTest.out = new StringWriter();
        RunTest.outMark = new StringWriter();
    }

    @After
    public
    void donw() throws IOException {
        sendWatch.close();
        Files.deleteIfExists(RunTest.tmpD);
        // 校验
        Assert.assertEquals(RunTest.out.toString(), RunTest.outMark.toString());
    }

    /*--------------------------------------------------------------------------------------------*/

    @Test
    public
    void watchDir() throws IOException {
        sendWatch.watchDir(RunTest.tmpD, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
                 .addCall(ENTRY_CREATE, nextstate((even, path) -> RunTest.out.write(
                        "C:" + even.context().toString() + ",F:tmpD")))
                 .addCall(ENTRY_MODIFY, nextstate((even, path) -> RunTest.out.write(
                        "M:" + even.context().toString() + ",F:tmpD")))
                 .addCall(ENTRY_DELETE, nextstate((even, path) -> RunTest.out.write(
                        "D:" + even.context().toString() + ",F:tmpD")));

        Files.createDirectories(RunTest.subD);
        RunTest.writeMark("C:sub,F:tmpD");

        sendWatch.watchDir(RunTest.subD, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
                 .addCall(ENTRY_CREATE, nextstate((even, path) -> RunTest.out.write(
                        "C:" + even.context().toString() + ",F:subD")))
                 .addCall(ENTRY_MODIFY, nextstate((even, path) -> RunTest.out.write(
                        "M:" + even.context().toString() + ",F:subD")))
                 .addCall(ENTRY_DELETE, nextstate((even, path) -> RunTest.out.write(
                        "D:" + even.context().toString() + ",F:subD")));

        Files.createFile(RunTest.tmpF);
        RunTest.writeMark("C:tmp.a,F:tmpD");

        Files.createFile(RunTest.subF);
        RunTest.writeMark("C:tmp.w,F:subD");
        Files.createFile(RunTest.subF2);
        RunTest.writeMark("C:tmp.as,F:subD");

        /*----------------------------*/

        Files.deleteIfExists(RunTest.tmpF);
        RunTest.writeMark("D:tmp.a,F:tmpD");

        Files.deleteIfExists(RunTest.subF);
        RunTest.writeMark("D:tmp.w,F:subD");
        Files.deleteIfExists(RunTest.subF2);
        RunTest.writeMark("D:tmp.as,F:subD");

        Files.deleteIfExists(RunTest.subD);
        RunTest.writeMark("D:sub,F:tmpD");
    }

    @Test
    public
    void watchFile() throws IOException {
        sendWatch.watchFil(RunTest.tmpF, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
                 .addCall(ENTRY_CREATE, nextstate((even, path) -> RunTest.out.write(
                        "C:" + even.context().toString() + ",F:tmpF")))
                 .addCall(ENTRY_MODIFY, nextstate((even, path) -> RunTest.out.write(
                        "M:" + even.context().toString() + ",F:tmpF")))
                 .addCall(ENTRY_DELETE, nextstate((even, path) -> RunTest.out.write(
                        "D:" + even.context().toString() + ",F:tmpF")));

        Files.createDirectories(RunTest.subD);

        Files.createFile(RunTest.tmpF);
        RunTest.writeMark("C:tmp.a,F:tmpF");

        Files.createFile(RunTest.subF);

        Files.writeString(RunTest.tmpF, "a");
        RunTest.writeMark("M:tmp.a,F:tmpF");

        /*----------------------------*/

        Files.deleteIfExists(RunTest.tmpF);
        RunTest.writeMark("D:tmp.a,F:tmpF");

        Files.deleteIfExists(RunTest.subF);
        Files.deleteIfExists(RunTest.subD);
    }
}
