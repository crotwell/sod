package edu.sc.seis.sod.util.display;

/*
 * Sometimes you just want something to hold a min value and a max value...
 */
public class IntRange {

    
    public IntRange(int min, int max){
        setRange(min, max);
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMin() {
        return min;
    }
    
    public int getDifference(){
        return max - min;
    }
    
    public int getSum(){
        return max + min;
    }
    
    public double getMean(){
        return (double)getSum()/2d;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public void setRange(int min, int max){
        this.min = min;
        this.max = max;
    }
    
    public String toString() {
        return "from " + min + " to " + max;
    }
    
    public boolean equals(Object o){
        if (o instanceof IntRange){
            IntRange tmp = (IntRange)o;
            return tmp.getMin() == min && tmp.getMax() == max;
        }
        return false;
    }

    private int min;

    private int max;
}
