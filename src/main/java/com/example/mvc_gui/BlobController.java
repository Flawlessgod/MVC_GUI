package com.example.mvc_gui;

import javafx.scene.image.PixelReader;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.time.Clock;
import java.util.ArrayList;

public class BlobController {
    BlobModel model;
    InteractionModel iModel;
    double prevX, prevY;
    double dX, dY;

    enum State {READY, PREPARE_CREATE, DRAGGING, RESIZE, MultiSelect, TRAINING}

    State currentState = State.READY;

    public BlobController() {

    }

    public void setModel(BlobModel newModel) {
        model = newModel;
    }

    public void setIModel(InteractionModel newIModel) {
        iModel = newIModel;
    }

    public void handleKeyPressed(KeyEvent keyEvent) {
        switch (currentState) {
            case READY -> {
                if (keyEvent.getCode() == KeyCode.DELETE) {
                    model.deleteBlob(iModel.getSelectedBlobs());
                }
                else if (keyEvent.getCode() == KeyCode.C) {
                    if( keyEvent.isControlDown() ) {
                        iModel.deepClone();
                    }
                }
                else if (keyEvent.getCode() == KeyCode.X) {
                    if (keyEvent.isControlDown()) {
                        iModel.cut();
                        model.deleteBlob(iModel.selectedBlobs);
                        iModel.unselect();
                    }
                }
                else if (keyEvent.getCode() == KeyCode.V) {
                    if (keyEvent.isControlDown()) {
                        ArrayList<Blob> pasted = iModel.paste();
                        model.addBlobs(pasted);
                    }
                }
                else if(keyEvent.getCode() == KeyCode.T && keyEvent.isControlDown()){
                    // can not enter training mode with no blobs
                    if(model.getBlobs().size()!=0) {
                        currentState = State.TRAINING;
                        iModel.setTraining(true);


                    }
                }
            }
        }
    }

    public void handlePressed(MouseEvent event) {
        switch (currentState) {
            case READY -> {
                if (model.hitBlob(event.getX(), event.getY())) {
                    if (event.isShiftDown() == false) {
                        Blob b = model.whichHit(event.getX(), event.getY());

                        if (event.isControlDown()) {
                            if (!iModel.getSelectedBlobs().contains(b)) { // don't double add blob
                                iModel.addSelected(b);
                            } else {
                                iModel.removeSelected(b);
                            }
                        }
                        else{ // just clicking of one blob resets selection
                            iModel.unselect();
                            iModel.addSelected(b);
                        }

                        prevX = event.getX();
                        prevY = event.getY();
                        currentState = State.DRAGGING;
                    } else { //resize
                        Blob b = model.whichHit(event.getX(), event.getY());
//                        if(!iModel.getSelectedBlobs().contains(b)){ // don't double add blob
//                            iModel.addSelected(b);
//                        }
                        prevX = event.getX();
                        currentState = State.RESIZE;
                    }
                } else if (event.isShiftDown()) {
                    currentState = State.PREPARE_CREATE;
                } else {
//                    iModel.unselect(); // did not hit anything
                    currentState = State.MultiSelect;
                }
            }
            case TRAINING-> {
                if(model.hitBlob(event.getX(),event.getY())){ //if you hit a blob
                    Blob blobHit = model.whichHit(event.getX(),event.getY());
                    if(model.getBlobs().get(iModel.getTrainingBlobClick()) == blobHit) { // if blob is the blob to hit
                        // if first blob is clicked start the timer test
                        Clock start = Clock.systemDefaultZone();
                        if(iModel.getTrainingBlobClick()==0){
//                            System.out.println("first: " + start.millis());
                            iModel.addReactionTimePoint(start.millis());
                        }

                        if (model.getBlobs().size()-1 == iModel.getTrainingBlobClick()) {
                            // add last time
                            iModel.addReactionTimePoint(start.millis());
                            currentState = State.READY;
                            iModel.setTraining(false);
                            iModel.resetTrainingBlobClick();

                            int count =0;
                            for (Long time:iModel.getReactionTimeArray()){
                                if(count==0){
                                    System.out.println("Reaction Time: "+ 0+"*");
                                    count++;
                                }
                                else{
                                    Long reactionTime = iModel.getReactionTimeArray().get(count)-iModel.getReactionTimeArray().get(count-1);
                                    System.out.println("Reaction Time:" + reactionTime);
                                    count++;
                                }
                            }

                            iModel.getReactionTimeArray().clear();

                        }else {
                            if(iModel.getTrainingBlobClick()!=0) {
//                                System.out.println("hitblob:" + start.millis());
                                iModel.addReactionTimePoint(start.millis());
                            }
                            iModel.incrementTrainingBlobClick();
                        }
                    }
                }
            }
        }
    }

