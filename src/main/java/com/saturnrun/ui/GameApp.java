package com.saturnrun.ui;

import com.saturnrun.game.GameController;
import com.saturnrun.game.StationGenerator;
import com.saturnrun.model.Station;
import com.saturnrun.model.StationType;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Path;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Vesperion: The Return - JavaFX game UI.
 * The supplied starfield and solar-system images are used as animated backdrops.
 */
public class GameApp extends Application {
    private double WIDTH = 1280;
    private double HEIGHT = 800;
    private Stage stage;
    private final Random rnd = new Random();
    private GameController game;

    private Label stationTitle;
    private Label questionLabel;
    private Label progressLabel;
    private Label wrongLabel;
    private Label locationLabel;
    private Label feedbackLabel;
    private Label resultIcon;
    private VBox solutionBox;
    private Button nextButton;
    private Button previousButton;
    private List<Button> answerButtons = new ArrayList<>();
    private Group routeRocket;
    private List<Circle> routeDots = new ArrayList<>();
    private boolean answered;
    private boolean introFinished;
    private Timeline introTimeline;
    private Timeline solutionTimeline;
    private ScrollPane gameScroll;
    private ScrollPane solutionScroll;
    private Label hintLabel;
    private Label conceptLabel;
    private Label formulaLabel;
    private Button hintButton;
    private int hintsUsed = 0;
    private static final int MAX_HINTS = 4;

    private static final String FONT_MAIN = "Trebuchet MS";
    private static final String FONT_STORY = "Georgia";
    private static final String FONT_MONO = "Consolas";

