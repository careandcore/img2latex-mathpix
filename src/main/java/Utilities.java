import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.*;


/**
 * Utilities.java
 * Contains common methods used across the project.
 */
class Utilities {

    // Recognition object initialisation
    private static Recognition recognition = new Recognition();

    private static Path configFilePath = Paths.get("./config");

    /**
     * Original source: https://stackoverflow.com/a/33477375/4658633
     *
     * @return if macOS enabled dark mode.
     */
    static boolean isMacDarkMode() {

        try {
            // process will exit with 0 if dark mode enabled
            final Process process = Runtime.getRuntime().exec(new String[]{"defaults", "read", "-g", "AppleInterfaceStyle"});
            process.waitFor(100, TimeUnit.MILLISECONDS);
            return process.exitValue() == 0;
        } catch (IOException | InterruptedException | IllegalThreadStateException e) {
            return false;
        }

    }

    /**
     * @return current image in the system clipboard.
     */
    static Image getClipboardImage() {

        final Clipboard clipboard = Clipboard.getSystemClipboard();

        // return null if there is no image in the clipboard
        if (clipboard.hasImage()) {
            return clipboard.getImage();
        }

        return null;

    }

    /**
     * @param text the recognised result to be put into clipboard.
     */
    static void putStringIntoClipboard(String text) {

        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);

    }

    /**
     * Execute the OCR request in Java concurrent way.
     *
     * @param image image to be recognised.
     * @return recognised result.
     */
    static Response concurrentCall(Image image) {

        ExecutorService executor = Executors.newSingleThreadExecutor();

        if (recognition.setSrcParameters(image)) {
            Future<Response> result = executor.submit(recognition);
            try {
                return result.get();
            } catch (InterruptedException | ExecutionException e) {
                return null;
            }
        }

        return null;

    }

    /**
     * Method to set left and right margin to a node with default bottom margin
     *
     * @param node  node to be set margin
     * @param left  left margin
     * @param right right margin
     */
    static void setDefaultNodeMargin(Node node, int left, int right) {
        GridPane.setMargin(node, new Insets(0, right, 5, left));
    }

    /**
     * Method to get a customised Label
     *
     * @param text text to be displayed
     * @return a customised Label
     */
    static Label getTextLabel(String text) {
        Label label = new Label(text);
        // set font size
        label.setFont(Font.font(12));
        // set text color
        label.setTextFill(new Color(0.149, 0.149, 0.149, 1));

        return label;
    }

    /**
     * Set config file path.
     */
    static void setConfigFilePath() {
        configFilePath = Paths.get(System.getProperty("user.home") + "/Library/Image2LaTeX/config");
    }

    /**
     * @return config file exists
     */
    static Boolean configFileExists() {
        return Files.exists(configFilePath);
    }

    /**
     * Create a standard config file.
     *
     * @param appID  APP ID to be written.
     * @param appKey APP key to be written.
     */
    static void createConfigFile(String appID, String appKey) {

        String text = appID + System.lineSeparator() + appKey;

        try {
            // create one if not exists
            if (!configFileExists()) {
                Files.createDirectories(configFilePath.getParent());
                Files.createFile(configFilePath);
            }
            Files.write(configFilePath, text.getBytes());
        } catch (IOException e) {
            e.printStackTrace();

        }

    }

    /**
     * Read app_id and app_key config from ./config file.
     *
     * @return AppConfig object.
     */
    static AppConfig readConfigFile() {

        try {
            // read config file
            List<String> configs = Files.readAllLines(configFilePath);
            return new AppConfig(configs.get(0), configs.get(1));
        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            return null;
        }

    }

    /**
     * Display an error alert dialog.
     *
     * @param error error message to be displayed.
     */
    static void displayError(String error) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");

        // set no header area in the dialog
        alert.setHeaderText(null);
        alert.setContentText(error);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.showAndWait();

    }

    /**
     * Wrap the original recognised result with $$ ... $$.
     *
     * @param originalResult recognised result.
     * @return recognised result with $$ wrapped.
     */
    static String addDoubleDollarWrapper(String originalResult) {
        // return null if the original result is null
        return originalResult == null ? null : "$$\n " + originalResult + " \n$$";
    }

    /**
     * Wrap the original recognised result with \begin{equation} ... \end{equation}.
     *
     * @param originalResult recognised result.
     * @return recognised result with {equation} wrapped.
     */
    static String addEquationWrapper(String originalResult) {
        // return null if the original result is null
        return originalResult == null ? null : "\\begin{equation}\n " + originalResult + " \n\\end{equation}";
    }

}
