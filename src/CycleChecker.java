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

    public void checkCycle(ArrayList<int[]> cycles) {

	int cycleRet[] = new int[period + 2];
	
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
		    
		    cycleRet[period] = i;
		    cycleRet[0] = window[i];
		
		    for(i++; (i < cycleRet[period] + period) &&
			    (i < window.length - period); i++) {
			
			cycleRet[i - cycleRet[period]] = window[i];
		    
			if(window[i] != window[i + period]) {
			    cycleRet[period] = -1;
			    offset = i;
			    break;
			}
		    }
		
		    if(cycleRet[period] != -1) {
		    
			while(i < window.length - period &&
			      window[i] == window[i + period]) i++;

			cycleRet[period + 1] = i + period - 1;

			offset = i;
			
			if(i == window.length - period) {
			    cycleContinue = true;
			    cycleRet[period + 1] = -1;
			}

			cycles.add(cycleRet);

			cycleRet = new int[period + 2];

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
