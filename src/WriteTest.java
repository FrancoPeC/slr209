import java.io.*;
import java.util.*;

public class WriteTest {
    private BufferedWriter fw;

    public WriteTest(String file) {
	try {
	    fw = new BufferedWriter(new FileWriter(file));
	}catch(Exception e) {
	    System.out.println("Writer couldn't open file");
	}
    }

    public void write(String s) {
	try {
	    fw.write(s);
	    fw.newLine();
	    fw.flush();
	}catch(Exception e) {
	    System.out.println("Writer couldn't write on file");
	}
    }

    public static void main(String args[]) {
	WriteTest wt = new WriteTest("test.txt");
	for(int i = 0; i < 40; i++) {
	    long startTime = System.currentTimeMillis();
	    while(System.currentTimeMillis() - startTime < 1000) {}
	    wt.write((i % 10) + ",");
	}

	wt.write("finish");
    }
}
