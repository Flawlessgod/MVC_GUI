package com.example.mvc_gui;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

public class InteractionModel {
    List<IModelListener> subscribers;

    ArrayList<Blob> selectedBlobs;

    ArrayList<Point2D> points; // points for the user path drawn

    ArrayList<Blob> clipBoard;

    ArrayList<Long> reactionTimeArray;

    int blobToClick;

    boolean training = false;

    public InteractionModel() {
        subscribers = new ArrayList<>();
        selectedBlobs = new ArrayList<>();
        points = new ArrayList<>();
        clipBoard = new ArrayList<>();
        blobToClick =0;
        reactionTimeArray = new ArrayList<>();
    }

    public void addSubscriber(IModelListener sub) {
        subscribers.add(sub);
    }

    private void notifySubscribers() {
        subscribers.forEach(s -> s.iModelChanged());
    }

    public void addSelected(Blob b) {
        selectedBlobs.add(b);
        notifySubscribers();
    }

    public void setSelected(ArrayList<Blob> blobs){
        selectedBlobs = blobs;
        notifySubscribers();
    }

    public void deepClone(){
        clipBoard = new ArrayList<>();
        for (Blob blob: selectedBlobs) {
            Blob aNewBlob = new Blob(blob.x, blob.y);
            aNewBlob.setRadius(blob.getRadius());
            clipBoard.add(aNewBlob);
        }
    }
    public void cut(){
        deepClone();
    }

    public ArrayList<Blob> paste(){
        selectedBlobs = new ArrayList<>();
        for (Blob blob: clipBoard) {
            Blob aNewBlob = new Blob(blob.x, blob.y);
            aNewBlob.setRadius(blob.getRadius());
            selectedBlobs.add(aNewBlob);
        }
        return selectedBlobs;
    }

    public void removeSelected(Blob b) {
        selectedBlobs.remove(b);
        notifySubscribers();
    }
    public void unselect() {
        selectedBlobs.clear();
        notifySubscribers();
    }
    public ArrayList<Blob> getSelectedBlobs() {
        return selectedBlobs;
    }

    public void cleaPoints(){
        points.clear();
        notifySubscribers();
    }

    public ArrayList<Point2D> getPoints(){
        return points;
    }

    // adds a new lasso point
    public void addPoints(double x, double y){
        Point2D point2D = new Point2D(x, y);
        points.add(point2D);
        notifySubscribers();
    }

    // aim training
    public void setTraining(boolean training) {
        this.training = training;
        notifySubscribers();
    }

    public boolean getTraining(){return this.training;}

    public int getTrainingBlobClick(){
        return blobToClick;
    }
    public void incrementTrainingBlobClick(){
        blobToClick+=1;
        notifySubscribers();

    }
    public void resetTrainingBlobClick(){
        blobToClick=0;
    }

    // reaction time
    public void addReactionTimePoint(long time){
        reactionTimeArray.add(time);
    }

    public ArrayList<Long> getReactionTimeArray(){
        return reactionTimeArray;
    }

}

