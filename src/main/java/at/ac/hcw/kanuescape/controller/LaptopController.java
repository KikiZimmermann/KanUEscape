package at.ac.hcw.kanuescape.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LaptopController {

    private static final double BOX_W = 120;
    private static final double BOX_H = 25;

    private Label draggedFromGap = null;
    private static final double ANSWER_BANK_X = 800; // Start X of answer-area
    private static final double ANSWER_BANK_MIN_X = 750; // Right of X=750 -> answer-area

    private final List<Label> gaps = new ArrayList<>(); // for later -> check if all answers correct

    @FXML
    private Pane overlayPane;

    private Runnable onSolved;

    public void setOnSolved(Runnable onSolved) { this.onSolved  = onSolved; }

    public boolean isSolved() {
        for (Label gap : gaps) {
            String correct = (String) gap.getUserData();
            String given = gap.getText() == null ? "" : gap.getText().trim();
            if(!given.equals(correct)) return false;
        }
        return true;
    }

    @FXML
    public void initialize() {
        resetPuzzle();
    }

    public void resetPuzzle(){

        gaps.clear();                           //gaps zurückgesetzt
        draggedFromGap = null;                  //nichts gedragged
        overlayPane.getChildren().clear();      //resettet nodes

        // Empty answer-spots (drag-to position)
        overlayPane.getChildren().add(createGap(85, 0, "class"));
        overlayPane.getChildren().add(createGap(127, 56, "static"));
        overlayPane.getChildren().add(createGap(199, 112, "REQUIRED_TASKS"));
        overlayPane.getChildren().add(createGap(324, 168, "false"));
        overlayPane.getChildren().add(createGap(320, 415, "true"));
        overlayPane.getChildren().add(createGap(158, 442, "else if"));
        overlayPane.getChildren().add(createGap(158, 498, "else"));
        overlayPane.getChildren().add(createGap(143, 550, "switch"));
        overlayPane.getChildren().add(createGap(524, 579, "Get focussed!"));
        overlayPane.getChildren().add(createGap(168, 767, "printSummary"));

        List<Integer> answerYPositions = new ArrayList<>(List.of(20, 70, 120, 170, 220, 270, 320, 370, 420, 470));
        Collections.shuffle(answerYPositions);

        // Answers, draggable
        overlayPane.getChildren().add(createAnswer("class", 800, answerYPositions.get(0)));
        overlayPane.getChildren().add(createAnswer("static", 800, answerYPositions.get(1)));
        overlayPane.getChildren().add(createAnswer("REQUIRED_TASKS", 800, answerYPositions.get(2)));
        overlayPane.getChildren().add(createAnswer("false", 800, answerYPositions.get(3)));
        overlayPane.getChildren().add(createAnswer("true", 800, answerYPositions.get(4)));
        overlayPane.getChildren().add(createAnswer("else if", 800, answerYPositions.get(5)));
        overlayPane.getChildren().add(createAnswer("else", 800, answerYPositions.get(6)));
        overlayPane.getChildren().add(createAnswer("switch", 800, answerYPositions.get(7)));
        overlayPane.getChildren().add(createAnswer("Get focussed!", 800, answerYPositions.get(8)));
        overlayPane.getChildren().add(createAnswer("printSummary", 800, answerYPositions.get(9)));


        overlayPane.setOnMouseClicked(e ->
                System.out.println((int)e.getX() + ", " + (int)e.getY()) );

        // If answer unused (not dropped in gap or pulled from gap again) -> new answer label
        overlayPane.setOnDragOver(e -> { if (e.getDragboard().hasString() && e.getX() >= ANSWER_BANK_MIN_X) {
            e.acceptTransferModes(TransferMode.MOVE);
        }
            e.consume();
        });

        overlayPane.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean success = false;
            if (db.hasString() && e.getX() >= ANSWER_BANK_MIN_X) {
                overlayPane.getChildren().add(createAnswer(db.getString(), ANSWER_BANK_X, e.getY()));
                success = true;
            }
            e.setDropCompleted(success);
            e.consume();
        });
    }

    private Label createGap(double x, double y, String correctAnswer) {
        Label gap = new Label("");
        gap.setPrefSize(BOX_W, BOX_H);
        gap.setMinSize(BOX_W, BOX_H);
        gap.setMaxSize(BOX_W, BOX_H);
        gap.setAlignment(Pos.CENTER);

        gap.setLayoutX(x);
        gap.setLayoutY(y);

        gap.setStyle("""
                -fx-border-color: #34344A;
                -fx-border-width: 2;
                -fx-background-color: #282839;
                -fx-padding: 4 8;
                -fx-text-fill: #EAEAF0;
                """);

        gap.setUserData(correctAnswer);

        gap.setOnDragOver(e -> {
            if (!e.getDragboard().hasString()) return;
            boolean gapEmpty = gap.getText() == null || gap.getText().isBlank();
            if (gapEmpty) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
            e.consume();
        });

        gap.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean gapEmpty = gap.getText() == null || gap.getText().isBlank();

            boolean success = false;
            if (db.hasString() && gapEmpty) {
                gap.setText(db.getString());
                success = true;
            }
            e.setDropCompleted(success);
            e.consume();
        });

        // NEW: remove answer that was placed in gap
        gap.setOnDragDetected(e -> {
            if (gap.getText() == null || gap.getText().isBlank()) return;
            draggedFromGap = gap;
            Dragboard db = gap.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(gap.getText());
            db.setContent(content);
            e.consume();
        });

        // NEW: if move was successful -> empty gap
        gap.setOnDragDone(e -> {
            if (e.getTransferMode() == TransferMode.MOVE && draggedFromGap == gap) {
                gap.setText("");
                draggedFromGap = null;
            }
            e.consume();
        });

        gaps.add(gap); // tells which fields to check -> to see later if everything correct

        return gap;
    }

    // drag-able answer
    private Label createAnswer(String text, double x, double y) {
        Label lbl = new Label(text);
        lbl.setPrefSize(BOX_W, BOX_H);
        lbl.setLayoutX(x);
        lbl.setLayoutY(y);
        lbl.setAlignment(Pos.CENTER);

        lbl.setStyle("""
                -fx-background-color: #3E3E56;
                -fx-border-color: #666;
                -fx-padding: 4 8;
                -fx-text-fill: #EAEAF0;
                """);

        lbl.setOnDragDetected(e -> {
            Dragboard db = lbl.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(lbl.getText());
            db.setContent(content);
            e.consume();
        });

        // NEW making used answer disappear
        lbl.setOnDragDone(e -> {
            if (e.getTransferMode() == TransferMode.MOVE) {
                overlayPane.getChildren().remove(lbl);
            }
            e.consume();
        });

        return lbl;
}


//private boolean checkAllGaps() {
//    boolean allCorrect = true;
//
//    for (Label gap : gaps) {
//        String correct = (String) gap.getUserData();
//        String given = gap.getText() == null ? "" : gap.getText().trim();
//
//        boolean ok = given.equals(correct);
//
//        if (ok) {
//            gap.setStyle("""
//                    -fx-border-color: #2f855a;
//                    -fx-border-width: 2;
//                    -fx-background-color: rgba(47, 133, 90, 0.25);
//                    -fx-padding: 4 8;
//                    """);
//        } else {
//            gap.setStyle("""
//                    -fx-border-color: #c53030
//                    -fx-border-width: 2;
//                    -fx-background-color: rgba(197, 48, 48, 0.25;
//                    -fx-padding: 4 8;
//                    """);
//            allCorrect = false;
//        }
//    }
//    return allCorrect;
//}
//
//    private void notifySolved() {
//        if (onSolved != null) onSolved.run();
//    }
}