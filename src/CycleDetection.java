import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CycleDetection {
    private int maxPeriod;
    private int window[];
    private int currentTime;
    private Cycle currentCycle ;
    private DataReader input;
    private DetectorOutput output;
    private ArrayList<CycleChecker> checkers;
    private ArrayList<Cycle> allCycles;
    private ConcurrentLinkedQueue<Cycle> cyclesFound;

    public CycleDetection(int maxPeriod, DataReader input, DetectorOutput output) {
	this.input = input;
	this.output = output;
	this.maxPeriod = maxPeriod;
	window = new int[2*maxPeriod];
	currentTime = -1;
	currentCycle = null;

	cyclesFound = new ConcurrentLinkedQueue<Cycle>();
	allCycles = new ArrayList<Cycle>();

	checkers = new ArrayList<CycleChecker>();
	for(int i = 1; i <= maxPeriod; i++) {
	    CycleChecker cc = new CycleChecker(i, window, cyclesFound);
	    checkers.add(cc);
	}
    }

    private void printResults() {
	
	ArrayList<Cycle> cycleRemove = new ArrayList<Cycle>();
	for(Cycle cycleRet : allCycles) {
	    if(currentCycle != null && cycleRet.getStart() >= currentCycle.getStart()) {
		cycleRemove.add(cycleRet);
	    }
	    else {
		if(cycleRet.getEnd() < currentTime - window.length) {

		    cycleRemove.add(cycleRet);

		    output.write("Cycle of period " + cycleRet.getPeriod() +
				 " from " + cycleRet.getStart() + " to " +
				 cycleRet.getEnd());
			    
		    output.write("Cycle: " + Arrays.toString(cycleRet.getCycle()));
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
		
		// Force them to check if there is a current cycle
		if(currentCycle != null) cc.setForced();
		
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

		// If a cycle has ended
		if(cycleRet.getStart() == -1) {
		    // If the cycle that ended is the current tracked cycle
		    if(cycleRet.getPeriod() == currentCycle.getPeriod()) {
			print = true;
		    }
		}

		// If the cycle found is a new cycle
		else {
		
		    cycleRet.setStart(currentTime - window.length + cycleRet.getStart());
		    // If the cycle has ended inside the window
		    if(cycleRet.getEnd() != -1) {
			
			cycleRet.setEnd(currentTime - window.length + cycleRet.getEnd());
			
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
				    currentCycle = cycleRet;
				}
				else {
				    for(Cycle cycleTemp : cyclesFound) {
					if(cycleTemp.getStart() == -1 &&
					   cycleTemp.getPeriod() == currentCycle.getPeriod()) {
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

			    if(cycleTemp.getStart() == cycleRet.getStart() &&
			       cycleTemp.getEnd() == cycleRet.getEnd()) {
				if(cycleTemp.getPeriod() > cycleRet.getPeriod()) {
				    allCycles.remove(cycleTemp);
				    break;
				}

				else {
				    flag = true;
				    break;
				}
			    }
			    
			    if(cycleTemp.getStart() >= cycleRet.getStart() &&
			       cycleTemp.getEnd() <= cycleRet.getEnd()) {
				allCycles.remove(cycleTemp);
				break;
			    }

			    if(cycleTemp.getStart() <= cycleRet.getStart() &&
			       cycleTemp.getEnd() >= cycleRet.getEnd()) {
				flag = true;
				break;
			    }
			}
			
			if(!flag){
			    allCycles.add(cycleRet);
			}
		    }

		    else {
			
			if((currentCycle == null ||
			    cycleRet.getStart() < currentCycle.getStart()) ||
			   (cycleRet.getStart() == currentCycle.getStart() &&
			    cycleRet.getPeriod() < currentCycle.getPeriod())) {
			
			    currentCycle = cycleRet;
			    print = false;
			}
		    }
		}
	    }

	    if(print) {
		
		printResults();

		for(int i = 0; i < window.length - 1; i++)
		    window[i] = window[i + 1];
		
		output.write("Cycle of period " + currentCycle.getPeriod() +
			     " from " + currentCycle.getStart() + " to " +
			     (currentTime - 2));
			    
		output.write("Cycle: " + Arrays.toString(currentCycle.getCycle()));
		
		currentCycle = null;

		checkers.forEach(cc -> cc.resetOffset());

		window[0] = window[window.length - 1];
		return true;
	    }
	}

	printResults();
	
	for(int i = 0; i < window.length - 1; i++)
	    window[i] = window[i + 1];

	return false;
    }    

    public void detectCycles() {
	try {
	    for(currentTime = 0; currentTime < window.length; currentTime++) {
		int data = input.getData();
		window[currentTime] = data;
		output.write(currentTime + ": " + data);
	    }
	    
	    periodCheck();

	    boolean reset = false;
	    int count = 1;
	    
	    while(true) {
		int data = input.getData();
		output.write(currentTime + ": " + data);

		currentTime++;

		if(reset) {
		    window[count] = data;
		    count++;
		    
		    if(count == window.length) {
			reset = periodCheck();
			count = 1;
		    }
		}
		
		else {
		    window[window.length - 1] = data;

		    reset = periodCheck();
		}
	    }
	}catch(DataEndException e) {}
	
	for(Cycle cycleRet : allCycles) {

	    output.write("Cycle of period " + cycleRet.getPeriod() +
			 " from " + cycleRet.getStart() + " to " +
			 cycleRet.getEnd());
			    
	    output.write("Cycle: " + Arrays.toString(cycleRet.getCycle()));
	}
	
	if(currentCycle != null) {
	    output.write("Cycle of period " + currentCycle.getPeriod() + " from " +
			 currentCycle.getStart() + " to " + (currentTime - 1));
			    
	    output.write("Cycle: " + Arrays.toString(currentCycle.getCycle()));
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
