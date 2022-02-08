package org.cs305.assignment1.classes;

public class SakilaTest3 {
    public short[] idArray;
    public byte[] idArray2;
    public long[] idArray3;
    public float[] rateStart;
    public double[] rateEnd;
    public SakilaTest3(short[] idArray, byte[] idArray2, long[] idArray3, float[] rateStart, double[] rateEnd) {
        this.idArray = idArray;
        this.idArray2 = idArray2;
        this.idArray3 = idArray3;
        this.rateStart = rateStart;
        this.rateEnd = rateEnd;
    }
    public short[] getIdArray() {
        return idArray;
    }
    public void setIdArray(short[] idArray) {
        this.idArray = idArray;
    }
    public float[] getRateStart() {
        return rateStart;
    }
    public void setRateStart(float[] rateStart) {
        this.rateStart = rateStart;
    }
    public double[] getRateEnd() {
        return rateEnd;
    }
    public void setRateEnd(double[] rateEnd) {
        this.rateEnd = rateEnd;
    }
    public byte[] getIdArray2() {
        return idArray2;
    }
    public void setIdArray2(byte[] idArray2) {
        this.idArray2 = idArray2;
    }
    public long[] getIdArray3() {
        return idArray3;
    }
    public void setIdArray3(long[] idArray3) {
        this.idArray3 = idArray3;
    }
}
