package com.example.assignment1;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.*;
import java.util.function.UnaryOperator;

public class MainController {
    public TableView<Process> tblProcess;
    public TableColumn<Process, String> tbcProcess;
    public TableColumn<Process, Integer> tbcBurst;
    public TableColumn<Process, Number> tbcArrival;
    public TableColumn<Process, Integer> tbcPriority;
    public TableColumn<Process, Button> tbcDelete;
    public TextField txtProcess;
    public TextField txtBurstTime;
    public TextField txtArrivalTime;
    public TextField txtPriority;
    public Button btnAddProcess;
    public Button btnRun;
    public RadioButton rBtn01;
    public RadioButton rBtn02;
    public RadioButton rBtn03;
    public RadioButton rBtn04;
    public Pane pnGanttChart;
    public Pane pnResult;
    public ArrayList<Process> processList = new ArrayList<>();
    public ArrayList<ProcessGanttData> resultList = new ArrayList<>();
    public String log = "";

    public ArrayList<String> queueLog = new ArrayList<>();
    public Button btnClear;

    @FXML
    protected void initialize() {
        //Number only text box
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getText();

            if (text.matches("\\d?")) { // this is the important line
                return change;
            }

            return null;
        };
        txtBurstTime.setTextFormatter(new TextFormatter<>(filter));
        txtArrivalTime.setTextFormatter(new TextFormatter<>(filter));
        txtPriority.setTextFormatter(new TextFormatter<>(filter));

