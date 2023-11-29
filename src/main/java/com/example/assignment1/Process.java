package com.example.assignment1;

import javafx.scene.control.Button;

public class Process implements Comparable<Process>, Cloneable {
    private String processName;
    private int burstTime;
    private  int arrivalTime;
    private int priority;
    private int remainingBurst;
    private int processCount;
    private int finishTime;
    private int initArrivalTime;
    private static int totalTime;

    private final Button btnDelete;
    public Process(String processName, int burstTime, int arrivalTime, int priority) {
        this.processName = processName;
        this.burstTime = burstTime;
        this.arrivalTime = arrivalTime;
        this.priority = priority;
        remainingBurst = burstTime;
        finishTime=0;
        initArrivalTime = arrivalTime;
        totalTime += burstTime;
        processCount = 0;
        btnDelete = new Button("Delete");
    }

    public Button getBtnDelete() {
        return btnDelete;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public void setBurstTime(int burstTime) {
        this.burstTime = burstTime;
    }

    public int getArrivalTime() {

        if (arrivalTime == 0) return initArrivalTime;
            else return arrivalTime;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setRemainingBurst(int remainingBurst) {
        this.remainingBurst = remainingBurst;
        processCount ++;
    }

    public int getRemainingBurst() {
        return remainingBurst;
    }

    public int getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(int finishTime) {
        this.finishTime = finishTime;
    }
    public static int getTotalTime() {
        return totalTime;
    }

    public static void setTotalTime(int totalTime) {
        Process.totalTime = totalTime;
    }

    public void setProcessCount(int processCount) {
        this.processCount = processCount;
    }

    public int getProcessCount() {
        return processCount;
    }

    public int getTurnAroundTime(){
        return this.finishTime-getArrivalTime();
    }

    public  int getWaitingTime(){
        return  getTurnAroundTime()-this.burstTime;
    }

    @Override
    public int compareTo(Process p) {
       if(this.arrivalTime != p.arrivalTime ) return this.arrivalTime - p.arrivalTime;
       else{
          if(this.priority != p.priority) return this.priority - p.priority;
          else return this.processCount - p.processCount;
       }
    }

    @Override
    public Process clone() {
        try {
            return (Process) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}