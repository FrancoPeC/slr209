import java.io.*;
import java.util.*;

public class DataFileReader implements DataReader{
    private LinkedList<String> files;
    private BufferedReader currentFile;
    
    
    public DataFileReader(String mainFolder) throws IOException, InterruptedException{
	files = new LinkedList<String>();
	ProcessBuilder pb = new ProcessBuilder("ls", mainFolder);
	Process pc = pb.start();

	BufferedReader pcReader =
	    new BufferedReader(new InputStreamReader(pc.getInputStream()));

	 String line;
	 ArrayList<String> folders = new ArrayList<String>();
	 while((line = pcReader.readLine()) != null) {
	     folders.add(mainFolder + "/" + line);
	 }

	 pc.waitFor();

	 for(String fd : folders) {
	     pb = new ProcessBuilder("ls", fd);
	     pc = pb.start();

	     pcReader = new BufferedReader(new InputStreamReader(pc.getInputStream()));

	     while((line = pcReader.readLine()) != null) {
		 files.push(fd + "/" + line);
	     }

	     pc.waitFor();
	 }

	 currentFile = new BufferedReader(new FileReader(files.pop()));
	 currentFile.readLine();
    }

    
    public int getData() throws DataEndException {
	String line;
	try {
	    if((line = currentFile.readLine()) == null) {
		currentFile.close();
		currentFile = new BufferedReader(new FileReader(files.pop()));
		currentFile.readLine();
		line = currentFile.readLine();
	    }
	}catch(Exception e){throw new DataEndException();}
	return Integer.parseInt(line.split("-")[line.split("-").length - 1].split(",")[0]);
    }
    
}
