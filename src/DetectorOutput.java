import java.io.*;

public interface DetectorOutput {
    public void writeData(String s);
    public void writeCycle(String s);
    public void error(String s);
}
