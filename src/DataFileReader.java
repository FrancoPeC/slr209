import java.io.*;
import java.util.*;

public class DataFileReader implements DataReader{
    private LinkedList<File> files;
    private BufferedReader currentFile;
    
    
    public DataFileReader(String mainFolder) throws IOException {
	files = new LinkedList<File>();
	File dataPath = new File(mainFolder);
	for(File dir : dataPath.listFiles()) {
	    if(dir.isDirectory()) {
		for(File f : dir.listFiles()) {
		    if(f.isFile()) files.push(f);
		}
	    }
	    
	    else if(dir.isFile()) {
		files.push(dir);
	    }
	}

	File f = files.pop();
	currentFile = new BufferedReader(new FileReader(f));
	System.out.println("Reading file: " + f.getPath());
	currentFile.readLine();
    }

    
    public int getData() throws DataEndException {
	String line;
	try {
	    if((line = currentFile.readLine()) == null) {
		currentFile.close();
		File f = files.pop();
		currentFile = new BufferedReader(new FileReader(f));
		System.out.println("Reading file: " + f.getPath());
		currentFile.readLine();
		line = currentFile.readLine();
	    }
	}catch(Exception e){throw new DataEndException();}
	return Integer.parseInt(line.split("-")[line.split("-").length - 1].split(",")[0]);
    }
    
}