    public void handleDragged(MouseEvent event) {
        switch (currentState) {
            case PREPARE_CREATE -> {
                currentState = State.READY;
            }
            case DRAGGING -> {
                dX = event.getX() - prevX;
                dY = event.getY() - prevY;
                prevX = event.getX();
                prevY = event.getY();
                model.moveBlob(iModel.getSelectedBlobs(), dX, dY);
            }
            case RESIZE -> {
                dX = event.getX() - prevX;
                prevX = event.getX();
                prevY = event.getY();
                model.resizeBlob(iModel.getSelectedBlobs(), dX);
            }
            case MultiSelect -> {
                // pass values into Interaction model
                iModel.addPoints(event.getX(), event.getY());
            }
        }
    }

    public void handleReleased(MouseEvent event, PixelReader LassoReader, PixelReader RectangleReader) {
        switch (currentState) {
            case PREPARE_CREATE -> {
                model.addBlob(event.getX(), event.getY());
                currentState = State.READY;
            }
            case DRAGGING, RESIZE -> {
                currentState = State.READY;
            }
            case MultiSelect -> {
                // perform lasso selection
                ArrayList<Blob> overlappingLassoBlobs = new ArrayList<>();
                for(Blob blob: model.getBlobs()){
                    if(LassoReader.getColor((int) blob.x, (int) blob.y).equals(Color.RED)){
                        overlappingLassoBlobs.add(blob);
                    }
                }

                // perform Rectangle selection
                ArrayList<Blob> overlappingRectangleBlobs = new ArrayList<>();
                for(Blob blob: model.getBlobs()){
                    if(RectangleReader.getColor((int) blob.x, (int) blob.y).equals(Color.GREEN)){
                        overlappingRectangleBlobs.add(blob);
                    }
                }

                // if lasso contains more blobs
                if(overlappingLassoBlobs.size()>overlappingRectangleBlobs.size()){
                    // control selection on lasso
                    if(event.isControlDown()){
                        ArrayList<Blob> swapSelect = swapSelections(overlappingLassoBlobs, iModel.getSelectedBlobs());
                        iModel.setSelected(swapSelect);

                    }else iModel.setSelected(overlappingLassoBlobs);
                }
                else{
                    // rectangle contains more blobs
                    if(event.isControlDown()){
                        ArrayList<Blob> swapSelect = swapSelections(overlappingRectangleBlobs, iModel.getSelectedBlobs());
                        iModel.setSelected(swapSelect);
                    } else iModel.setSelected(overlappingRectangleBlobs);
                }
                currentState = State.READY;
                iModel.cleaPoints();
            }

        }
    }

    // control lasso
    private ArrayList<Blob> swapSelections(ArrayList<Blob> overlappingBlobs, ArrayList<Blob> selectedBlobs) {

        for (Blob blob : overlappingBlobs) {
            if (selectedBlobs.contains(blob)) {
                selectedBlobs.remove(blob);
            } else {
                selectedBlobs.add(blob);
            }
        }
        return selectedBlobs;
    }

}
