import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CycleChecker implements Runnable{
    private int period;
    private int window[];
    private int offset;
    private boolean cycleContinue;
    private boolean forced;
    private ConcurrentLinkedQueue<Cycle> cycles;

    public CycleChecker(int period, int window[], ConcurrentLinkedQueue<Cycle> cycles) {
	this.period = period;
	this.window = window;
	this.cycles = cycles;
	offset = 0;
	forced = false;
	cycleContinue = false;
    }
    
    public void checkCycle() {

	Cycle cycleRet = new Cycle(period);
	
	if(cycleContinue) {
	    
	    if(window[window.length - 1] != window[window.length - period - 1]) {
		offset--;
		cycleContinue = false;
		cycles.add(cycleRet);
	    }

	    return;
	}
	
	if(offset > 0)
	    offset--;

	if(offset == 0 || forced) {
	    
	    for(int i = offset; i <= window.length - 2*period; i++) {
		
		if(window[i] == window[i + period]) {
		    
		    cycleRet.setStart(i);
		    cycleRet.addValue(window[i]);
		
		    for(i++; (i < cycleRet.getStart() + period) &&
			    (i < window.length - period); i++) {
			
			cycleRet.addValue(window[i]);
		    
			if(window[i] != window[i + period]) {
			    cycleRet.setStart(-1);
			    offset = i;
			    break;
			}
		    }
		
		    if(cycleRet.getStart() != -1) {
		    
			while(i < window.length - period &&
			      window[i] == window[i + period]) i++;

			cycleRet.setEnd(i + period - 1);

			offset = i;
			
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

    @Override
    public void run() {
	checkCycle();
    }
    
}
