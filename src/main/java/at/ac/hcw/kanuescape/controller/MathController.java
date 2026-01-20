package at.ac.hcw.kanuescape.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MathController {

    @FXML private Label progressLabel;
    @FXML private Label questionLabel;
    @FXML private Label statusLabel;

    @FXML private ImageView questionImage;

    @FXML private RadioButton optA;
    @FXML private RadioButton optB;
    @FXML private RadioButton optC;
    @FXML private RadioButton optD;

    @FXML private Button backBtn;
    @FXML private Button nextBtn;
    @FXML private Button checkBtn;

    // Quiz state
    private final ToggleGroup group = new ToggleGroup();
    private final List<Question> questions = new ArrayList<>();

    private int index = 0;
    private int[] selected;

    // Result callback
    private Runnable onSolved;
    public void setOnSolved(Runnable onSolved) { this.onSolved = onSolved; }

    @FXML
    public void initialize() {
        optA.setToggleGroup(group);
        optB.setToggleGroup(group);
        optC.setToggleGroup(group);
        optD.setToggleGroup(group);

        buildQuestions();

        selected = new int[questions.size()];
        for (int i = 0; i < selected.length; i++) selected[i] = -1;

        group.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) selected[index] = -1;
            else if (newT == optA) selected[index] = 0;
            else if (newT == optB) selected[index] = 1;
            else if (newT == optC) selected[index] = 2;
            else if (newT == optD) selected[index] = 3;
            statusLabel.setText("");
        });

        showQuestion(0);
    }

    private void buildQuestions() {

        // 6 multiple choice questions; each 1 answer correct

        questions.add(new Question(
                "Imaginary Numbers - Which one is NOT an imaginary number?",
                new String[]{"3i", "-7i", "5", "√(-4)"},
                2, null, null
        ));

        questions.add(new Question(
                "Differentiation - What's the correct derivative of f(x) = 3x² ?",
                new String[]{"6x²", "6x", "3x", "x³"},
                1, null, null
        ));

        questions.add(new Question(
                "Normal Vector - In R², a normal vector to v = (a, b) is …",
                new String[]{"(-b, a)", "(a, b)", "(b, a)", "(-b, -a)"},
                0, null, null
        ));

        questions.add(new Question(
                "Laws of exponents - Which result is correct?",
                new String[]{"(x³)² = x⁵", "2³ · 3² = 2⁵", "x⁴ / x² = x⁶", "a² · a³ = a⁵"},
                3, null, null
        ));

        questions.add(new Question(
                "Identity Matrix - Which one is the identity matrix?",
                        new String[]{
                                        "( 1  0 )\n     ( 0  1 )", // --> richtig; eingerückt
                                        "( 0  1 )\n     ( 1  0 )",
                                        "( 1  1 )\n     ( 0  1 )",
                                        "( 2  0 )\n     ( 0  2 )"
                        },
        0,null,null));

        questions.add(new Question(
                "Area of Definition - For which values is f(x) = 1/x NOT defined?",
                new String[]{"x > 0", "x < 0", "x = 0", "for any x"},
                2, null, null
        ));
    }

    private void showQuestion(int newIndex) {
        index = newIndex;
        Question q = questions.get(index);

        progressLabel.setText("Question " + (index + 1) + "/" + questions.size());
        questionLabel.setText(q.prompt);

        optA.setText("A) " + q.options[0]);
        optB.setText("B) " + q.options[1]);
        optC.setText("C) " + q.options[2]);
        optD.setText("D) " + q.options[3]);

        int sel = selected[index];
        if (sel == -1) group.selectToggle(null);
        else if (sel == 0) group.selectToggle(optA);
        else if (sel == 1) group.selectToggle(optB);
        else if (sel == 2) group.selectToggle(optC);
        else group.selectToggle(optD);

        backBtn.setDisable(index == 0);
        nextBtn.setDisable(index == questions.size() - 1);
        checkBtn.setDisable(index != questions.size() - 1);
        statusLabel.setText("");
    }

    @FXML
    private void onBack() {
        if (index > 0) showQuestion(index - 1);
    }

    @FXML
    private void onNext() {
        if (index < questions.size() - 1) showQuestion(index + 1);
    }

    @FXML
    private void onCheck() {
        for (int i = 0; i < selected.length; i++) {
            if (selected[i] == -1) {
                statusLabel.setText("Please answer all questions before checking!");
                return;
            }
        }

        boolean allCorrect = true;
        for (int i = 0; i < questions.size(); i++) {
            if (selected[i] != questions.get(i).correctIndex) {
                allCorrect = false;
                break;
            }
        }

        if (allCorrect) {
            statusLabel.setText("Good job, all answers are correct!");
            if (onSolved != null) onSolved.run();
        } else {
            statusLabel.setText("Not quite. Please check again!");
        }
    }

    private static class Question {
        final String prompt;
        final String[] options;
        final int correctIndex;
        final String questionImagePath;
        final String[] optionImagePaths;

        Question(String prompt, String[] options, int correctIndex, String questionImagePath, String[] optionImagePaths) {
            this.prompt = prompt;
            this.options = options;
            this.correctIndex = correctIndex;
            this.questionImagePath = questionImagePath;
            this.optionImagePaths = optionImagePaths;
        }
    }

    // reset methode für New Game
    public void resetQuiz() {
        index = 0;

        if (selected == null || selected.length != questions.size()) {
            selected = new int[questions.size()];
        }
        for (int i = 0; i < selected.length; i++) selected[i] = -1;

        group.selectToggle(null);

        if (statusLabel != null) statusLabel.setText("");
        showQuestion(0);
    }
}