package com.example.mvc_gui;

import java.util.*;

public class BlobModel {
    private List<BlobModelListener> subscribers;
    private List<Blob> blobs;

    public BlobModel() {
        subscribers = new ArrayList<>();
        blobs = new ArrayList<>();
    }

    public void addBlob(double x, double y) {
        blobs.add(new Blob(x,y));
        notifySubscribers();
    }

    public void moveBlob(ArrayList<Blob> blobs, double dx, double dy) {
        for (Blob blob : blobs ){
            blob.move(dx,dy);
        }
        notifySubscribers();
    }

    public void addSubscriber(BlobModelListener sub) {
        subscribers.add(sub);
        notifySubscribers();
    }

    private void notifySubscribers() {
        subscribers.forEach(s -> s.modelChanged());
    }

    public List<Blob> getBlobs() {
        return blobs;
    }

    public boolean hitBlob(double x, double y) {
        for (Blob b : blobs) {
            if (b.contains(x,y)) return true;
        }
        return false;
    }

    public Blob whichHit(double x, double y) {
        for (Blob b : blobs) {
            if (b.contains(x,y)) return b;
        }
        return null;
    }

    public void resizeBlob(ArrayList<Blob> selected, double dX) {
        for(Blob blob: selected){
            blob.setRadius(blob.getRadius()+dX);
            if(blob.getRadius()<5) blob.setRadius(5); // Setting min seize to 5px
        }
        notifySubscribers();
    }

    public void deleteBlob(ArrayList<Blob> toDelete){
        if(blobs==null){}
        else{
            for(Blob blob : toDelete){
                blobs.remove(blob);
            }
        }
        notifySubscribers();
    }

    // add pasted blobs
    public void addBlobs(ArrayList<Blob> blobsToAdd){
        blobs.addAll(blobsToAdd);
        notifySubscribers();
    }
}
