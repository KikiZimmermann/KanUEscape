package at.ac.hcw.kanuescape.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BuecherController {
        // UI elements injected from FXML
    @FXML private AnchorPane BuecherScene;
    @FXML private HBox shelveOne;
    @FXML private HBox shelveTwo;
    @FXML private HBox shelveThree;
    @FXML private Label winState;
    @FXML private Label escape_alert;
    private boolean win = false;



    // Defined winning sequences for each shelf (based on ImageView IDs)
    private final List<String> target1 = List.of("image11", "image12", "image13", "image14", "image15");
    private final List<String> target2 = List.of("image21", "image22", "image23", "image24", "image25");
    private final List<String> target3 = List.of("image31", "image32", "image33", "image34", "image35");

    // Dynamic lists to track the current arrangement of books by the user
    private List<String> shelveList1 = new ArrayList<>();
    private List<String> shelveList2 = new ArrayList<>();
    private List<String> shelveList3 = new ArrayList<>();

    // Variables for drag-and-drop mechanics: tracking mouse position and recovery coordinates
    private double mouseX, mouseY, lastValidTranslateX;

    @FXML
    private void initialize() {
        // Randomize the position of books in each shelf at the start of the puzzle
        shuffleShelf(shelveOne);
        shuffleShelf(shelveTwo);
        shuffleShelf(shelveThree);

        // Populate initial lists with the randomized starting positions
        refreshAllLists();

        // Attach event listeners to all books to make them interactive
        makeAllBooksDraggable(shelveOne);
        makeAllBooksDraggable(shelveTwo);
        makeAllBooksDraggable(shelveThree);
    }

    /**
     * Shuffles the children of an HBox to create a random puzzle start.
     */
    private void shuffleShelf(HBox shelf) {
        List<Node> books = new ArrayList<>(shelf.getChildren());
        Collections.shuffle(books);
        shelf.getChildren().setAll(books); // Re-add nodes in shuffled order
    }

    /**
     * Utility to synchronize all tracking lists with the visual state of the UI.
     */
    private void refreshAllLists() {
        updateOrderList(shelveOne);
        updateOrderList(shelveTwo);
        updateOrderList(shelveThree);
    }

    /**
     * Sorts the books in a shelf based on their visual horizontal position
     * and updates the corresponding tracking list.
     */
    private void updateOrderList(HBox shelf) {
        // Collect all ImageView nodes from the HBox
        List<Node> allBooks = new ArrayList<>();
        for (Node node : shelf.getChildren()) {
            if (node instanceof ImageView) {
                allBooks.add(node);
            }
        }

        // Sort books by their X-coordinate relative to the shelf's parent container
        allBooks.sort(Comparator.comparingDouble(node -> node.getBoundsInParent().getMinX()));

        // Extract IDs in the new sorted order
        List<String> currentIds = new ArrayList<>();
        for (Node node : allBooks) {
            currentIds.add(node.getId());
        }

        // Assign the sorted ID list to the appropriate shelf variable
        if (shelf == shelveOne) {
            shelveList1 = currentIds;
        } else if (shelf == shelveTwo) {
            shelveList2 = currentIds;
        } else if (shelf == shelveThree) {
            shelveList3 = currentIds;
        }

        // Check if the current state matches the solution
        checkWinCondition();
    }

    /**
     * Compares current shelf arrangements with target sequences to detect a win.
     */
    private void checkWinCondition() {
        // The user wins only if all three shelves match the target orders perfectly
        if (shelveList1.equals(target1) &&
                shelveList2.equals(target2) &&
                shelveList3.equals(target3)) {
            win = true;
            BuecherScene.setStyle("-fx-background-color: lightgreen;");
            winState.setOpacity(1);
            escape_alert.setOpacity(0);
        }
    }

    // --- Drag & Drop Implementation ---

    /**
     * Loops through a shelf and enables drag logic for every image.
     */
    private void makeAllBooksDraggable(HBox shelf) {
        for (Node node : shelf.getChildren()) {
            if (node instanceof ImageView) {
                enableDrag((ImageView) node);
            }
        }
    }

    /**
     * Adds mouse event handlers to a book for dragging functionality.
     */
    private void enableDrag(ImageView book) {
        // Save initial coordinates when the user clicks the book
        book.setOnMousePressed(event -> {
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
            lastValidTranslateX = book.getTranslateX(); // Store position for potential reset
        });

        // Update the book's position as the mouse moves
        book.setOnMouseDragged(event -> {
            double deltaX = event.getSceneX() - mouseX;
            double deltaY = event.getSceneY() - mouseY;
            book.setTranslateX(book.getTranslateX() + deltaX);
            book.setTranslateY(book.getTranslateY() + deltaY);
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
        });

        // Handle the drop event: validate position and snap into place
        book.setOnMouseReleased(event -> {
            HBox currentShelf = (HBox) book.getParent();

            // Check if book is dragged outside the shelf or overlaps another book
            boolean isOutOfShelf = !currentShelf.getLayoutBounds().contains(book.getBoundsInParent());
            boolean hasOverlap = checkOverlap(book, currentShelf);

            if (isOutOfShelf || hasOverlap) {
                // If invalid, return to the last known good position
                book.setTranslateX(lastValidTranslateX);
                book.setTranslateY(0);
            } else {
                // If valid, align horizontally and refresh the list order
                book.setTranslateY(0);
                updateOrderList(currentShelf);
            }
        });
    }

    /**
     * Checks if a book is colliding with any other book on the same shelf.
     */
    private boolean checkOverlap(ImageView currentBook, HBox shelf) {
        for (Node other : shelf.getChildren()) {
            if (other != currentBook && other instanceof ImageView) {
                // Return true if the bounding boxes intersect
                if (currentBook.getBoundsInParent().intersects(other.getBoundsInParent())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the root pane of the scene.
     */
    public AnchorPane getBuecherScene() {
        return BuecherScene;
    }

//    getter for win
    public boolean isSolved() {
        return win;
    }

    // reset methode für New Game
    public void resetPuzzle() {
        win = false;

        if (BuecherScene != null) {
            BuecherScene.setStyle(""); // oder euer default style
        }
        if (winState != null) winState.setOpacity(0);
        if (escape_alert != null) escape_alert.setOpacity(1);

        // neu mischen
        shuffleShelf(shelveOne);
        resetBookTransforms(shelveOne);

        shuffleShelf(shelveTwo);
        resetBookTransforms(shelveTwo);

        shuffleShelf(shelveThree);
        resetBookTransforms(shelveThree);


        refreshAllLists();
    }
    private void resetBookTransforms(HBox shelf) {
        for (Node n : shelf.getChildren()) {
            n.setTranslateX(0);
            n.setTranslateY(0);
        }
    }


}