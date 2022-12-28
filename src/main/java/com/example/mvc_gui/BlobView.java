package com.example.mvc_gui;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class BlobView extends StackPane implements BlobModelListener, IModelListener {
    GraphicsContext gc;
    Canvas myCanvas;
    BlobModel model;
    InteractionModel iModel;

    BlobController controller;
    PixelReader LassoReader; // for checking the offscreen bitmap's colours
    PixelReader RectangleReader; // for checking the offscreen bitmap's colours

    public BlobView() {
        myCanvas = new Canvas(800,800);
        gc = myCanvas.getGraphicsContext2D();
        gc.setFill(Color.ORANGE);
//        gc.fillRect(100,100,200,200);

        this.getChildren().add(myCanvas);
    }

    private void setupOffscreen() {
        // offscreen bitmap for checking 'contains' on an oddly-shaped polygon
        Canvas checkCanvas = new Canvas(800, 800);
        GraphicsContext checkGC = checkCanvas.getGraphicsContext2D();
        checkGC.setFill(Color.RED);

        // draws lasso on background
        checkGC.beginPath();
        checkGC.moveTo(iModel.getPoints().get(0).getX(), iModel.getPoints().get(0).getY());
        for (Point2D points : iModel.points) {
            checkGC.lineTo(points.getX(), points.getY());
        }
        checkGC.closePath();
        checkGC.fill();

        WritableImage buffer = checkCanvas.snapshot(null, null);
        LassoReader = buffer.getPixelReader();

        //// Repeat For Rectangle
        // offscreen bitmap for checking 'contains' on rectangle polygon
        Canvas RectangleCheckCanvas = new Canvas(800, 800);
        GraphicsContext RectangleCheckGC = RectangleCheckCanvas.getGraphicsContext2D();
        RectangleCheckGC.setFill(Color.GREEN);
        // draws Rectangle on background
        int lastvalue = iModel.getPoints().size()-1;

        double first_x = iModel.getPoints().get(0).getX();
        double first_y = iModel.getPoints().get(0).getY();
        double last_x = iModel.getPoints().get(lastvalue).getX();
        double last_y = iModel.getPoints().get(lastvalue).getY();
        // if mouse is dragged into quadrant III from origin click
        if(last_x>first_x && last_y>first_y){
            RectangleCheckGC.fillRect(first_x, first_y, last_x-first_x, last_y-first_y);
        }
        // if mouse is dragged into quadrant II from origin click
        else if(last_x<first_x && last_y>first_y){
            RectangleCheckGC.fillRect(last_x, first_y, first_x-last_x, last_y-first_y);
        }
        // if mouse is dragged into quadrant IV from origin click
        else if(last_x>first_x && last_y<first_y){
            RectangleCheckGC.fillRect(first_x, last_y, last_x-first_x, first_y-last_y);
        }
        //else mouse is in quadrant I
        else{
            RectangleCheckGC.fillRect(last_x, last_y, first_x-last_x, first_y-last_y);
        }

        WritableImage RectangleBuffer = RectangleCheckCanvas.snapshot(null, null);
        RectangleReader = RectangleBuffer.getPixelReader();
        ////
    }

    private void draw() {
        if (iModel.getTraining()) {
            // draw training mode
            gc.setFill(Color.LIGHTCORAL);
            gc.fillRect(0,0,myCanvas.getWidth(),myCanvas.getHeight());

            gc.setFill(Color.DARKKHAKI);
            Blob blobToHit = model.getBlobs().get(iModel.getTrainingBlobClick());
            gc.fillOval(blobToHit.x - blobToHit.r, blobToHit.y - blobToHit.r, blobToHit.r * 2, blobToHit.r * 2);

        } else {
            // draw selection mode
            gc.clearRect(0, 0, myCanvas.getWidth(), myCanvas.getHeight());
            int buttonNumber = 1;

            for (Blob blob : model.getBlobs()) {
                if (iModel.getSelectedBlobs().contains(blob)) {
                    gc.setFill(Color.TOMATO);
                } else {
                    gc.setFill(Color.BEIGE);
                }
                gc.fillOval(blob.x - blob.r, blob.y - blob.r, blob.r * 2, blob.r * 2);

                gc.setFill(Color.BLUE);
                gc.fillText(String.valueOf(buttonNumber), blob.x, blob.y);
                buttonNumber++; // increment blob number
            }

            // points
            for (Point2D point : iModel.getPoints()) {
                gc.setFill(Color.GAINSBORO);
                gc.fillOval(point.getX(), point.getY(), 3, 3);
            }

            // draw rectangle && making square display on full 360 rotation of the mouse around a point
            if (iModel.points.size() != 0) {
                gc.setStroke(Color.DARKGOLDENROD);
                double first_x = iModel.getPoints().get(0).getX();
                double first_y = iModel.getPoints().get(0).getY();
                int lastvalue = iModel.getPoints().size() - 1;
                double last_x = iModel.getPoints().get(lastvalue).getX();
                double last_y = iModel.getPoints().get(lastvalue).getY();

                // if mouse is dragged into quadrant III from origin click
                if (last_x > first_x && last_y > first_y) {
                    gc.strokeRect(first_x, first_y, last_x - first_x, last_y - first_y);
                }
                // if mouse is dragged into quadrant II from origin click
                else if (last_x < first_x && last_y > first_y) {
                    gc.strokeRect(last_x, first_y, first_x - last_x, last_y - first_y);
                }
                // if mouse is dragged into quadrant IV from origin click
                else if (last_x > first_x && last_y < first_y) {
                    gc.strokeRect(first_x, last_y, last_x - first_x, first_y - last_y);
                }
                //else mouse is in quadrant I
                else {
                    gc.strokeRect(last_x, last_y, first_x - last_x, first_y - last_y);
                }
            }

        }
    }

    public void setModel(BlobModel newModel) {
        model = newModel;
    }

    public void setIModel(InteractionModel newIModel) {
        iModel = newIModel;
    }

    @Override
    public void modelChanged() {
        draw();

    }

    @Override
    public void iModelChanged() {
        draw();
    }

    public void setController(BlobController controller) {
        this.controller = controller;
        myCanvas.setOnMousePressed(controller::handlePressed);
        myCanvas.setOnMouseDragged(controller::handleDragged);
        myCanvas.setOnMouseReleased(this::handleReleased);
    }

    private void handleReleased(MouseEvent mouseEvent) {
        if(iModel.points.size()>0){
            setupOffscreen();
        }
        controller.handleReleased(mouseEvent, LassoReader, RectangleReader);
    }

}
