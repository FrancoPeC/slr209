import java.io.*;
import java.util.*;

public class CycleDetection {
    private int maxPeriod;
    private int window[];
    private int currentTime;
    private Cycle cycle;
    private DataReader input;
    private DetectorOutput output;
    private ArrayList<CycleChecker> checkers;
    private ArrayList<Cycle> allCycles;

    public CycleDetection(int maxPeriod, DataReader input, DetectorOutput output) {
	this.input = input;
	this.output = output;
	this.maxPeriod = maxPeriod;
	window = new int[2*maxPeriod];
	currentTime = -1;
	cycle = null;

	allCycles = new ArrayList<Cycle>();

	checkers = new ArrayList<CycleChecker>();
	for(int i = 1; i <= maxPeriod; i++) {
	    CycleChecker cc = new CycleChecker(i, window);
	    checkers.add(cc);
	}
    }

    private void printResults() {
	
	ArrayList<Cycle> cycleRemove = new ArrayList<Cycle>();
	for(Cycle cycleRet : allCycles) {
	    if(cycle != null && cycleRet.getStart() >= cycle.getStart()) {
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

	// If there is an ongoing cycle
	if(cycle != null) {
	    
	    printResults();
	    
	    ArrayList<Cycle> cycles = new ArrayList<Cycle>();
	    checkers.get(cycle.getPeriod() - 1).checkCycle(cycles);

	    for(int i = 0; i < window.length - 1; i++)
		window[i] = window[i + 1];

	    
	    if(cycles.size() > 0) {
		output.write("Cycle of period " + cycle.getPeriod() +
			     " from " + cycle.getStart() + " to " +
			     (currentTime - 2));
			    
		output.write("Cycle: " + Arrays.toString(cycle.getCycle()));
		
		cycle = null;
		for(CycleChecker cc : checkers) {
		    cc.resetOffset();
		}

		window[0] = window[window.length - 1];
		return true;
	    }

	    return false;
	}

	for(int period = maxPeriod; period > 0; period--) {
	    CycleChecker cc = checkers.get(period - 1);
	    ArrayList<Cycle> cycles = new ArrayList<Cycle>();
	    
	    cc.checkCycle(cycles);
	    
	    if(cycles.size() > 0) {

		for(int i = 0; i < cycles.size(); i++) {
		    Cycle cycleRet = cycles.get(i);
		    cycleRet.setStart(currentTime - window.length + cycleRet.getStart());

		    if(cycleRet.getEnd() != -1) {
			
			cycleRet.setEnd(currentTime - window.length + cycleRet.getEnd());
			
			boolean flag = false;
			
			for(Cycle cycleTemp : allCycles) {

			    if(cycle != null && cycle.getStart() <= cycleRet.getStart()) {
				flag = true;
				break;
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
			
			if(cycle == null || cycleRet.getStart() <= cycle.getStart() ) {
			    cycle = new Cycle(period);
			    cycle.setCycle(cycleRet.getCycle());

			    cycle.setStart(cycleRet.getStart());
			}
		    }
		}
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
	
	if(cycle != null) {
	    output.write("Cycle of period " + cycle.getPeriod() + " from " +
			 cycle.getStart() + " to " + (currentTime - 1));
			    
	    output.write("Cycle: " + Arrays.toString(cycle.getCycle()));
	}
    }
    
    public static void main(String args[]) {
	
	if(args.length < 2) {
	    System.out.println("Please, enter the maximum period and input mode");
	}
	
	int maxPeriod = Integer.parseInt(args[0]);
	DataReader input = null;
	
	if(args[1].equals("0")) {
	    try {
		input = new DataFileReader(args[2]);
	    }catch(IOException e){
		System.out.println("File not found");
		return;
	    }catch(InterruptedException e) {
		System.out.println("Process interrupted");
		return;
	    }
	}
	
	CycleDetection cd = new CycleDetection(maxPeriod, input, new TerminalOutput());
	cd.detectCycles();
    }
}
