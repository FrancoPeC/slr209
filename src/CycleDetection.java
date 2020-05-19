import java.io.*;
import java.util.*;

public class CycleDetection {
    private int maxPeriod;
    private int window[];
    private int lastValue;
    private int cycleStart;
    private int currentTime;
    private int cycle[];
    private DataReader input;
    private DetectorOutput output;
    private ArrayList<CycleChecker> checkers;
    private ArrayList<int []> allCycles;

    public CycleDetection(int maxPeriod, DataReader input, DetectorOutput output) {
	this.input = input;
	this.output = output;
	this.maxPeriod = maxPeriod;
	window = new int[2*maxPeriod];
	lastValue = -1;
	cycleStart = -1;
	currentTime = -1;
	cycle = null;

	allCycles = new ArrayList<int[]>();

	checkers = new ArrayList<CycleChecker>();
	for(int i = 1; i <= maxPeriod; i++) {
	    CycleChecker cc = new CycleChecker(i, window);
	    checkers.add(cc);
	}
    }

    // private void checkRepetition(int data) {
    // 	if(data == lastValue && cycleStart[0] == -1) {
    // 	    cycleStart[0] = currentTime;
    // 	}
		
    // 	else if(data != lastValue) {
    // 	    if(cycleStart[0] != -1) {
    // 		output.write("Value " + lastValue + " repeated from " +
    // 			     cycleStart[0] + " to " + currentTime);
			
    // 		cycleStart[0] = -1;
    // 		lastValue = data;
    // 	    }
		    
    // 	    else lastValue = data;
    // 	}
    // }

    // Returns true if the window should be reset

    private void printResults() {
	
	ArrayList<int[]> cycleRemove = new ArrayList<int[]>();
	for(int[] cycleRet : allCycles) {
	    if(cycleStart != -1 && cycleRet[cycleRet.length - 2] >= cycleStart) {
		cycleRemove.add(cycleRet);
	    }
	    else {
		if(cycleRet[cycleRet.length - 1] < currentTime - window.length) {

		    cycleRemove.add(cycleRet);
	    
		    int cycleTemp[] = new int[cycleRet.length - 2];
	
		    System.arraycopy(cycleRet, 0, cycleTemp, 0, cycleTemp.length);
	    

		    output.write("Cycle of period " + cycleTemp.length +
				 " from " + cycleRet[cycleRet.length - 2] + " to " +
				 cycleRet[cycleRet.length - 1]);
			    
		    output.write("Cycle: " + Arrays.toString(cycleTemp));
		}
	    }
	}
	for(int[] cycleTemp : cycleRemove) {
	    allCycles.remove(cycleTemp);
	}
    }

    
    private boolean periodCheck() {

	// If there is an ongoing cycle
	if(cycleStart != -1) {
	    
	    printResults();
	    
	    ArrayList<int[]> cycles = new ArrayList<int[]>();
	    checkers.get(cycle.length - 1).checkCycle(cycles);

	    for(int i = 0; i < window.length - 1; i++)
		window[i] = window[i + 1];

	    
	    if(cycles.size() > 0) {
		output.write("Cycle of period " + cycle.length +
			     " from " + cycleStart + " to " +
			     (currentTime - 2));
			    
		output.write("Cycle: " + Arrays.toString(cycle));
		
		cycleStart = -1;
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
	    ArrayList<int[]> cycles = new ArrayList<int[]>();
	    
	    cc.checkCycle(cycles);
	    
	    if(cycles.size() > 0) {

		for(int i = 0; i < cycles.size(); i++) {
		    int cycleRet[] = cycles.get(i);
		    cycleRet[period] = currentTime - window.length + cycleRet[period];

		    if(cycleRet[period + 1] != -1) {
			
			cycleRet[period + 1] = currentTime - window.length + cycleRet[period + 1];
			boolean flag = false;
			
			for(int[] cycleTemp : allCycles) {

			    if(cycleStart != -1 && cycleStart <= cycleRet[period]) {
				flag = true;
				break;
			    }
			    
			    if(cycleTemp[cycleTemp.length - 2] >= cycleRet[period] &&
			       cycleTemp[cycleTemp.length - 1] <= cycleRet[period + 1]) {
				allCycles.remove(cycleTemp);
				break;
			    }

			    if(cycleTemp[cycleTemp.length - 2] <= cycleRet[period] &&
			       cycleTemp[cycleTemp.length - 1] >= cycleRet[period + 1]) {
				flag = true;
				break;
			    }
			}
			
			if(!flag){
			    allCycles.add(cycleRet);
			}
		    }

		    else {
			
			if(cycleRet[period] <= cycleStart || cycleStart == -1) {
			    cycle = new int[period];
			    System.arraycopy(cycleRet, 0, cycle, 0, period);

			    cycleStart = cycleRet[period];
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

		//checkRepetition(data);
	    }
	    
	    periodCheck();

	    boolean reset = false;
	    int count = 1;
	    
	    while(true) {
		int data = input.getData();
		output.write(currentTime + ": " + data);
		
		//checkRepetition(data);

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

	if(cycleStart != -1) {
	    output.write("Cycle of period " + cycle.length + " from " +
			 cycleStart + " to " + (currentTime - 1));
			    
	    output.write("Cycle: " + Arrays.toString(cycle));
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