    private static final String NORMAL =
            "-fx-background-color: rgba(8,16,29,0.88); -fx-text-fill: #f3f6fb; " +
            "-fx-background-radius: 12; -fx-border-radius: 12; " +
            "-fx-border-color: rgba(154,190,230,0.45); -fx-border-width: 1.2; " +
            "-fx-padding: 13 22;";
    private static final String CORRECT =
            "-fx-background-color: #1f9d68; -fx-text-fill: white; -fx-background-radius: 12; " +
            "-fx-border-radius: 12; -fx-border-color: #b4ffe0; -fx-border-width: 1.8; -fx-padding: 13 22;";
    private static final String WRONG =
            "-fx-background-color: #a83f4a; -fx-text-fill: white; -fx-background-radius: 12; " +
            "-fx-border-radius: 12; -fx-border-color: #ffb7bf; -fx-border-width: 1.8; -fx-padding: 13 22;";

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        javafx.geometry.Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        WIDTH = bounds.getWidth();
        HEIGHT = bounds.getHeight();
        stage.setTitle("SpaceBound");
        stage.setFullScreenExitHint("Press ESC to exit full screen");
        playIntro();
        stage.setFullScreen(true);
        stage.show();
    }

    // ---------------------------------------------------------------------
    // IMAGE BACKGROUNDS + DYNAMIC STARFIELD
    // ---------------------------------------------------------------------

    private Image loadImage(String name) {
        // First try the normal JavaFX classpath location.
        var stream = getClass().getResourceAsStream("/assets/" + name);
        if (stream != null) {
            return new Image(stream);
        }

        // IntelliJ projects sometimes do not mark src/main/resources as a
        // resources root. In that case, fall back to the project resource path.
        try {
            java.nio.file.Path file = java.nio.file.Paths.get(
                    "src", "main", "resources", "assets", name);
            if (java.nio.file.Files.exists(file)) {
                return new Image(file.toUri().toString());
            }
        } catch (Exception ignored) {
            // handled by the final fallback below
        }

        // Never crash the game just because a background image is missing.
        // Return a tiny transparent image so the rest of the UI can start.
        System.err.println("WARNING: Could not load image: " + name);
        return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=");
    }

    private StackPane starBackground() {
        StackPane root = new StackPane();
        ImageView starsImage = new ImageView(loadImage("starfield.png"));
        starsImage.setFitWidth(WIDTH * 1.10);
        starsImage.setFitHeight(HEIGHT * 1.10);
        starsImage.setPreserveRatio(false);
        starsImage.setOpacity(0.78);
        root.getChildren().add(starsImage);

        TranslateTransition pan = new TranslateTransition(Duration.seconds(22), starsImage);
        pan.setFromX(-WIDTH * 0.04);
        pan.setToX(WIDTH * 0.04);
        pan.setFromY(-HEIGHT * 0.025);
        pan.setToY(HEIGHT * 0.025);
        pan.setAutoReverse(true);
        pan.setCycleCount(Animation.INDEFINITE);
        pan.setInterpolator(Interpolator.EASE_BOTH);
        pan.play();

        Pane stars = new Pane();
        stars.setMouseTransparent(true);
        for (int i = 0; i < 115; i++) {
            Circle s = new Circle(0.6 + rnd.nextDouble() * 1.5, Color.WHITE);
            s.setLayoutX(rnd.nextDouble() * WIDTH);
            s.setLayoutY(rnd.nextDouble() * HEIGHT);
            s.setOpacity(0.2 + rnd.nextDouble() * 0.75);
            stars.getChildren().add(s);

            FadeTransition blink = new FadeTransition(Duration.seconds(0.7 + rnd.nextDouble() * 2.2), s);
            blink.setFromValue(0.12);
            blink.setToValue(0.95);
            blink.setAutoReverse(true);
            blink.setCycleCount(Animation.INDEFINITE);
            blink.setDelay(Duration.seconds(rnd.nextDouble() * 2));
            blink.play();

            TranslateTransition drift = new TranslateTransition(Duration.seconds(15 + rnd.nextDouble() * 15), s);
            drift.setByX(15 + rnd.nextDouble() * 25);
            drift.setByY(-8 + rnd.nextDouble() * 16);
            drift.setAutoReverse(true);
            drift.setCycleCount(Animation.INDEFINITE);
            drift.play();
        }
        root.getChildren().add(stars);
        return root;
    }

    private StackPane solarBackground() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #0b111a;");

        // Keep the supplied solar-system artwork as a very dark atmospheric layer.
        // The original image remains recognizable, but it no longer washes the UI out.
        ImageView solar = new ImageView(loadImage("solar.jpg"));
        solar.setFitWidth(WIDTH);
        solar.setFitHeight(HEIGHT);
        solar.setPreserveRatio(false);
        solar.setOpacity(0.14);
        root.getChildren().add(solar);

        // Strong black space treatment for the question/briefing screens.
        Rectangle shade = new Rectangle(WIDTH, HEIGHT, Color.rgb(0, 0, 0, 0.72));
        root.getChildren().add(shade);

        addBlinkingStars(root, 85, 0.38);
        addOrbitingPlanets(root);
        return root;
    }


    private void addBlinkingStars(Pane parent, int count, double opacityLimit) {
        Pane stars = new Pane();
        stars.setMouseTransparent(true);

        for (int i = 0; i < count; i++) {
            double radius = 0.55 + rnd.nextDouble() * 1.85;
            Circle s = new Circle(radius, Color.WHITE);
            s.setLayoutX(12 + rnd.nextDouble() * Math.max(1, WIDTH - 24));
            s.setLayoutY(12 + rnd.nextDouble() * Math.max(1, HEIGHT - 24));
            s.setOpacity(0.18 + rnd.nextDouble() * opacityLimit);
            stars.getChildren().add(s);

            if (i % 5 == 0) {
                FadeTransition blink = new FadeTransition(
                        Duration.seconds(1.8 + rnd.nextDouble() * 2.8), s);
                blink.setFromValue(0.16);
                blink.setToValue(0.48);
                blink.setAutoReverse(true);
                blink.setCycleCount(Animation.INDEFINITE);
                blink.setDelay(Duration.seconds(rnd.nextDouble() * 1.4));
                blink.play();
            }

            TranslateTransition drift = new TranslateTransition(
                    Duration.seconds(18 + rnd.nextDouble() * 18), s);
            drift.setByX(8 + rnd.nextDouble() * 20);
            drift.setByY(-5 + rnd.nextDouble() * 10);
            drift.setAutoReverse(true);
            drift.setCycleCount(Animation.INDEFINITE);
            drift.play();
        }
        parent.getChildren().add(stars);
    }

    private void addOrbitingPlanets(Pane parent) {
        Group system = new Group();
        double cx = WIDTH * 0.51;
        double cy = HEIGHT * 0.50;

        // Very slow, uniform orbital motion. Every planet completes one orbit in six minutes,
        // so the background feels almost still while the motion remains visible.
        double[] rx = {
                WIDTH * 0.075, WIDTH * 0.115, WIDTH * 0.16, WIDTH * 0.205,
                WIDTH * 0.255, WIDTH * 0.31, WIDTH * 0.37, WIDTH * 0.43
        };
        double[] ry = {
                HEIGHT * 0.055, HEIGHT * 0.085, HEIGHT * 0.12, HEIGHT * 0.155,
                HEIGHT * 0.20, HEIGHT * 0.25, HEIGHT * 0.30, HEIGHT * 0.35
        };
        // Every planet moves at the same extremely slow, uniform speed.
        // One complete orbit takes six minutes.
        double[] durations = {600, 600, 600, 600, 600, 600, 600, 600};
        double[] sizes = {14, 14, 14, 14, 14, 14, 14, 14};
        double[] starts = {0.35, 1.8, 2.9, 4.4, 5.6, 0.9, 3.4, 5.0};
        Color[] colors = {
                Color.web("#b9c7d6"), Color.web("#d9c79b"),
                Color.web("#6e9fd1"), Color.web("#c48868"),
                Color.web("#8cb8e2"), Color.web("#d6c49b"),
                Color.web("#c6d9ec"), Color.web("#91a9c3")
        };

        for (int i = 0; i < rx.length; i++) {
            Ellipse orbitLine = new Ellipse(cx, cy, rx[i], ry[i]);
            orbitLine.setFill(Color.TRANSPARENT);
            orbitLine.setStroke(Color.rgb(205, 225, 245, 0.15));
            orbitLine.setStrokeWidth(0.9);
            system.getChildren().add(orbitLine);

            Group planetNode = new Group();
            Circle planet = new Circle(sizes[i], new RadialGradient(
                    0, 0, 0.30, 0.24, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#ffffff")),
                    new Stop(0.16, colors[i].deriveColor(0, 1, 0.82, 1)),
                    new Stop(1, colors[i].darker().darker())));
            planet.setOpacity(0.96);
            planet.setEffect(new DropShadow(9, colors[i].deriveColor(0, 1, 1, 0.24)));
            planetNode.getChildren().add(planet);

            if (i == 5 || i == 7) {
                Ellipse ringBack = new Ellipse(0, 0, sizes[i] * 1.85, sizes[i] * 0.60);
                ringBack.setFill(Color.TRANSPARENT);
                ringBack.setStroke(Color.rgb(220, 228, 242, 0.58));
                ringBack.setStrokeWidth(Math.max(1.1, sizes[i] * 0.07));
                ringBack.setRotate(16);
                Ellipse ringFront = new Ellipse(0, 0, sizes[i] * 1.55, sizes[i] * 0.46);
                ringFront.setFill(Color.TRANSPARENT);
                ringFront.setStroke(Color.rgb(220, 228, 242, 0.36));
                ringFront.setStrokeWidth(Math.max(0.8, sizes[i] * 0.045));
                ringFront.setRotate(16);
                planetNode.getChildren().add(0, ringBack);
                planetNode.getChildren().add(2, ringFront);
            }

            double startAngle = starts[i];
            planetNode.setLayoutX(cx + rx[i] * Math.cos(startAngle));
            planetNode.setLayoutY(cy + ry[i] * Math.sin(startAngle));
            system.getChildren().add(planetNode);

            Timeline orbitMotion = new Timeline();
            int steps = 180;
            for (int step = 0; step <= steps; step++) {
                double t = (Math.PI * 2.0 * step) / steps + startAngle;
                double x = cx + rx[i] * Math.cos(t);
                double y = cy + ry[i] * Math.sin(t);
                Duration d = Duration.seconds(durations[i] * step / steps);
                orbitMotion.getKeyFrames().add(new KeyFrame(d,
                        new KeyValue(planetNode.layoutXProperty(), x, Interpolator.LINEAR),
                        new KeyValue(planetNode.layoutYProperty(), y, Interpolator.LINEAR)));
            }
            orbitMotion.setCycleCount(Animation.INDEFINITE);
            orbitMotion.play();

            RotateTransition selfSpin = new RotateTransition(
                    Duration.seconds(45), planetNode);
            selfSpin.setByAngle(360);
            selfSpin.setCycleCount(Animation.INDEFINITE);
            selfSpin.setInterpolator(Interpolator.LINEAR);
            selfSpin.play();
        }

        Circle coreGlow = new Circle(8, Color.web("#ffe7a5"));
        coreGlow.setLayoutX(cx);
        coreGlow.setLayoutY(cy);
        coreGlow.setEffect(new DropShadow(28, Color.web("#ffd36f")));
        system.getChildren().add(coreGlow);
        parent.getChildren().add(system);
    }


    // ---------------------------------------------------------------------
    // INTRO CINEMATIC - 5 seconds, ENTER skips it
    // ---------------------------------------------------------------------

    private void playIntro() {
        introFinished = false;
        if (introTimeline != null) introTimeline.stop();

        // Intro is intentionally a clean black cinematic, matching the
        // supplied reference: no solar-system background and no typewriter.
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: black;");

        Pane scene = new Pane();
        root.getChildren().add(scene);

        double earthX = WIDTH * 0.16;
        double vesperionX = WIDTH * 0.50;
        double aetheonX = WIDTH * 0.82;
        double routeY = HEIGHT * 0.50;

        Circle earth = planet(58, "#4fa8e8", "#123b72");
        earth.setLayoutX(earthX);
        earth.setLayoutY(routeY);
        scene.getChildren().add(earth);
        labelAt(scene, "EARTH", earthX - 24, routeY + 78, 13, "#bcd7f3");

        Circle vesperion = planet(72, "#6b9fba", "#182b4e");
        vesperion.setLayoutX(vesperionX);
        vesperion.setLayoutY(routeY);
        vesperion.setEffect(new DropShadow(34, Color.web("#4e9fd0")));
        scene.getChildren().add(vesperion);
        labelAt(scene, "VESPERION", vesperionX - 43, routeY + 94, 14, "#c4e7f6");

        // Atherion-7 is shown as the intended destination, but the rocket
        // never reaches it because the failure happens en route to Vesperion.
        Circle aetheon = planet(34, "#bd876d", "#4a2524");
        aetheon.setLayoutX(aetheonX);
        aetheon.setLayoutY(routeY);
        scene.getChildren().add(aetheon);
        labelAt(scene, "ATHERION-7", aetheonX - 50, routeY + 58, 13, "#d8b7aa");

        Line pathToVesperion = new Line(earthX + 70, routeY, vesperionX - 62, routeY);
        pathToVesperion.setStroke(Color.rgb(160, 194, 226, 0.28));
        pathToVesperion.getStrokeDashArray().addAll(8.0, 12.0);
        scene.getChildren().add(pathToVesperion);

        Group rocket = rocketGraphic();
        rocket.setLayoutX(earthX + 70);
        rocket.setLayoutY(routeY);
        rocket.setRotate(90); // Nose points right: Earth -> Vesperion.
        scene.getChildren().add(rocket);

        Label lost = new Label("Lost in the Void");
        lost.setFont(Font.font(FONT_STORY, FontWeight.NORMAL, 30));
        lost.setTextFill(Color.web("#e9f2fb"));
        lost.setEffect(new DropShadow(18, Color.rgb(0, 0, 0, 0.95)));
        StackPane.setAlignment(lost, Pos.BOTTOM_LEFT);
        StackPane.setMargin(lost, new Insets(0, 0, 58, 72));
        root.getChildren().add(lost);

        Label skip = new Label("PRESS ENTER TO SKIP INTRO");
        skip.setFont(Font.font(FONT_MONO, FontWeight.BOLD, 12));
        skip.setTextFill(Color.web("#7894b2"));
        StackPane.setAlignment(skip, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(skip, new Insets(0, 28, 24, 0));
        root.getChildren().add(skip);

        Scene introScene = new Scene(root, WIDTH, HEIGHT);
        introScene.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER")) {
                if (introTimeline != null) introTimeline.stop();
                showMissionBriefing();
            }
        });
        stage.setScene(introScene);
        stage.setFullScreen(true);

        double startX = earthX + 70;
        double stopX = vesperionX - 62;

        TranslateTransition flight = new TranslateTransition(Duration.seconds(3.5), rocket);
        flight.setToX(stopX - startX);
        flight.setInterpolator(Interpolator.EASE_BOTH);
        flight.setOnFinished(e -> {
            if (introFinished) return;

            Timeline fuelCut = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(rocket.opacityProperty(), 1.0),
                            new KeyValue(rocket.rotateProperty(), 90)),
                    new KeyFrame(Duration.millis(100),
                            new KeyValue(rocket.opacityProperty(), 0.18),
                            new KeyValue(rocket.rotateProperty(), 87)),
                    new KeyFrame(Duration.millis(220),
                            new KeyValue(rocket.opacityProperty(), 0.95),
                            new KeyValue(rocket.rotateProperty(), 93)),
                    new KeyFrame(Duration.millis(360),
                            new KeyValue(rocket.opacityProperty(), 0.35),
                            new KeyValue(rocket.rotateProperty(), 88)),
                    new KeyFrame(Duration.millis(500),
                            new KeyValue(rocket.opacityProperty(), 0.75),
                            new KeyValue(rocket.rotateProperty(), 90))
            );
            fuelCut.setOnFinished(done -> {
                if (introFinished) return;
                rocket.setOpacity(0.78);
                PauseTransition stranded = new PauseTransition(Duration.millis(1000));
                stranded.setOnFinished(x -> {
                    if (!introFinished) showMissionBriefing();
                });
                stranded.play();
            });
            fuelCut.play();
        });

        // Total cinematic time: ~5 seconds.
        introTimeline = new Timeline(
                new KeyFrame(Duration.ZERO),
                new KeyFrame(Duration.seconds(5.0), e -> {
                    if (!introFinished) showMissionBriefing();
                })
        );
        introTimeline.play();
        flight.play();
    }

    private void showMissionBriefing() {
        if (introFinished) return;
        introFinished = true;

        StackPane bg = solarBackground();
        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(650);
        // A StackPane stretches a VBox to the full screen height by default.
        // Locking max height to the preferred height makes the box start just
        // above the title and end just below the BEGIN MISSION button.
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setPadding(new Insets(22, 40, 24, 40));
        card.setStyle("-fx-background-color: rgba(3,10,20,0.55); -fx-background-radius: 24; -fx-border-color: rgba(202,225,255,0.25); -fx-border-radius: 24;");

        Label title = new Label("SpaceBound");
        title.setFont(Font.font(FONT_MAIN, FontWeight.BOLD, 38));
        title.setTextFill(Color.web("#f2f6fc"));
        title.setEffect(new DropShadow(22, Color.web("#62a9df")));

        Label story = new Label(
                "Your research flight left Earth for Atherion-7. During the journey, a fuel-system glitch disabled the ship.\n\n" +
                "You were forced to land on Vesperion, between Earth and Atherion-7. The return route has 12 math locks. Solve questions correctly to collect fuel; 8 correct answers provide enough fuel for the journey to Earth.\n" +
                "A correct answer moves the rocket forward. A wrong answer keeps it where it is. Solve 8 locks to restart the return engine. Four wrong answers end the mission."
        );
        story.setFont(Font.font(FONT_STORY, 16));
        story.setTextFill(Color.web("#dce7f4"));
        story.setWrapText(true);
        story.setTextAlignment(TextAlignment.CENTER);

        Button begin = new Button("BEGIN MISSION");
        begin.setFont(Font.font(FONT_MAIN, FontWeight.BOLD, 18));
        begin.setStyle("-fx-background-color: #2d78d0; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 13 34;");
        begin.setOnAction(e -> playMissionLaunch(begin));

        card.getChildren().addAll(title, story, begin);
        bg.getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);

        switchScene(bg);
    }

    private void playMissionLaunch(Button button) {
        button.setDisable(true);
        Pane overlay = new Pane();
        overlay.setMouseTransparent(true);
        Group rocket = rocketGraphic();
        rocket.setScaleX(0.72); rocket.setScaleY(0.72);
        rocket.setRotate(45); // points along the fast bottom-left -> top-right launch path
        rocket.setLayoutX(70); rocket.setLayoutY(HEIGHT - 70);
        overlay.getChildren().add(rocket);

        Rectangle streak = new Rectangle(8, 170, Color.web("#f7c96c", 0.45));
        streak.setRotate(42);
        streak.setLayoutX(95); streak.setLayoutY(HEIGHT - 210);
        overlay.getChildren().add(streak);
        ((StackPane) stage.getScene().getRoot()).getChildren().add(overlay);

        TranslateTransition dash = new TranslateTransition(Duration.millis(900), rocket);
        dash.setToX(WIDTH - 130);
        dash.setToY(-HEIGHT + 120);
        dash.setInterpolator(Interpolator.EASE_IN);
        ScaleTransition zoom = new ScaleTransition(Duration.millis(900), rocket);
        zoom.setToX(1.2); zoom.setToY(1.2);
        ParallelTransition move = new ParallelTransition(dash, zoom);
        move.setOnFinished(e -> {
            overlay.getChildren().clear();
            Label pop = new Label("ROUTE LOCKED\n12 MATH LOCKS ONLINE");
            pop.setFont(Font.font(FONT_MONO, FontWeight.BOLD, 22));
            pop.setTextFill(Color.web("#bce6ff"));
            pop.setTextAlignment(TextAlignment.CENTER);
            pop.setStyle("-fx-background-color: rgba(3,11,22,0.92); -fx-background-radius: 18; -fx-padding: 22 34; -fx-border-color: #5da6df; -fx-border-radius: 18;");
            pop.setLayoutX(WIDTH / 2 - 180); pop.setLayoutY(HEIGHT / 2 - 55);
            overlay.getChildren().add(pop);
            FadeTransition f = new FadeTransition(Duration.millis(650), pop);
            f.setFromValue(0); f.setToValue(1);
            f.setOnFinished(x -> {
                PauseTransition p = new PauseTransition(Duration.millis(420));
                p.setOnFinished(y -> startNewGame());
                p.play();
            });
            f.play();
        });
        move.play();
    }

    // ---------------------------------------------------------------------
    // GAME ROUTE + QUESTIONS
    // ---------------------------------------------------------------------

    private Scene buildGameScene() {
        StackPane root = solarBackground();

        // Fixed header + route. The question card is centered on the screen;
        // the learning aid lives completely outside it on the right side.
        VBox fixedTop = new VBox(8);
        fixedTop.setAlignment(Pos.TOP_CENTER);
        fixedTop.setPadding(new Insets(12, WIDTH * 0.05, 8, WIDTH * 0.05));

        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        Label gameTitle = new Label("SpaceBound");
        gameTitle.setFont(Font.font(FONT_MAIN, FontWeight.BOLD, 24));
        gameTitle.setTextFill(Color.web("#e9f2fb"));
        locationLabel = new Label("RETURN ROUTE • 12 MATH LOCKS");
        locationLabel.setFont(Font.font(FONT_MONO, FontWeight.BOLD, 11));
        locationLabel.setTextFill(Color.web("#82b9e5"));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        progressLabel = new Label();
        progressLabel.setFont(Font.font(FONT_MONO, FontWeight.BOLD, 13));
        progressLabel.setTextFill(Color.web("#8fe2b4"));
        wrongLabel = new Label();
        wrongLabel.setFont(Font.font(FONT_MONO, FontWeight.BOLD, 13));
        wrongLabel.setTextFill(Color.web("#ef9b9b"));
        header.getChildren().addAll(gameTitle, locationLabel, spacer, progressLabel, wrongLabel);

        Pane route = buildRouteBand();
        fixedTop.getChildren().addAll(header, route);

        VBox questionCard = new VBox(13);
        questionCard.setAlignment(Pos.TOP_CENTER);
        questionCard.setMaxWidth(800);
        // Let the card hug its content before an answer. When a solution is
        // revealed, it grows only enough to contain the fixed solution area.
        questionCard.setMaxHeight(Region.USE_PREF_SIZE);
        questionCard.setPadding(new Insets(20, 30, 18, 30));
        questionCard.setStyle(
                "-fx-background-color: rgba(0,0,0,0.94); -fx-background-radius: 22; " +
                "-fx-border-color: rgba(202,225,255,0.34); -fx-border-radius: 22; -fx-border-width: 1.0;");

        stationTitle = new Label();
        stationTitle.setFont(Font.font(FONT_MAIN, FontWeight.BOLD, 18));
        stationTitle.setTextFill(Color.web("#a8d2f5"));

        questionLabel = new Label();
        questionLabel.setFont(Font.font(FONT_STORY, FontWeight.NORMAL, 18));
        questionLabel.setTextFill(Color.web("#f5f8fc"));
        questionLabel.setEffect(new DropShadow(7, Color.rgb(0, 0, 0, 0.95)));
        questionLabel.setWrapText(true);
        questionLabel.setTextAlignment(TextAlignment.CENTER);
        questionLabel.setMaxWidth(740);
        questionLabel.setMinHeight(Region.USE_PREF_SIZE);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(14); grid.setVgap(12);
        answerButtons.clear();
        for (int i = 0; i < 4; i++) {
            Button b = new Button();
            b.setMinSize(300, 54); b.setMaxSize(300, 54);
            b.setFont(Font.font(FONT_MAIN, FontWeight.BOLD, 16));
            b.setStyle(NORMAL);
            final int index = i;
            b.setOnAction(e -> answer(index));
            answerButtons.add(b);
            grid.add(b, i % 2, i / 2);
        }

        resultIcon = new Label();
        resultIcon.setFont(Font.font(FONT_MAIN, FontWeight.BOLD, 30));
        resultIcon.setVisible(false); resultIcon.setManaged(false);

        feedbackLabel = new Label();
        feedbackLabel.setFont(Font.font(FONT_MAIN, FontWeight.BOLD, 13));
        feedbackLabel.setWrapText(true);
        feedbackLabel.setTextAlignment(TextAlignment.CENTER);
        feedbackLabel.setMaxWidth(760);

        solutionBox = new VBox(4);
        solutionBox.setMaxWidth(690);
        solutionBox.setPadding(new Insets(4, 8, 4, 8));
        solutionScroll = new ScrollPane(solutionBox);
        solutionScroll.setFitToWidth(true);
        solutionScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        solutionScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        solutionScroll.setPrefViewportHeight(125);
        solutionScroll.setMinViewportHeight(125);
        solutionScroll.setMaxWidth(710);
        solutionScroll.setVisible(false);
        solutionScroll.setManaged(false);
        solutionScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent; -fx-padding: 0;");

        previousButton = new Button("PREVIOUS");
        previousButton.setFont(Font.font(FONT_MAIN, FontWeight.BOLD, 11));
        previousButton.setStyle("-fx-background-color: rgba(25,42,61,0.96); -fx-text-fill: #c6d8e8; -fx-background-radius: 9; -fx-padding: 8 18;");
        previousButton.setOnAction(e -> navigatePrevious());

        nextButton = new Button("NEXT");
        nextButton.setFont(Font.font(FONT_MAIN, FontWeight.BOLD, 11));
        nextButton.setStyle("-fx-background-color: #2d78d0; -fx-text-fill: white; -fx-background-radius: 9; -fx-padding: 8 22;");
        nextButton.setOnAction(e -> navigateNext());

        HBox navRow = new HBox(12, previousButton, nextButton);
        navRow.setAlignment(Pos.CENTER);
        questionCard.getChildren().addAll(stationTitle, questionLabel, grid, resultIcon, feedbackLabel, solutionScroll, navRow);

        // Completely separate right-side learning panel. It never overlaps the question card.
        VBox infoBox = new VBox(8);
        infoBox.setAlignment(Pos.TOP_LEFT);
        infoBox.setPrefWidth(300);
        infoBox.setMaxWidth(300);
        // Do not let StackPane stretch this panel to the full play area.
        // Its bottom should finish shortly after the hint controls.
        infoBox.setMaxHeight(Region.USE_PREF_SIZE);
        infoBox.setPadding(new Insets(15, 16, 15, 16));
        infoBox.setStyle(
                "-fx-background-color: #091c2d; -fx-background-radius: 14; " +
                "-fx-border-color: #3f87b8; -fx-border-radius: 14; -fx-border-width: 1.4; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 10, 0.2, 0, 3);");

        Label infoTitle = new Label("CONCEPT + FORMULA");
        infoTitle.setFont(Font.font(FONT_MONO, FontWeight.BOLD, 12));
        infoTitle.setTextFill(Color.web("#a9ddff"));
        conceptLabel = new Label();
        conceptLabel.setWrapText(true);
        conceptLabel.setFont(Font.font(FONT_MAIN, FontWeight.NORMAL, 12));
        conceptLabel.setMaxHeight(Region.USE_PREF_SIZE);
        conceptLabel.setTextFill(Color.web("#e2edf6"));
        formulaLabel = new Label();
        formulaLabel.setWrapText(true);
        formulaLabel.setFont(Font.font(FONT_MONO, FontWeight.BOLD, 12));
        formulaLabel.setTextFill(Color.web("#c7e8ff"));
        formulaLabel.setMaxHeight(Region.USE_PREF_SIZE);
        hintButton = new Button("HINT (0/4)");
        hintButton.setFont(Font.font(FONT_MAIN, FontWeight.BOLD, 11));
        hintButton.setStyle("-fx-background-color: #1d5f91; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 7 14;");
        hintButton.setOnAction(e -> showHint());
        hintLabel = new Label();
        hintLabel.setWrapText(true);
        hintLabel.setFont(Font.font(FONT_MAIN, FontWeight.NORMAL, 11));
        hintLabel.setTextFill(Color.web("#ffd98a"));
        hintLabel.setVisible(false); hintLabel.setManaged(false);
        infoBox.getChildren().addAll(infoTitle, conceptLabel, formulaLabel, new Separator(), hintButton, hintLabel);

        StackPane playArea = new StackPane(questionCard, infoBox);
        StackPane.setAlignment(questionCard, Pos.CENTER);
        // Lift the centered question card slightly and keep a clear horizontal gap
        // from the right-side concept panel.
        questionCard.setTranslateY(-100);
        StackPane.setAlignment(infoBox, Pos.TOP_RIGHT);
        StackPane.setMargin(infoBox, new Insets(0, 24, 0, 55));
        VBox.setVgrow(playArea, Priority.ALWAYS);
        root.getChildren().add(new VBox(fixedTop, playArea));
        return new Scene(root, WIDTH, HEIGHT);

    }

    private Pane buildRouteBand() {
        Pane band = new Pane();
        band.setPrefHeight(125);
        band.setMaxWidth(WIDTH * 0.90);

        Line line = new Line(95, 60, WIDTH * 0.90 - 95, 60);
        line.setStroke(Color.rgb(155, 193, 224, 0.65));
        line.setStrokeWidth(2.0);
        band.getChildren().add(line);

        labelAt(band, "VESPERION", 45, 78, 11, "#b8dff4");
        labelAt(band, "EARTH", WIDTH * 0.90 - 55, 78, 11, "#b8dff4");

        routeDots.clear();
        double left = 125, right = WIDTH * 0.90 - 125;
        for (int i = 0; i < GameController.TOTAL_STATIONS; i++) {
            double x = left + (right - left) * i / (GameController.TOTAL_STATIONS - 1);
            Circle dot = new Circle(x, 60, 7, Color.web(i == 0 ? "#69c4f2" : "#526b83"));
            dot.setStroke(Color.web("#c9e7fa", 0.55));
            routeDots.add(dot);
            band.getChildren().add(dot);
            labelAt(band, String.valueOf(i + 1), x - 4, 35, 9, "#8da4bb");
        }

        routeRocket = rocketGraphic();
        routeRocket.setScaleX(0.34); routeRocket.setScaleY(0.34);
        routeRocket.setLayoutX(left);
        routeRocket.setLayoutY(57);
        band.getChildren().add(routeRocket);
        return band;
    }

    private void startNewGame() {
        hintsUsed = 0;
        game = new GameController(new StationGenerator());
        Scene scene = buildGameScene();
        stage.setScene(scene);
        FadeTransition fade = new FadeTransition(Duration.millis(350), scene.getRoot());
        scene.getRoot().setOpacity(0);
        fade.setToValue(1);
        fade.play();
        loadStation();
    }

    private void loadStation() {
        Station s = game.getCurrentStation();
        if (s == null) return;
        answered = game.isCurrentQuestionSolved();
        stationTitle.setText("MATH LOCK  •  " + s.type + "  •  " + s.difficulty.toUpperCase());
        locationLabel.setText("RETURN ROUTE • 12 MATH LOCKS");
        questionLabel.setText(s.flavorText);
        setLearningAid(s, conceptLabel, formulaLabel);
        progressLabel.setText("FUEL  " + game.getCorrectAnswers() + " / 8");
        wrongLabel.setText("WRONG  " + game.getWrongAnswers() + " / 4");
        feedbackLabel.setText(answered ? "Solved correctly — this lock has already added fuel." : "Choose an answer. Only a correct answer moves the rocket.");
        feedbackLabel.setTextFill(answered ? Color.web("#8fe8b4") : Color.web("#b7cee3"));
        if (solutionTimeline != null) solutionTimeline.stop();
        solutionBox.getChildren().clear();
        solutionScroll.setVisible(false); solutionScroll.setManaged(false); solutionScroll.setVvalue(0);
        resultIcon.setVisible(false); resultIcon.setManaged(false);
        for (int i = 0; i < 4; i++) {
            Button b = answerButtons.get(i);
            b.setText(String.valueOf(s.options.get(i)));
            b.setDisable(answered || game.isGameOver());
            b.setStyle(NORMAL);
            if (answered && i == s.correctOptionIndex) b.setStyle(CORRECT);
        }
        if (hintLabel != null) { hintLabel.setText(""); hintLabel.setVisible(false); hintLabel.setManaged(false); }
        hintButton.setText(hintsUsed >= MAX_HINTS ? "HINTS USED (4/4)" : "HINT (" + hintsUsed + "/4)");
        hintButton.setDisable(hintsUsed >= MAX_HINTS || answered || game.isGameOver());
        previousButton.setDisable(game.getCurrentStationIndex() <= 0);
        nextButton.setDisable(game.getCurrentStationIndex() >= game.getTotalStations() - 1 && !game.isGameOver());
        nextButton.setText(game.isGameWon() ? "FLY TO EARTH" : "NEXT");
        if (game.isGameOver() && !game.isGameWon()) nextButton.setText("MISSION RESULT");
    }

    private void answer(int selected) {
        if (game.isGameOver() || game.isCurrentQuestionSolved()) return;
        Station s = game.getCurrentStation();
        long chosen = s.options.get(selected);
        boolean correct = game.submitAnswer(chosen);

        for (int i = 0; i < answerButtons.size(); i++) {
            Button b = answerButtons.get(i);
            if (correct || i == selected) b.setDisable(correct || i == selected);
            if (i == s.correctOptionIndex) b.setStyle(CORRECT);
            if (i == selected && !correct) b.setStyle(WRONG);
        }

        resultIcon.setText(correct ? "✓" : "✕");
        resultIcon.setTextFill(correct ? Color.web("#8fe8b4") : Color.web("#ff9696"));
        resultIcon.setVisible(true); resultIcon.setManaged(true);
        feedbackLabel.setText(correct
                ? (game.isGameWon() ? "Correct! You collected enough fuel to reach Earth." : "Correct! Fuel collected. The rocket moves forward.")
                : "Wrong answer. The rocket stays where it is. You can retry this question or use Previous / Next.");
        feedbackLabel.setTextFill(correct ? Color.web("#8fe8b4") : Color.web("#ff9b9b"));
        progressLabel.setText("FUEL  " + game.getCorrectAnswers() + " / 8");
        wrongLabel.setText("WRONG  " + game.getWrongAnswers() + " / 4");

        solutionBox.getChildren().clear();
        solutionBox.setStyle("-fx-background-color: rgba(7,18,30,0.97); -fx-background-radius: 10; -fx-border-color: rgba(154,190,230,0.35); -fx-border-radius: 10; -fx-padding: 7 10 8 10;");
        Label solutionTitle = new Label("Step-by-step solution");
        solutionTitle.setFont(Font.font(FONT_MAIN, FontWeight.BOLD, 11));
        solutionTitle.setTextFill(Color.web("#9fc7e8"));
        solutionBox.getChildren().add(solutionTitle);
        solutionScroll.setVisible(true); solutionScroll.setManaged(true); solutionScroll.setVvalue(0);

        if (correct) {
            moveRouteRocketToFuel(game.getCorrectAnswers(), () -> {});
            if (game.isGameWon()) nextButton.setText("FLY TO EARTH");
        }

        if (solutionTimeline != null) solutionTimeline.stop();
        solutionTimeline = new Timeline();
        int count = 0;
        for (String step : s.derivationSteps) {
            count++;
            final String reveal = step;
            solutionTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(380 * count), ev -> addSolutionStep(reveal)));
        }
        solutionTimeline.play();
    }

    private void navigatePrevious() {
        if (game.previousQuestion()) loadStation();
    }

    private void navigateNext() {
        if (game.isGameWon()) { flyDirectToEarth(); return; }
        if (game.isGameOver()) { playFailureCinematic(); return; }
        if (game.nextQuestion()) loadStation();
    }

    private void addSolutionStep(String step) {
        Label line = new Label("\u2022 " + step);
        line.setFont(Font.font(FONT_MAIN, 11));
        line.setTextFill(Color.web("#d3deea"));
        line.setWrapText(true);
        line.setMaxWidth(660);
        line.setMinHeight(Region.USE_PREF_SIZE); // never truncate with "..."
        line.setOpacity(0);
        solutionBox.getChildren().add(line);
        FadeTransition f = new FadeTransition(Duration.millis(260), line);
        f.setToValue(1);
        f.play();
        scrollToBottom();
    }

    private void scrollToBottom() {
        if (solutionScroll == null) return;
        Platform.runLater(() -> {
            solutionScroll.applyCss();
            solutionScroll.layout();
            solutionScroll.setVvalue(1.0);
        });
    }

    private void setLearningAid(Station s, Label conceptLabel, Label formulaLabel) {
        switch (s.type) {
            case PERMUTATION:
                conceptLabel.setText("Use permutation when ORDER matters. You are arranging r items chosen from n.");
                formulaLabel.setText("nPr = n! / (n - r)!");
                break;
            case COMBINATION:
                conceptLabel.setText("Use combination when ORDER does not matter. You are choosing r items from n.");
                formulaLabel.setText("nCr = n! / [r! × (n - r)!]");
                break;
            case PIGEONHOLE:
                conceptLabel.setText("If more items go into fewer boxes, some box must contain multiple items.");
                formulaLabel.setText("Guaranteed minimum = ceil(N / M)");
                break;
            case RECURRENCE:
                conceptLabel.setText("Build each term from the previous term(s), starting with the given base value(s).");
                formulaLabel.setText("Substitute f(n-1), f(n-2), ... step-by-step");
                break;
            default:
                conceptLabel.setText("Identify the mathematical rule and substitute the given values carefully.");
                formulaLabel.setText("Read the given formula before calculating.");
        }
    }

    private void showHint() {
        if (game == null || game.isGameOver() || answered || hintsUsed >= MAX_HINTS) return;
        Station s = game.getCurrentStation();
        if (s == null) return;

        hintsUsed++;
        String hint;
        switch (s.type) {
            case PERMUTATION:
                hint = "Hint: Because order matters, use nPr. Put the question's n and r into n! / (n-r)!.";
                break;
            case COMBINATION:
                hint = "Hint: Because order does not matter, use nCr. Calculate n!, r!, and (n-r)! first.";
                break;
            case PIGEONHOLE:
                hint = "Hint: Divide the number of items by the number of boxes and round UP (ceiling).";
                break;
            case RECURRENCE:
                hint = "Hint: Start from the given base value(s), calculate the next term, then use that term to get the next one.";
                break;
            default:
                hint = "Hint: Write down the given values and apply the displayed formula step-by-step.";
        }

        hintLabel.setText(hint);
        hintLabel.setVisible(true);
        hintLabel.setManaged(true);
        hintButton.setText(hintsUsed >= MAX_HINTS ? "HINTS USED (4/4)" : "HINT (" + hintsUsed + "/4)");
        if (hintsUsed >= MAX_HINTS) hintButton.setDisable(true);
    }

    private void typewriter(Label label, String text) {
        label.setText("");
        Timeline typing = new Timeline();
        for (int i = 1; i <= text.length(); i++) {
            final int end = i;
            typing.getKeyFrames().add(new KeyFrame(Duration.millis(24L * i), e -> label.setText(text.substring(0, end))));
        }
        typing.play();
    }

    private void continueAfterAnswer() { navigateNext(); }

    private void moveRouteRocketToFuel(int fuel, Runnable done) {
        int index = Math.min(Math.max(fuel, 0), routeDots.size() - 1);
        Circle target = routeDots.get(index);
        TranslateTransition move = new TranslateTransition(Duration.millis(700), routeRocket);
        double left = routeDots.get(0).getCenterX();
        move.setToX(target.getCenterX() - left);
        move.setInterpolator(Interpolator.EASE_BOTH);
        move.setOnFinished(e -> {
            target.setFill(Color.web("#70d8ff"));
            done.run();
        });
        move.play();
    }

    private void flyDirectToEarth() {
        // The route marker reaches Earth first; the cinematic then performs the
        // slower circular burns and the final curved approach.
        int earthIndex = routeDots.size() - 1;
        Circle target = routeDots.get(earthIndex);
        TranslateTransition move = new TranslateTransition(Duration.seconds(0.75), routeRocket);
        double left = routeDots.get(0).getCenterX();
        move.setToX(target.getCenterX() - left);
        move.setInterpolator(Interpolator.EASE_IN);
        move.setOnFinished(e -> playSuccessCinematic());
        move.play();
    }


    // ---------------------------------------------------------------------
    // SUCCESS / FAILURE CINEMATICS - retained as a stronger finish sequence
    // ---------------------------------------------------------------------

    private void playSuccessCinematic() {
        // Pure black space background for the final cinematic.
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #010309;");
        Pane scene = new Pane();
        root.getChildren().add(scene);

        // Vesperion stays behind while the ship performs two slow circular
        // burns before committing to the curved Earth approach.
        Circle vesperion = planet(72, "#6b9fba", "#182b4e");
        double vx = WIDTH * 0.16;
        double vy = HEIGHT * 0.52;
        vesperion.setLayoutX(vx);
        vesperion.setLayoutY(vy);
        scene.getChildren().add(vesperion);

        Circle earth = planet(78, "#4fa8e8", "#123b72");
        double ex = WIDTH * 0.84;
        double ey = HEIGHT * 0.52;
        earth.setLayoutX(ex);
        earth.setLayoutY(ey);
        scene.getChildren().add(earth);

        labelAt(scene, "VESPERION", vx - 42, vy + 92, 14, "#c3e7f7");
        labelAt(scene, "EARTH", ex - 25, ey + 103, 14, "#c3e7f7");

        Group orbit = new Group();
        orbit.setLayoutX(vx);
        orbit.setLayoutY(vy);
        scene.getChildren().add(orbit);

        Group rocket = rocketGraphic();
        rocket.setScaleX(0.80);
        rocket.setScaleY(0.80);
        // Start just to the right of Vesperion. RocketGraphic points upward,
        // so +90 degrees makes the nose point to the right.
        double radius = 125;
        rocket.setLayoutX(radius);
        rocket.setLayoutY(0);
        rocket.setRotate(180);
        orbit.getChildren().add(rocket);

        Label title = new Label("RETURN BURN");
        title.setFont(Font.font(FONT_MONO, FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#9dc8e9"));
        title.setLayoutX(WIDTH / 2 - 65);
        title.setLayoutY(35);
        scene.getChildren().add(title);

        RotateTransition orbitBurn = new RotateTransition(Duration.seconds(1.5), orbit);
        orbitBurn.setByAngle(720);
        orbitBurn.setInterpolator(Interpolator.EASE_BOTH);

        orbitBurn.setOnFinished(e -> {
            // Remove the orbit parent so the rocket can now fly freely.
            double rocketStartX = vx + radius;
            double rocketStartY = vy;

            orbit.getChildren().remove(rocket);
            scene.getChildren().remove(orbit);
            rocket.setLayoutX(0);
            rocket.setLayoutY(0);

            // A carrier gives the rocket a +90 degree local offset so the
            // nose (which points up in rocketGraphic()) follows the tangent.
            Group rocketCarrier = new Group(rocket);
            rocket.setRotate(90);
            rocketCarrier.setLayoutX(rocketStartX);
            rocketCarrier.setLayoutY(rocketStartY);
            scene.getChildren().add(rocketCarrier);

            // Curved route: Vesperion -> above the system -> Earth.
            Path path = new Path();
            path.getElements().add(new MoveTo(0, 0));
            path.getElements().add(new CubicCurveTo(
                    WIDTH * 0.32 - rocketStartX, HEIGHT * 0.18 - rocketStartY,
                    WIDTH * 0.58 - rocketStartX, HEIGHT * 0.16 - rocketStartY,
                    WIDTH * 0.70 - rocketStartX, HEIGHT * 0.35 - rocketStartY));
            path.getElements().add(new CubicCurveTo(
                    WIDTH * 0.76 - rocketStartX, HEIGHT * 0.45 - rocketStartY,
                    WIDTH * 0.79 - rocketStartX, HEIGHT * 0.50 - rocketStartY,
                    ex - 18 - rocketStartX, ey - rocketStartY));

            PathTransition approach = new PathTransition(Duration.seconds(2.25), path, rocketCarrier);
            approach.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
            approach.setInterpolator(Interpolator.EASE_BOTH);
            approach.setOnFinished(done -> {
                rocketCarrier.setVisible(false);
                Label success = new Label("MISSION SUCCESS");
                success.setFont(Font.font(FONT_MAIN, FontWeight.BOLD, 52));
                success.setTextFill(Color.web("#8fe8b4"));
                success.setEffect(new DropShadow(35, Color.web("#32df8a")));
                StackPane.setAlignment(success, Pos.CENTER);
                root.getChildren().add(success);

                FadeTransition f = new FadeTransition(Duration.millis(600), success);
                f.setFromValue(0);
                f.setToValue(1);
                f.play();

                PauseTransition p = new PauseTransition(Duration.seconds(0.5));
                p.setOnFinished(x -> endScreen(true));
                p.play();
            });
            approach.play();
        });

        switchScene(root);
        orbitBurn.play();
    }


    private void playFailureCinematic() {
        // Failure is deliberately blacker than the old starfield scene.
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #010309;");
        Pane scene = new Pane();
        root.getChildren().add(scene);

        Circle vesperion = planet(96, "#6b9fba", "#182b4e");
        vesperion.setLayoutX(WIDTH * 0.70);
        vesperion.setLayoutY(HEIGHT * 0.45);
        scene.getChildren().add(vesperion);

        Group rocket = rocketGraphic();
        rocket.setLayoutX(WIDTH * 0.45);
        rocket.setLayoutY(HEIGHT * 0.45);
        scene.getChildren().add(rocket);

        Timeline shake = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(rocket.rotateProperty(), -5)),
                new KeyFrame(Duration.millis(120), new KeyValue(rocket.rotateProperty(), 5)),
                new KeyFrame(Duration.millis(240), new KeyValue(rocket.rotateProperty(), -5)));
        shake.setCycleCount(5);
        shake.setAutoReverse(true);
        shake.setOnFinished(e -> {
            rocket.setVisible(false);
            for (int i = 0; i < 28; i++) {
                spawnParticle(scene, rocket.getLayoutX(), rocket.getLayoutY());
            }
            Label fail = new Label("MISSION FAILED");
            fail.setFont(Font.font(FONT_MAIN, FontWeight.BOLD, 50));
            fail.setTextFill(Color.web("#ff8f8f"));
            fail.setEffect(new DropShadow(35, Color.web("#ff3434")));
            StackPane.setAlignment(fail, Pos.CENTER);
            root.getChildren().add(fail);
            PauseTransition p = new PauseTransition(Duration.seconds(2.2));
            p.setOnFinished(x -> endScreen(false));
            p.play();
        });

        switchScene(root);
        shake.play();
    }


    private void spawnParticle(Pane scene, double x, double y) {
        Circle p = new Circle(2 + rnd.nextDouble() * 3, Color.web(rnd.nextBoolean() ? "#ffbf69" : "#e6f2ff"));
        p.setLayoutX(x); p.setLayoutY(y); scene.getChildren().add(p);
        double angle = rnd.nextDouble() * Math.PI * 2;
        double distance = 40 + rnd.nextDouble() * 130;
        TranslateTransition t = new TranslateTransition(Duration.millis(650 + rnd.nextInt(500)), p);
        t.setByX(Math.cos(angle) * distance); t.setByY(Math.sin(angle) * distance);
        FadeTransition f = new FadeTransition(Duration.millis(900), p); f.setToValue(0);
        ParallelTransition pt = new ParallelTransition(t, f); pt.setOnFinished(e -> scene.getChildren().remove(p)); pt.play();
    }

    private void endScreen(boolean won) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #010309;");
        addBlinkingStars(root, 85, 0.38);
        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(760);
        box.setPadding(new Insets(24));
        // No enclosing panel: the result floats directly over the starfield.
        Label title = new Label(won ? "MISSION SUCCESS" : "MISSION FAILED");
        title.setFont(Font.font(FONT_MAIN, FontWeight.BOLD, 38));
        title.setTextFill(won ? Color.web("#8fe8b4") : Color.web("#ff8f8f"));
        Label msg = new Label(won
                ? "The rocket was fully fueled and successfully returned home to Earth."
                : "The rocket remains on Vesperion. Too many wrong locks were entered.");
        msg.setFont(Font.font(FONT_STORY, 16)); msg.setTextFill(Color.web("#d8e4f0"));
        msg.setWrapText(true); msg.setTextAlignment(TextAlignment.CENTER);
        Label stats = new Label("Correct: " + game.getCorrectAnswers() + " / 8   •   Wrong: " + game.getWrongAnswers() + " / 4");
        stats.setFont(Font.font(FONT_MONO, 12)); stats.setTextFill(Color.web("#9fb4c8"));
        Button again = new Button("PLAY AGAIN");
        again.setFont(Font.font(FONT_MAIN, FontWeight.BOLD, 15));
        again.setStyle("-fx-background-color: #2d78d0; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 10 24;");
        again.setOnAction(e -> playIntro());
        box.getChildren().addAll(title, msg, stats, again);
        root.getChildren().add(box); StackPane.setAlignment(box, Pos.CENTER);
        switchScene(root);
    }

    // ---------------------------------------------------------------------
    // GRAPHICS + SMALL HELPERS
    // ---------------------------------------------------------------------

    private Circle planet(double radius, String centerHex, String edgeHex) {
        return new Circle(radius, new RadialGradient(0, 0, 0.32, 0.32, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#f4f8ff")),
                new Stop(0.16, Color.web(centerHex)),
                new Stop(1, Color.web(edgeHex))));
    }

    private Group rocketGraphic() {

        Polygon nose = new Polygon(0, -24, 10, -2, -10, -2);
        nose.setFill(Color.web("#e66a61"));
        Rectangle body = new Rectangle(-8, -2, 16, 28);
        body.setArcWidth(8); body.setArcHeight(8); body.setFill(Color.web("#dce6ef"));
        Circle window = new Circle(0, 8, 3.5, Color.web("#55aee5"));
        Polygon left = new Polygon(-8, 17, -19, 31, -8, 27); left.setFill(Color.web("#91a7bd"));
        Polygon right = new Polygon(8, 17, 19, 31, 8, 27); right.setFill(Color.web("#91a7bd"));
        Polygon flame = new Polygon(-6, 26, 6, 26, 0, 48); flame.setFill(Color.web("#ffc35c"));
        Group rocket = new Group(flame, left, right, body, nose, window);
        Timeline flicker = new Timeline(
                new KeyFrame(Duration.millis(80), new KeyValue(flame.scaleYProperty(), 0.7)),
                new KeyFrame(Duration.millis(160), new KeyValue(flame.scaleYProperty(), 1.3)));
        flicker.setAutoReverse(true); flicker.setCycleCount(Animation.INDEFINITE); flicker.play();
        return rocket;
    }

    private void labelAt(Pane pane, String text, double x, double y, double size, String color) {
        Label l = new Label(text);
        l.setFont(Font.font(FONT_MONO, FontWeight.BOLD, size));
        l.setTextFill(Color.web(color));
        l.setLayoutX(x); l.setLayoutY(y);
        pane.getChildren().add(l);
    }

    private void labelAt(Group group, String text, double x, double y, double size, String color) {
        Label l = new Label(text);
        l.setFont(Font.font(FONT_MONO, FontWeight.BOLD, size));
        l.setTextFill(Color.web(color));
        l.setLayoutX(x); l.setLayoutY(y);
        group.getChildren().add(l);
    }

    private void switchScene(Parent root) {
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        switchScene(scene);
    }

    private void switchScene(Scene scene) {
        Node root = scene.getRoot();
        root.setOpacity(0);
        stage.setScene(scene);
        FadeTransition fade = new FadeTransition(Duration.millis(450), root);
        fade.setToValue(1);
        fade.play();
    }

    public static void main(String[] args) { launch(args); }
}
