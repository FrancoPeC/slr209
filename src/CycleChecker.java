import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CycleChecker implements Runnable{
    private int period; // Cycle period
    private int window[]; // Window of time to be read
    private int offset; // Starting position not yet analysed
    private boolean cycleContinue; // If the cycle may continue in the next window
    private boolean forced; // Force the analysis
    private ConcurrentLinkedQueue<Cycle> cycles; // Where to save the found cycles

    // Constructor
    public CycleChecker(int period, int window[], ConcurrentLinkedQueue<Cycle> cycles) {
	this.period = period;
	this.window = window;
	this.cycles = cycles;
	offset = 0;
	forced = false;
	cycleContinue = false;
    }

    // Checks the window for cycles of the specified period
    public void checkCycle() {

	Cycle cycleRet = new Cycle(period);

	// If the last cycle found may continue
	if(cycleContinue) {

	    // If the cycle has ended, add an empty cycle to the shared object
	    if(window[window.length - 1] != window[window.length - period - 1]) {
		offset--;
		cycleContinue = false;
		cycles.add(cycleRet);
	    }

	    return;
	}

	// Changes offset as window has moved
	if(offset > 0)
	    offset--;

	if(offset == 0 || forced) {
	    
	    for(int i = offset; i <= window.length - 2*period; i++) {

		// If a cycle of this period may exist
		if(window[i] == window[i + period]) {
		    
		    cycleRet.setStart(i);
		    cycleRet.addValue(window[i]);

		    // Checks if a full cycle has happened, saving it
		    for(i++; (i < cycleRet.getStart() + period) &&
			    (i < window.length - period); i++) {
			
			cycleRet.addValue(window[i]);
		    
			if(window[i] != window[i + period]) {
			    cycleRet.setStart(-1);
			    offset = i;
			    break;
			}
		    }

		    // If a full cycle happened
		    if(cycleRet.getStart() != -1) {

			// Checks where it ends
			while(i < window.length - period &&
			      window[i] == window[i + period]) i++;

			cycleRet.setEnd(i + period - 1);

			offset = i;

			// If the cycle may continue to the next window
			if(i == window.length - period) {
			    cycleContinue = true;
			    cycleRet.setEnd(-1);
			}

			cycles.add(cycleRet);
		    }

		    cycleRet = new Cycle(period);
		}
	    }
	}

	forced = false;
    }
    
    public void resetOffset() {
	offset = 0;
	cycleContinue = false;
    }
    
    public void setForced() {
	forced = true;
    }

    public int getPeriod() {
	return period;
    }

    @Override
    public void run() {
	checkCycle();
    }
    
}
