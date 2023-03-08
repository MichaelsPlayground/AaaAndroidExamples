package de.androidcrypto.aaaandroidexamples.json;

public class JsonModel {
    private String entry1;
    private String entry2;
    private String arrayName;
    private ArrayModel[] files;

    public JsonModel(String entry1, String entry2, String arrayName, ArrayModel[] array) {
        this.entry1 = entry1;
        this.entry2 = entry2;
        this.arrayName = arrayName;
        this.files = array;
    }

    public String getEntry1() {
        return entry1;
    }

    public void setEntry1(String entry1) {
        this.entry1 = entry1;
    }

    public String getEntry2() {
        return entry2;
    }

    public void setEntry2(String entry2) {
        this.entry2 = entry2;
    }

    public String getArrayName() {
        return arrayName;
    }

    public void setArrayName(String arrayName) {
        this.arrayName = arrayName;
    }

    public ArrayModel[] getArray() {
        return files;
    }

    public void setArray(ArrayModel[] array) {
        this.files = array;
    }
}
