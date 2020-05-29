import java.io.*;
import java.util.*;

public class TerminalOutput implements DetectorOutput{
    public TerminalOutput() {}

    public void writeData(String s) {
	System.out.println(s);
    }

    public void writeCycle(String s) {
	System.out.println(s);
    }

    public void error(String s) {
	System.out.println(s);
    }
}
