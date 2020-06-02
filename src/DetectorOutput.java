import java.io.*;

public interface DetectorOutput {
    // Writes the data read
    public void writeData(String s);
    // Writes the cycles found
    public void writeCycle(String s);
    // Writes an error
    public void error(String s);
}
