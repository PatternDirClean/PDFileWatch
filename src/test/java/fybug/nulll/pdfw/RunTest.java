package fybug.nulll.pdfw;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.File;
import java.io.StringWriter;
import java.nio.file.Path;

import fybug.nulll.pdfw.watch.watchTest;

@RunWith( Suite.class )
@Suite.SuiteClasses( {watchTest.class} )
public
class RunTest {
    // 测试根路径
    public final static File ROOT = new File("").getAbsoluteFile();

    // 临时目录1
    public final static Path tmpD = new File(RunTest.ROOT, "tmp").toPath();
    // 临时文件1
    public final static Path tmpF = new File(RunTest.ROOT, "tmp/tmp.a").toPath();
    // 临时目录2
    public final static Path subD = new File(RunTest.ROOT, "tmp/sub").toPath();
    // 临时文件2
    public final static Path subF = new File(RunTest.ROOT, "tmp/sub/tmp.w").toPath();
    // 临时文件3
    public final static Path subF2 = new File(RunTest.ROOT, "tmp/sub/tmp.as").toPath();

    // 测试输出
    public static StringWriter out;
    // 对比用正确的输出
    public static StringWriter outMark;

    public static
    void writeMark(String s) {
        outMark.write(s);
        try {
            Thread.sleep(100);
        } catch ( InterruptedException ignored ) {
        }
    }
}
