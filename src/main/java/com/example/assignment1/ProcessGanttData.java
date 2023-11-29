package com.example.assignment1;

public class ProcessGanttData {
    private String processName;
    private int processTime;

    public ProcessGanttData(String processName, int processTime) {
        this.processName = processName;
        this.processTime = processTime;
    }

    public String getProcessName() {
        return processName;
    }

    public int getProcessTime() {
        return processTime;
    }

    public void setProcessTime(int processTime) {
        this.processTime = processTime;
    }

    @Override
    public String toString() {
        return "processName= " + processName + ", processTime= " + processTime;

    }
}
