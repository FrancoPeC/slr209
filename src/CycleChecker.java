import java.io.*;
import java.util.*;

public class CycleChecker {
    int period;
    int window[];
    int offset;
    boolean cycleContinue;

    public CycleChecker(int period, int window[]) {
	this.period = period;
	this.window = window;
	offset = 0;
	cycleContinue = false;
    }

    public void checkCycle(ArrayList<Cycle> cycles) {

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

	if(offset == 0) {
	    
	    for(int i = 0; i <= window.length - 2*period; i++) {
		
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

			cycleRet = new Cycle(period);

		    }
		}
	    }
	}
    }

    public void resetOffset() {
	this.offset = 0;
	this.cycleContinue = false;
    }
}