        //Radio Button
        ToggleGroup tgAlgo = new ToggleGroup();
        rBtn01.setToggleGroup(tgAlgo);
        rBtn02.setToggleGroup(tgAlgo);
        rBtn03.setToggleGroup(tgAlgo);
        rBtn04.setToggleGroup(tgAlgo);
    }

    public void btnAddProcess_clicked() {
        boolean error = false;
        //Break point
        if (txtProcess.getText().equals("") || txtBurstTime.getText().equals("") || txtArrivalTime.getText().equals("")) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Incomplete field input.", ButtonType.OK);
            alert.show();
            return;
        }

        if (processList.size() == 10) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Max number of process entered.", ButtonType.OK);
            alert.show();
            return;
        }

        if (Objects.equals(txtPriority.getText(), "")) {
            txtPriority.setText("0");
        }

        Process process = new Process(txtProcess.getText(), Integer.parseInt(txtBurstTime.getText()), Integer.parseInt(txtArrivalTime.getText()), Integer.parseInt(txtPriority.getText()));

        //Process cannot duplicate
        for (Process p : tblProcess.getItems()) {
            if (p.getProcessName().equals(txtProcess.getText())) {
                error = true;
                break;
            }
        }

        if (!error) {
            tblProcess.getItems().add(process);
            tbcProcess.setCellValueFactory(new PropertyValueFactory<>("processName"));
            tbcBurst.setCellValueFactory(new PropertyValueFactory<>("burstTime"));
            tbcArrival.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
            tbcPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
            tbcDelete.setCellValueFactory(new PropertyValueFactory<>("btnDelete"));

            for (Process p: tblProcess.getItems()) {
                Button btnDel = p.getBtnDelete();

                btnDel.setOnAction(event -> deleteRow(p));
            }

            txtProcess.setText("");
            txtBurstTime.setText("");
            txtArrivalTime.setText("");
            txtPriority.setText("");
        } else {
            //Show Message
            Alert alert = new Alert(Alert.AlertType.ERROR, "Process name exist!", ButtonType.OK);
            alert.show();
            txtProcess.setText("");
        }
    }

    public void btnRun_clicked() {
        resultList.clear();
        processList.clear();

        //Eliminate reference when repeat running
        for (Process p : tblProcess.getItems()){
            processList.add(p.clone());
        }

        if (processList.size() >= 3) {
            if (rBtn01.isSelected()) roundRobin();
            else if (rBtn02.isSelected()) return;
            else if (rBtn03.isSelected()) return;
            else if (rBtn04.isSelected()) return;

            createGanttChart();
            createResultTable();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Insufficient process entered.", ButtonType.OK);
            alert.show();
        }

        for (String qLog: queueLog) {
            System.out.println(qLog);
        }
    }

    public void txtOnKey_pressed(KeyEvent keyEvent) {
        if(keyEvent.getCode().equals(KeyCode.ENTER)){
            txtProcess.requestFocus();
            btnAddProcess_clicked();
        }
    }

    private void roundRobin() {
        int quantumTime = 2;
        int currTime = 0;

        PriorityQueue<Process> processQueue = new PriorityQueue<>();
        processQueue.addAll(processList);

        if (!processQueue.isEmpty()) {
            Queue<Process> readyQueue = new LinkedList<>();
            Stack<Process> bufferStack = new Stack<>();

            //Start algo
            while (!processQueue.isEmpty() || !readyQueue.isEmpty()) {
                //Push process by arrival time into ready queue
                while (!processQueue.isEmpty() && processQueue.peek().getArrivalTime() <= currTime) {
                    readyQueue.offer(processQueue.poll());
                }

                while (!bufferStack.isEmpty()) {
                    readyQueue.offer(bufferStack.pop());
                }

                if (!readyQueue.isEmpty()) {
                    Process currProcess = readyQueue.poll();
                    if (currProcess.getRemainingBurst() <= quantumTime) {
                        currTime += currProcess.getRemainingBurst();

                        currProcess.setRemainingBurst(0);
                        currProcess.setProcessCount(currProcess.getProcessCount() + 1);
                        currProcess.setFinishTime(currTime);
                        Process.setTotalTime(currTime);
                    } else {
                        currTime += quantumTime;

                        currProcess.setRemainingBurst(currProcess.getRemainingBurst() - quantumTime);
                        bufferStack.push(currProcess);
                    }
                    resultList.add(new ProcessGanttData(currProcess.getProcessName(), currTime));
                    log = log.concat(currProcess.getProcessName() + "(" + currTime + "), ");

                    String currQueue ="T" + currTime + ": ";
                    //Queue log
                    for (Process p : readyQueue) {
                        currQueue = currQueue.concat(p.getProcessName() + "(" + p.getRemainingBurst() + "), ");
                    }

                    currQueue = currQueue.substring(0, currQueue.length()-2);
                    queueLog.add(currQueue);

                }else{
                    currTime++;
                }
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No process available!", ButtonType.OK);
            alert.show();
        }
    }

    private void createGanttChart(){
        pnGanttChart.getChildren().clear();

        int minWidth = 850;

        Rectangle rectangle = new Rectangle(minWidth,30);
        rectangle.setFill(Color.WHITE);
        rectangle.setStroke(Color.BLACK);

        StackPane spProcess = new StackPane();
        spProcess.setMinWidth(minWidth);

        StackPane spTime = new StackPane();
        spTime.setMinWidth(minWidth);

        HBox processData = new HBox();
        processData.setMaxWidth(minWidth);
        processData.setAlignment(Pos.TOP_CENTER);

        HBox timeData = new HBox();
        timeData.setMaxWidth(minWidth);

        timeData.getChildren().add(setLine("0", 0));
        int widthStack = 0;

        for (ProcessGanttData g: resultList) {
            Label lblProcess = new Label(g.getProcessName());

            double widthLabel = (((double) minWidth/Process.getTotalTime())*(g.getProcessTime()-widthStack));

            lblProcess.setMinWidth(widthLabel);
            lblProcess.setPadding(new Insets(10,0,10,0));
            lblProcess.setWrapText(true);

            lblProcess.setAlignment(Pos.BASELINE_CENTER);

            processData.getChildren().add(lblProcess);
            timeData.getChildren().add(setLine(Integer.toString(g.getProcessTime()), widthLabel));
            widthStack = g.getProcessTime();
        }

        spProcess.getChildren().add(processData);
        spTime.getChildren().add(timeData);
        pnGanttChart.getChildren().addAll(rectangle, spProcess, spTime);
    }

    private void createResultTable(){
        pnResult.getChildren().clear();

        int totalTA = 0;
        int totalWT=0;

        GridPane gpResult = new GridPane();
        gpResult.setStyle("-fx-background-color: white; -fx-grid-lines-visible: true");

        GridPane gpSummary = new GridPane();


        //Result Table
        for(int i =0; i< 6 ; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setMinWidth(50);
            col.setMaxWidth(200);
            gpResult.getColumnConstraints().add(col);
            gpResult.addColumn(0);
        }

        for(int j = 0; j< processList.size()+1 ; j++){
            gpResult.addRow(0);
        }
        //Initialize Header
        Label lblProcess = new Label("Process");
        Label lblAT =  new Label("Arrival Time");
        Label lblBT = new Label("Burst Time");
        Label lblFT = new Label("Finish Time");
        Label lblTA = new Label("Turnaround Time");
        Label lblWT = new Label("Wait Time");
        lblProcess.setFont(Font.font("System",FontWeight.BOLD,12));
        lblAT.setFont(Font.font("System",FontWeight.BOLD,12));
        lblBT.setFont(Font.font("System",FontWeight.BOLD,12));
        lblFT.setFont(Font.font("System",FontWeight.BOLD,12));
        lblTA.setFont(Font.font("System",FontWeight.BOLD,12));
        lblWT.setFont(Font.font("System",FontWeight.BOLD,12));

        lblProcess.setPadding(new Insets(5,5,5,5));
        lblAT.setPadding(new Insets(5,5,5,5));
        lblBT.setPadding(new Insets(5,5,5,5));
        lblFT.setPadding(new Insets(5,5,5,5));
        lblTA.setPadding(new Insets(5,5,5,5));
        lblWT.setPadding(new Insets(5,5,5,5));

        gpResult.add(lblProcess, 0,0);
        gpResult.add(lblAT,1,0);
        gpResult.add(lblBT,2,0);
        gpResult.add(lblFT, 3, 0);
        gpResult.add(lblTA,4,0);
        gpResult.add(lblWT,5,0);

        for (int i=0; i<processList.size(); i++) {
            totalTA += processList.get(i).getTurnAroundTime();
            totalWT += processList.get(i).getWaitingTime();

            Label lblResP= new Label(processList.get(i).getProcessName());
            Label lblResAT=new Label(Integer.toString(processList.get(i).getArrivalTime()));
            Label lblResBT=new Label(Integer.toString(processList.get(i).getBurstTime()));
            Label lblResFT=new Label(Integer.toString(processList.get(i).getFinishTime()));
            Label lblResTA=new Label(Integer.toString(processList.get(i).getTurnAroundTime()));
            Label lblResWT=new Label(Integer.toString(processList.get(i).getWaitingTime()));

            lblResP.setPadding(new Insets(3,5,3,5));
            lblResAT.setPadding(new Insets(3,5,3,5));
            lblResBT.setPadding(new Insets(3,5,3,5));
            lblResFT.setPadding(new Insets(3,5,3,5));
            lblResTA.setPadding(new Insets(3,5,3,5));
            lblResWT.setPadding(new Insets(3,5,3,5));

            gpResult.add(lblResP,0, i+1);
            gpResult.add(lblResAT,1, i+1);
            gpResult.add(lblResBT,2, i+1);
            gpResult.add(lblResFT,3, i+1);
            gpResult.add(lblResTA,4, i+1);
            gpResult.add(lblResWT,5, i+1);
        }

        //Summary
        gpSummary.addColumn(0);
        for(int j=0; j<4; j++){
            gpSummary.addRow(0);
        }

        Label lblTotTA = new Label("Total Turnaround Time: " + totalTA);
        Label lblTotWT = new Label("Total Waiting Time: " + totalWT);
        Label lblAvgTA = new Label("Average Turnaround Time: " + String.format("%.2f", (double)totalTA/(double)processList.size()));
        Label lblAvgWT = new Label("Average Turnaround Time: " + String.format("%.2f", (double)totalWT/(double)processList.size()));

        lblTotTA.setFont(Font.font("System",FontWeight.BOLD,16));
        lblTotWT.setFont(Font.font("System",FontWeight.BOLD,16));
        lblAvgTA.setFont(Font.font("System",FontWeight.BOLD,16));
        lblAvgWT.setFont(Font.font("System",FontWeight.BOLD,16));

        gpSummary.add(lblTotTA, 0, 0);
        gpSummary.add(lblAvgTA, 0, 1);
        gpSummary.add(lblTotWT, 0, 2);
        gpSummary.add(lblAvgWT, 0, 3);

        gpSummary.setPadding(new Insets(0,30,0,30));
        gpSummary.setHgap(10);

        pnResult.getChildren().addAll(gpResult, gpSummary);
    }

    private Node setLine(String v, double width){
        VBox vBox = new VBox();
        Line line = new Line(0,0,0,40);
        Label value = new Label(v);

        vBox.setAlignment(Pos.CENTER_RIGHT);

        vBox.setMinWidth(width);
        vBox.setMaxWidth(width);

        vBox.getChildren().addAll(line,value);

        return vBox;
    }

    private void deleteRow(Process p){
        tblProcess.getItems().remove(p);
    }

    public void btnClear_clicked() { tblProcess.getItems().clear();
    }
}