public class Cycle {
    private int position;
    private int cycle[];
    private int startTime;
    private int endTime;

    public Cycle(int period) {
	cycle = new int[period];
	startTime = -1;
	endTime = -1;
	position = 0;
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

    public boolean addValue(int value) {
	if(position < cycle.length) {
	    cycle[position] = value;
	    position++;
	    return true;
	}
	else return false;
    }

    public void setCycle(int cycle[]) {
	if(cycle != null) {
	    this.cycle = new int[cycle.length];
	    System.arraycopy(cycle, 0, this.cycle, 0, cycle.length);
	    position = cycle.length;
	}
	else cycle = null;
    }

    public int[] getCycle() {
	if(cycle != null) {
	    int cycleRet[] = new int[cycle.length];
	    System.arraycopy(cycle, 0, cycleRet, 0, cycle.length);
	    return cycleRet;
	}
	else return null;
    }
}
