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
    public final static File ROOT = new File("").getAbsoluteFile();

    public final static Path tmpD = new File(RunTest.ROOT, "tmp").toPath();
    public final static Path tmpF = new File(RunTest.ROOT, "tmp/tmp.a").toPath();
    public final static Path subD = new File(RunTest.ROOT, "tmp/sub").toPath();
    public final static Path subF = new File(RunTest.ROOT, "tmp/sub/tmp.w").toPath();
    public final static Path subF2 = new File(RunTest.ROOT, "tmp/sub/tmp.as").toPath();

    public static StringWriter out;
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
