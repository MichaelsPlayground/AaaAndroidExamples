package de.androidcrypto.aaaandroidexamples.json;

public class ArrayModel {

    private String entryName;
    private String entryValue;

    public ArrayModel(String entryName, String entryValue) {
        this.entryName = entryName;
        this.entryValue = entryValue;
    }

    public String getEntryName() {
        return entryName;
    }

    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }

    public String getEntryValue() {
        return entryValue;
    }

    public void setEntryValue(String entryValue) {
        this.entryValue = entryValue;
    }
}
