package at.ac.hcw.kanuescape.audio;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;

public final class AudioManager {

    private static final AudioManager INSTANCE = new AudioManager();
    public static AudioManager get() { return INSTANCE; }
    private boolean musicEnabled = true;

    private MediaPlayer musicPlayer;
    private double musicVolume = 0.35;   // Default (0.0–1.0)
    private double sfxVolume = 0.70;

    // damit beim Re-Enable wieder start:
    private String lastMusicPath = null;
    private boolean lastLoop = true;

    private AudioManager() {}

    // --- MUSIC ---

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;

        if (!musicEnabled) {
            stopMusic();
        } else {
            // resume last requested track (wenn es einen gab)
            if (lastMusicPath != null) {
                playMusicLoop(lastMusicPath, lastLoop);
            }
        }
    }

    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.dispose();
            musicPlayer = null;
        }
    }

    public void playMusicLoop(String resourcePath) {
        playMusicLoop(resourcePath, true);
    }

    // mit cross fade
    public void playMusicLoop(String resourcePath, boolean loop) {
        // Merken, egal ob enabled oder nicht (damit resume klappt)
        lastMusicPath = resourcePath;
        lastLoop = loop;

        if (!musicEnabled) return;

        // alte Musik weg
        stopMusic();

        var url = AudioManager.class.getResource(resourcePath);
        if (url == null) {
            System.out.println("MUSIC NOT FOUND: " + resourcePath);
            return;
        }

        Media media = new Media(url.toExternalForm());
        musicPlayer = new MediaPlayer(media);

        if (loop) {
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        }

        musicPlayer.play();
    }

    // --- SFX (minimal, optional für später) ---
    // Für SFX kann man auch AudioClip verwenden (leichter & schneller als MediaPlayer).


    // --- helpers ---

    private static double clamp01(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }

    private static void fade(MediaPlayer p, double from, double to, Duration dur, Runnable onFinished) {
        if (p == null) return;
        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(p.volumeProperty(), from)),
                new KeyFrame(dur,          new KeyValue(p.volumeProperty(), to))
        );
        if (onFinished != null) tl.setOnFinished(e -> onFinished.run());
        tl.play();
    }
}
