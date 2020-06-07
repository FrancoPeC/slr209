import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CycleChecker implements Runnable{
    private int period; // Cycle period
    private DataWindow window; // Window of time to be read
    private int offset; // Starting position not yet analysed
    private boolean cycleContinue; // If the cycle may continue in the next window
    private boolean forced; // Force the analysis
    private ConcurrentLinkedQueue<Cycle> cycles; // Where to save the found cycles

    // Constructor
    public CycleChecker(int period, DataWindow window, ConcurrentLinkedQueue<Cycle> cycles) {
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
	    if(window.get(window.getSize() - 1) != window.get(window.getSize() - period - 1)) {
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
	    
	    for(int i = offset; i <= window.getSize() - 2*period; i++) {

		// If a cycle of this period may exist
		if(window.get(i) == window.get(i + period)) {
		    
		    cycleRet.setStart(i);
		    cycleRet.addValue(window.get(i));

		    // Checks if a full cycle has happened, saving it
		    for(i++; (i < cycleRet.getStart() + period) &&
			    (i < window.getSize() - period); i++) {
			
			cycleRet.addValue(window.get(i));
		    
			if(window.get(i) != window.get(i + period)) {
			    cycleRet.setStart(-1);
			    offset = i;
			    break;
			}
		    }

		    // If a full cycle happened
		    if(cycleRet.getStart() != -1) {

			// Checks where it ends
			while(i < window.getSize() - period &&
			      window.get(i) == window.get(i + period)) i++;

			cycleRet.setEnd(i + period - 1);

			offset = i;

			// If the cycle may continue to the next window
			if(i == window.getSize() - period) {
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

    // Forces execution
    public void setForced() {
	forced = true;
    }

    public int getPeriod() {
	return period;
    }

    public void setWindow(DataWindow window) {
	this.window = window;
    }

    @Override
    public void run() {
	checkCycle();
    }
    
}
