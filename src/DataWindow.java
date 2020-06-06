
// Class representing a frame of data
public class DataWindow {
    private int window[]; // Array containing data
    private int offset; // Where is the start of data

    // Constructor.
    public DataWindow(int size) {
	window = new int[size];
	offset = 0;
    }

    // Stores the next data value
    public void addValue(int data) {
	window[offset] = data;
	
	if (offset == window.length - 1) offset = 0;
	else offset++;
    }

    public int get(int index) {
	return window[(offset + index) % window.length];
    }

    public int getSize() {
	return window.length;
    }

    public void setSize(int size) {
	int w[] = new int[size];
	
        for (int i = 0; i < size; i++) {
	    w[i] = window[(offset + i) % window.length];
	}

	offset = 0;
	window = w;
    }

    public void reset() {
	window[0] = get(getSize() - 1);
	offset = 1;
    }
}
