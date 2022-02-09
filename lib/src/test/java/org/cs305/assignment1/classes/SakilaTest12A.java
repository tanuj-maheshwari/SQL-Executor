package org.cs305.assignment1.classes;

public class SakilaTest12A {
    public String[] lastnameArray;
    public SakilaTest12A(String[] lastnameArray) {
        this.lastnameArray = lastnameArray;
    }
    public String[] getLastnameArray() {
        return lastnameArray;
    }
    public void setLastnameArray(String[] lastnameArray) {
        this.lastnameArray = lastnameArray;
    }
    @Override
    public String toString() {
        String returnString = new String("(");
        for(int i=0; i<lastnameArray.length; i++) {
            returnString = returnString + "\"" + lastnameArray[i] + "\"";
            if(i<lastnameArray.length-1) {
                returnString = returnString + ", ";
            }
        }
        returnString = returnString + ")";
        return returnString;
    }
}
