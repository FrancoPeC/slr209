import java.io.*;

public class FileOutput implements DetectorOutput{
    private BufferedWriter file;

    public FileOutput(String fileName) throws IOException{
	file = new BufferedWriter(new FileWriter(fileName));
    }
    public void write(String s) {
	try {
	    file.write(s);
	    file.newLine();
	}catch(Exception e) {
	    error("Could not write on file");
	}
    }
    public void error(String s) {
	System.out.println(s);
    }

    @Override
    public void finalize() {
	try {
	    file.flush();
	    file.close();
	}catch(Exception e){}
    }
}
