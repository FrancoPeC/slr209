import java.io.*;
import java.util.*;

public class TerminalOutput implements DetectorOutput{
    public TerminalOutput() {}

    public void write(String s) {
	System.out.println(s);
    }

    public void error(String s) {
	System.out.println(s);
    }
}
