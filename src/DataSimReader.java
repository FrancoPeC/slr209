import java.util.*;
import java.io.*;

public class DataSimReader implements DataReader {
    private BufferedReader file;
    
    public DataSimReader(String fileName) throws IOException {
	file = new BufferedReader(new FileReader(fileName));
	file.readLine();
    }

    public int getData() throws DataEndException {
	try {   
	    String line;
	    long startTime = System.currentTimeMillis();
	    
	    while((line = file.readLine()) == null) {
		if(System.currentTimeMillis() - startTime > 30000)
		    throw new DataEndException();
	    }
	    
	    if(line.equals("finish")) throw new DataEndException();
	    
	    return Integer.parseInt(line.split("-")[line.split("-").length - 1].split(",")[0]);
	    }catch(IOException e) {throw new DataEndException();}
    }
}
