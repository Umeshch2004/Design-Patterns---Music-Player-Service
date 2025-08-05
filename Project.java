import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.json.JSONArray;
import org.json.JSONObject;


class MusicPlayerApp {
    public static void main(String[] args) {
        MusicSource musicSource = new AudioDbMusicSource();

        MusicPlayerViewModel viewModel = new MusicPlayerViewModel(musicSource);
        
        SwingUtilities.invokeLater(() -> {
            MusicPlayerView view = new MusicPlayerView(viewModel);
            view.setVisible(true);
        });
    }
}


// MARK: - Data Models

record Song(String id, String title, String artist, int duration) {
    @Override
    public String toString() {
        return title + " - " + artist;
    }
}

enum PlaybackState {
    PLAYING, PAUSED, STOPPED
}


// MARK: - Observer Pattern

interface Observer {
    void update();
}

interface Subject {
    void addObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers();
}


// MARK: - Music Source Strategy

interface MusicSource {
    CompletableFuture<List<Song>> loadSongs();
}

class LocalMusicSource implements MusicSource {
    @Override
    public CompletableFuture<List<Song>> loadSongs() {
        List<Song> songs = List.of(
            new Song("local-1", "Local Song 1", "Local Artist", 180),
            new Song("local-2", "Local Song 2", "Local Artist", 240)
        );
        return CompletableFuture.completedFuture(songs);
    }
}

class AudioDbMusicSource implements MusicSource {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String apiUrl = "https://www.theaudiodb.com/api/v1/json/2/mostloved.php?format=track";

    @Override
    public CompletableFuture<List<Song>> loadSongs() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseSongsFromResponse);
    }

    private List<Song> parseSongsFromResponse(String jsonBody) {
        List<Song> songs = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(jsonBody);
            JSONArray tracks = json.getJSONArray("loved");

            for (int i = 0; i < tracks.length(); i++) {
                JSONObject trackJson = tracks.getJSONObject(i);
                
                String id = trackJson.optString("idTrack", "");
                String title = trackJson.optString("strTrack", "Unknown Title");
                String artist = trackJson.optString("strArtist", "Unknown Artist");
                int durationInSeconds = trackJson.optInt("intDuration", 180000) / 1000;

                if (!id.isEmpty()) {
                    songs.add(new Song(id, title, artist, durationInSeconds));
                }
            }
        } catch (Exception exception) {
            System.err.println("Could not parse JSON from TheAudioDB: " + exception.getMessage());
        }
        return songs;
    }
}


// MARK: - Music Player Service (Singleton & Facade)

class MusicPlayerService implements Subject {
    private static final MusicPlayerService INSTANCE = new MusicPlayerService();

    private final List<Observer> observers = new ArrayList<>();
    private List<Song> playlist = new ArrayList<>();
    private int currentSongIndex = -1;
    
    private Song currentSong;
    private PlaybackState playbackState = PlaybackState.STOPPED;
    private int currentTimeInSeconds = 0;
    
    private final Timer playbackTimer;

    private MusicPlayerService() {
        this.playbackTimer = new Timer(1000, (tick) -> {
            if (playbackState == PlaybackState.PLAYING && currentSong != null) {
                if (currentTimeInSeconds < currentSong.duration()) {
                    currentTimeInSeconds++;
                } else {
                    skipToNextSong();
                }
                notifyObservers();
            }
        });
    }

    public static MusicPlayerService getInstance() {
        return INSTANCE;
    }

    public void setPlaylist(List<Song> songs) {
        this.playlist = songs;
        this.currentSongIndex = songs.isEmpty() ? -1 : 0;
        this.currentSong = songs.isEmpty() ? null : songs.get(0);
        this.currentTimeInSeconds = 0;
        this.playbackState = PlaybackState.STOPPED;
        notifyObservers();
    }

    public void play() {
        if (currentSong != null) {
            playbackState = PlaybackState.PLAYING;
            playbackTimer.start();
            notifyObservers();
        }
    }

    public void pause() {
        playbackState = PlaybackState.PAUSED;
        playbackTimer.stop();
        notifyObservers();
    }

    public void skipToNextSong() {
        if (!playlist.isEmpty()) {
            currentSongIndex = (currentSongIndex + 1) % playlist.size();
            currentSong = playlist.get(currentSongIndex);
            currentTimeInSeconds = 0;
            if (playbackState != PlaybackState.PLAYING) {
                playbackState = PlaybackState.PAUSED;
            }
            notifyObservers();
        }
    }

    public void returnToPreviousSong() {
        if (!playlist.isEmpty()) {
            currentSongIndex = (currentSongIndex - 1 + playlist.size()) % playlist.size();
            currentSong = playlist.get(currentSongIndex);
            currentTimeInSeconds = 0;
            if (playbackState != PlaybackState.PLAYING) {
                playbackState = PlaybackState.PAUSED;
            }
            notifyObservers();
        }
    }

