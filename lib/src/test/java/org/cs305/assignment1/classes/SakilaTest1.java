package org.cs305.assignment1.classes;

public class SakilaTest1 {
    public String rating;
    public float priceStart;
    public double priceEnd;
    public short durationStart;
    public int durationEnd;
    public SakilaTest1(String rating, float priceStart, double priceEnd, short durationStart, int durationEnd) {
        this.rating = rating;
        this.priceStart = priceStart;
        this.priceEnd = priceEnd;
        this.durationStart = durationStart;
        this.durationEnd = durationEnd;
    }
    public String getRating() {
        return rating;
    }
    public void setRating(String rating) {
        this.rating = rating;
    }
    public float getPriceStart() {
        return priceStart;
    }
    public void setPriceStart(float priceStart) {
        this.priceStart = priceStart;
    }
    public double getPriceEnd() {
        return priceEnd;
    }
    public void setPriceEnd(double priceEnd) {
        this.priceEnd = priceEnd;
    }
    public short getDurationStart() {
        return durationStart;
    }
    public void setDurationStart(short durationStart) {
        this.durationStart = durationStart;
    }
    public int getDurationEnd() {
        return durationEnd;
    }
    public void setDurationEnd(int durationEnd) {
        this.durationEnd = durationEnd;
    }
}
