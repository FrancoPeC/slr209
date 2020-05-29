import java.io.*;

public class FileOutput implements DetectorOutput{
    private BufferedWriter file;

    public FileOutput(String fileName) throws IOException{
	file = new BufferedWriter(new FileWriter(fileName));
    }

    public void writeData(String s) {
	System.out.println(s);
    }
    
    public void writeCycle(String s) {
	try {
	    file.write(s);
	    file.newLine();
	    file.flush();
	}catch(Exception e) {
	    error("Could not write on file");
	}
    }
    
    public void error(String s) {
	System.out.println(s);
    }
}
