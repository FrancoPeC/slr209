import java.io.*;

// Interface for feeding data for the cycle detector
public interface DataReader {
    // Throws an exception if no more data can be read
    public int getData() throws DataEndException;
}