    public Song getCurrentSong() { return currentSong; }
    public PlaybackState getPlaybackState() { return playbackState; }
    public int getCurrentTime() { return currentTimeInSeconds; }

    @Override
    public void addObserver(Observer observer) { observers.add(observer); }
    @Override
    public void removeObserver(Observer observer) { observers.remove(observer); }
    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }
}


// MARK: - ViewModel (MVVM)

class MusicPlayerViewModel implements Observer, Subject {
    private final MusicSource musicSource;
    private final MusicPlayerService playerService = MusicPlayerService.getInstance();
    private final List<Observer> observers = new ArrayList<>();

    private List<Song> songs = new ArrayList<>();
    private Song currentSong;
    private PlaybackState playbackState = PlaybackState.STOPPED;
    private int currentTime = 0;

    public MusicPlayerViewModel(MusicSource musicSource) {
        this.musicSource = musicSource;
        this.playerService.addObserver(this);
        loadSongs();
    }
    
    public void loadSongs() {
        musicSource.loadSongs().thenAccept(loadedSongs -> {
            SwingUtilities.invokeLater(() -> {
                this.songs = loadedSongs;
                playerService.setPlaylist(loadedSongs);
                notifyObservers();
            });
        }).exceptionally(error -> {
            System.err.println("Failed to load songs: " + error.getMessage());
            return null;
        });
    }

    public void play() { playerService.play(); }
    public void pause() { playerService.pause(); }
    public void skip() { playerService.skipToNextSong(); }
    public void previous() { playerService.returnToPreviousSong(); }

    public List<Song> getSongs() { return songs; }
    public Song getCurrentSong() { return currentSong; }
    public PlaybackState getPlaybackState() { return playbackState; }
    public int getCurrentTime() { return currentTime; }

    @Override
    public void update() {
        this.currentSong = playerService.getCurrentSong();
        this.playbackState = playerService.getPlaybackState();
        this.currentTime = playerService.getCurrentTime();
        notifyObservers();
    }

    @Override
    public void addObserver(Observer observer) { observers.add(observer); }
    @Override
    public void removeObserver(Observer observer) { observers.remove(observer); }
    @Override
    public void notifyObservers() {
        SwingUtilities.invokeLater(() -> {
            for (Observer observer : observers) {
                observer.update();
            }
        });
    }
}


// MARK: - View (Swing UI)

class MusicPlayerView extends JFrame implements Observer {
    private final MusicPlayerViewModel viewModel;

    private final JLabel songTitleLabel = new JLabel("Loading songs...", SwingConstants.CENTER);
    private final JLabel artistLabel = new JLabel("", SwingConstants.CENTER);
    private final JProgressBar progressBar = new JProgressBar();
    private final JButton playPauseButton = new JButton("Play");
    private final JButton prevButton = new JButton("Previous");
    private final JButton skipButton = new JButton("Skip");
    private final JList<Song> playlistView = new JList<>();

    public MusicPlayerView(MusicPlayerViewModel viewModel) {
        this.viewModel = viewModel;
        this.viewModel.addObserver(this);

        buildUI();
        connectActions();
        update();
    }

    private void buildUI() {
        setTitle("Music Player");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        songTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        artistLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        infoPanel.add(songTitleLabel);
        infoPanel.add(artistLabel);
        infoPanel.add(progressBar);

        JScrollPane scrollPane = new JScrollPane(playlistView);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Playlist"));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.add(prevButton);
        controlPanel.add(playPauseButton);
        controlPanel.add(skipButton);

        add(infoPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void connectActions() {
        playPauseButton.addActionListener(event -> {
            if (viewModel.getPlaybackState() == PlaybackState.PLAYING) {
                viewModel.pause();
            } else {
                viewModel.play();
            }
        });
        skipButton.addActionListener(event -> viewModel.skip());
        prevButton.addActionListener(event -> viewModel.previous());
    }

    @Override
    public void update() {
        Song song = viewModel.getCurrentSong();
        if (song != null) {
            songTitleLabel.setText(song.title());
            artistLabel.setText(song.artist());
            progressBar.setMaximum(song.duration());
            progressBar.setValue(viewModel.getCurrentTime());
        } else {
            songTitleLabel.setText("Playlist loaded. Select a song.");
            artistLabel.setText("");
            progressBar.setValue(0);
        }

        playPauseButton.setText(viewModel.getPlaybackState() == PlaybackState.PLAYING ? "Pause" : "Play");

        DefaultListModel<Song> model = new DefaultListModel<>();
        viewModel.getSongs().forEach(model::addElement);
        playlistView.setModel(model);
        
        if (song != null) {
            int songIndex = viewModel.getSongs().indexOf(song);
            if(songIndex != -1) {
                playlistView.setSelectedIndex(songIndex);
            }
        }
    }
}
