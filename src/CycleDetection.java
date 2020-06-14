import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CycleDetection {
    private int maxPeriod; // Maximum period checked
    private DataWindow window; // Window of time currently being analysed
    private int currentTime;
    private Cycle currentCycle; // Cycle being tracked that may continue
    private DataReader input; // Responsible for getting the data
    private DetectorOutput output; // Responsible for writing the output
    private ArrayList<CycleChecker> checkers; // Responsible for checking a specific period
    private ArrayList<Cycle> allCycles; // Valid cycles found
    private ConcurrentLinkedQueue<Cycle> cyclesFound; // All cycles found on the current window
    private long lastID;

    // Constructor. Initialises the checkers and variables
    public CycleDetection(int maxPeriod, DataReader input, DetectorOutput output) {
	this.input = input;
	this.output = output;
	this.maxPeriod = maxPeriod;
	window = new DataWindow(2*maxPeriod);
	currentTime = -1;
	currentCycle = null;
	lastID = 0;

	cyclesFound = new ConcurrentLinkedQueue<Cycle>();
	allCycles = new ArrayList<Cycle>();

	checkers = new ArrayList<CycleChecker>();
	for(int i = 1; i <= maxPeriod; i++) {
	    CycleChecker cc = new CycleChecker(i, window, cyclesFound);
	    checkers.add(cc);
	}
    }

    // Prints all valid cycles that cannot be replaced anymore
    private void printResults() {
	
	ArrayList<Cycle> cycleRemove = new ArrayList<Cycle>();
	for(Cycle cycleRet : allCycles) {
	    if(currentCycle != null && cycleRet.getStart() >= currentCycle.getStart()) {
		cycleRemove.add(cycleRet);
	    }
	    else {
		if(cycleRet.getStart() < currentTime - window.getSize()) {

		    cycleRemove.add(cycleRet);

		    output.writeCycle("Cycle Found");
		    output.writeCycle("Cycle ID : " + cycleRet.id);
		    output.writeCycle("Cycle of period " + cycleRet.getPeriod());
		    output.writeCycle("Time span : " + cycleRet.getStart() + " to " +
				      cycleRet.getEnd());
		    output.writeCycle("Repeated " +
				      (cycleRet.getEnd() - cycleRet.getStart()) / cycleRet.getPeriod()
				      + " times");
		    output.writeCycle("Values: " + Arrays.toString(cycleRet.getCycle()));
		    output.writeCycle("");
		}
	    }
	}
	for(Cycle cycleTemp : cycleRemove) {
	    allCycles.remove(cycleTemp);
	}
    }

    
    private boolean periodCheck() {

	// Starts the threads that check each cycle
	ArrayList<Thread> threads = new ArrayList<Thread>();
	checkers.forEach(cc -> {
		
		// Force them to check if there is a current cycle with bigger period
		if(currentCycle != null)
		    cc.setForced();
		
		Thread t = new Thread(cc);
		threads.add(t); t.start();
	    });

	for(Thread t : threads) {
	    try{ t.join(); } catch(Exception e){}
	}

	// If there is any cycle found
	if(cyclesFound.size() > 0) {

	    boolean print = false;

	    Cycle cycleRet;

	    // Chcecks the validity of each cycle found
	    while((cycleRet = cyclesFound.poll()) != null) {

		// If a tracked cycle has ended
		if(cycleRet.getStart() == -1) {
		    // If the cycle that ended is the current tracked cycle
		    if(cycleRet.getPeriod() == currentCycle.getPeriod()) {
			print = true;
		    }
		}

		// If the cycle found is a new cycle
		else {
		
		    cycleRet.setStart(currentTime - window.getSize() + cycleRet.getStart());
		    // If the cycle has ended inside the window
		    if(cycleRet.getEnd() != -1) {
			
			cycleRet.setEnd(currentTime - window.getSize() + cycleRet.getEnd());
			
			boolean flag = false;

			// If there is a current tracked cycle
			if(currentCycle != null) {
			    
			    if(currentCycle.getStart() <= cycleRet.getStart())
				flag = true;

			    // If this cycle might be the true form of the tracked one
			    if(currentCycle.getStart() == cycleRet.getStart() &&
			       currentCycle.getPeriod() > cycleRet.getPeriod() &&
			       cycleRet.getEnd() == currentTime - 2){
				
				if(print) {
				    cycleRet.id = currentCycle.id;
				    currentCycle = cycleRet;
				}
				else {
				    for(Cycle cycleTemp : cyclesFound) {
					if(cycleTemp.getStart() == -1 &&
					   cycleTemp.getPeriod() == currentCycle.getPeriod()) {
					    cycleRet.id = currentCycle.id;
					    currentCycle = cycleRet;
					    print = true;
					    break;
					}
				    }
				}
			    }
			}

			// Checks if there is an identical or conflicting cycle registered
			for(Cycle cycleTemp : allCycles) {

			    // If both cycles happen at the same time.
			    if(cycleTemp.getStart() == cycleRet.getStart() &&
			       cycleTemp.getEnd() == cycleRet.getEnd()) {

				// Picks the one with the smallest period
				if(cycleTemp.getPeriod() > cycleRet.getPeriod()) {
				    if (cycleTemp.id != -1) cycleRet.id = cycleTemp.id;
				    allCycles.remove(cycleTemp);
				    break;
				}

				else {
				    flag = true;
				    break;
				}
			    }

			    // If the cycle previously stored can be replaced
			    if(cycleTemp.getStart() >= cycleRet.getStart() &&
			       cycleTemp.getEnd() <= cycleRet.getEnd()) {
				
				if (cycleTemp.id != -1) cycleRet.id = cycleTemp.id;
				allCycles.remove(cycleTemp);
				break;
			    }

			    // If this cycle is invalid
			    if(cycleTemp.getStart() <= cycleRet.getStart() &&
			       cycleTemp.getEnd() >= cycleRet.getEnd()) {
				flag = true;
				break;
			    }
			}
			
			if(!flag){
			    allCycles.add(cycleRet);
			    if (cycleRet.id == -1) {
				cycleRet.id = lastID;
				lastID++;
			    }
			}
		    }

		    // If the cycle may continue
		    else {

			// If there isn't a current tracked cycle or it can be replaced
			if((currentCycle == null ||
			    cycleRet.getStart() < currentCycle.getStart()) ||
			   (cycleRet.getStart() == currentCycle.getStart() &&
			    cycleRet.getPeriod() < currentCycle.getPeriod())) {

			    if (currentCycle != null) cycleRet.id = currentCycle.id;
			    else {
				cycleRet.id = lastID;
				lastID++;
			    }
			    currentCycle = cycleRet;
			    print = false;

			    output.writeCycle("Cycle Found");
			    output.writeCycle("Cycle ID : " + currentCycle.id);
			    output.writeCycle("Values : " + Arrays.toString(currentCycle.getCycle()));
			    output.writeCycle("Started at " + currentCycle.getStart());
			    output.writeCycle("");
			}
		    }
		}
	    }

	    printResults();
	    
	    // If the current tracked cycle has ended and cannot be replaced
	    if(print) {
		
		output.writeCycle("Cycle Ended");
		output.writeCycle("Cycle ID : " + currentCycle.id);
		output.writeCycle("Cycle of period " + currentCycle.getPeriod());
		output.writeCycle("Time span : " + currentCycle.getStart() + " to " +
				  (currentTime - 2));
		output.writeCycle("Repeated " +
				  ((currentTime - 2) - currentCycle.getStart()) / currentCycle.getPeriod()
				  + " times");
			    
		output.writeCycle("Values: " + Arrays.toString(currentCycle.getCycle()));
		output.writeCycle("");
		
		currentCycle = null;

		checkers.forEach(cc -> cc.resetOffset());

		window.reset();
		return true;
	    }
	}

	return false;
    }

    private void finalCheck(int length) {

    }

    // Method for starting the cycle detection
    public void detectCycles() {
	boolean started = false;
	int count = 0;
	try {
	    // Fills the frame with data
	    for(currentTime = 0; currentTime < window.getSize(); currentTime++) {
		int data = input.getData();
                window.addValue(data);
		output.writeData(currentTime + ": " + data);
		count++;
	    }

	    // Checks for the first time
	    periodCheck();

	    boolean reset = false;
	    count = 0;

	    started = true;

	    // While no DataEndException was received
	    while(true) {
		int data = input.getData();
		output.writeData(currentTime + ": " + data);

		currentTime++;

		// If an ongoing cycle has ended,
		// the window is filled completely again
		if(reset) {
		    count++;
		    started = false;
		    window.addValue(data);
		    
		    if(count == window.getSize() - 1) {
			reset = periodCheck();
			count = 0;
			started = true;
		    }
		}

		// Continues the usual check
		else {
                    window.addValue(data);

		    reset = periodCheck();
		}
	    }
	}catch(DataEndException e) {}

	// If the frame was not filled entirely
        if(!started && count > 0) {            
	    window.setSize(count + 2);
	    periodCheck();
	}

	// Prints the remaining cycles stored
	for(Cycle cycleRet : allCycles) {
	    output.writeCycle("Cycle Found");
	    output.writeCycle("Cycle ID : " + cycleRet.id);
	    output.writeCycle("Cycle of period " + cycleRet.getPeriod());
	    output.writeCycle("Time span : " + cycleRet.getStart() + " to " +
			      cycleRet.getEnd());
	    output.writeCycle("Repeated " +
			      (cycleRet.getEnd() - cycleRet.getStart()) / cycleRet.getPeriod()
			      + " times");
	    output.writeCycle("Values: " + Arrays.toString(cycleRet.getCycle()));
	    output.writeCycle("");
	}

	// If there was an ongoing cycle, prints it
	if(currentCycle != null) {
	    output.writeCycle("Cycle Ended");
	    output.writeCycle("Cycle ID : " + currentCycle.id);
	    output.writeCycle("Cycle of period " + currentCycle.getPeriod());
	    output.writeCycle("Time span : " + currentCycle.getStart() + " to " +
			      (currentTime - 1));
	    output.writeCycle("Repeated " +
			      ((currentTime - 1) - currentCycle.getStart()) / currentCycle.getPeriod()
			      + " times");
			    
	    output.writeCycle("Values: " + Arrays.toString(currentCycle.getCycle()));
	}
    }
    
    public static void main(String args[]) {
	
	if(args.length < 2) {
	    System.out.println("Please, enter the maximum period and input mode");
	}
	
	int maxPeriod = Integer.parseInt(args[0]);
	DataReader input = null;
	DetectorOutput output = new TerminalOutput();
	
	if(args[1].equals("0")) {
	    try {
		input = new DataFileReader(args[2]);
	    }catch(IOException e){
		System.out.println("Files not found");
		return;
	    }
	}

	if(args[1].equals("1")) {
	    try {
		input = new DataSimReader(args[2]);
	    }catch(IOException e) {
		System.out.println("File not found");
		return;
	    }
	}

	if(args[1].equals("2")) {
	    try {
		input = new DataFileReader(args[2]);
	    }catch(IOException e) {
		System.out.println("Files not found");
		return;
	    }

	    try {
		output = new FileOutput(args[3]);
	    }catch(IOException e) {
		System.out.println("Could not open file");
		return;
	    }
	}

	if(args[1].equals("3")) {
	    try {
		input = new DataSimReader(args[2]);
	    }catch(IOException e) {
		System.out.println("File not found");
		return;
	    }
	    
	    try {
		output = new FileOutput(args[3]);
	    }catch(IOException e) {
		System.out.println("Could not open file");
		return;
	    }
	}
	
	CycleDetection cd = new CycleDetection(maxPeriod, input, output);
	cd.detectCycles();
    }
}
