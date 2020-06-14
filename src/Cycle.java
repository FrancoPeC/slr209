
// Class representing a cycle
public class Cycle {
    private int cycle[]; // Cycle pattern
    private int startTime; // The time it started
    private int endTime; // The time it ended
    private int position; // Next position in the pattern to be updated
    public  long id; // Unique id of the cycle.

    // Constructor.
    public Cycle(int period) {
	cycle = new int[period];
	startTime = -1;
	endTime = -1;
	position = 0;
	id = -1;
    }

    public void setStart(int startTime) {
	this.startTime = startTime;
    }

    public int getStart() {
	return startTime;
    }

    public void setEnd(int endTime) {
	this.endTime = endTime;
    }

    public int getEnd() {
	return endTime;
    }

    public int getPeriod() {
	return cycle.length;
    }

    // Adds a value to the cycle pattern on the last updated position
    public boolean addValue(int value) {
	if(position < cycle.length) {
	    cycle[position] = value;
	    position++;
	    return true;
	}
	else return false;
    }

    // Sets the cycle pattern
    public void setCycle(int cycle[]) {
	if(cycle != null) {
	    this.cycle = new int[cycle.length];
	    System.arraycopy(cycle, 0, this.cycle, 0, cycle.length);
	    position = cycle.length;
	}
	else cycle = null;
    }

    // Gets the cycle pattern
    public int[] getCycle() {
	if(cycle != null) {
	    int cycleRet[] = new int[cycle.length];
	    System.arraycopy(cycle, 0, cycleRet, 0, cycle.length);
	    return cycleRet;
	}
	else return null;
    }
}
