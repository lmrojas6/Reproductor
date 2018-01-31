/*
 * Copyright (c) 2011, Pro JavaFX Authors
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of JFXtras nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.File;
import java.net.URL;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.Slider;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.sun.javafx.runtime.VersionInfo;
/**
 * @author dean
 */
 class AudioPlayer3 extends Application {
  private final SongModel songModel;
  
  private MetadataView metaDataView;
  private PlayerControlsView playerControlsView;
  
  public static void main(String[] args) {
    launch(args);
  }

  public AudioPlayer3() {
    songModel = new SongModel();
  }

  @Override
  public void start(Stage primaryStage) {
    System.out.println("JavaFX version: "+VersionInfo.getRuntimeVersion());
    songModel.setURL("http://traffic.libsyn.com/dickwall/JavaPosse373.mp3");
    metaDataView = new MetadataView(songModel);
    playerControlsView = new PlayerControlsView(songModel);
    
    final BorderPane root = new BorderPane();
    root.setCenter(metaDataView.getViewNode());
    root.setBottom(playerControlsView.getViewNode());
    
    final Scene scene = new Scene(root, 800, 400);
    initSceneDragAndDrop(scene);
    
    final URL stylesheet = getClass().getResource("media.css");
    scene.getStylesheets().add(stylesheet.toString());

    primaryStage.setScene(scene);
    primaryStage.setTitle("Audio Player 3");
    primaryStage.show();
  }
  
  private void initSceneDragAndDrop(Scene scene) {
    scene.setOnDragOver(new EventHandler<DragEvent>() {
      @Override
      public void handle(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles() || db.hasUrl()) {
          event.acceptTransferModes(TransferMode.ANY);
        }
        event.consume();
      }
    });

    scene.setOnDragDropped(new EventHandler<DragEvent>() {
      @Override
      public void handle(DragEvent event) {
        Dragboard db = event.getDragboard();
        String url = null;
        
        if (db.hasFiles()) {
          url = db.getFiles().get(0).toURI().toString();
        } else if (db.hasUrl()) {
          url = db.getUrl();
        }
        
        if (url != null) {
          songModel.setURL(url);
          songModel.getMediaPlayer().play();
        }
        
        event.setDropCompleted(url != null);
        event.consume();
      }
    });
  }
}
/**
 * @author dean
 */
 class MetadataView extends AbstractView {

  public MetadataView(SongModel songModel) {
    super(songModel);
  }
  
  @Override
  protected Node initView() {
    final Label title = createLabel("title");
    final Label artist = createLabel("artist");
    final Label album = createLabel("album");
    final Label year = createLabel("year");
    final ImageView albumCover = createAlbumCover();
    
    title.textProperty().bind(songModel.titleProperty());
    artist.textProperty().bind(songModel.artistProperty());
    album.textProperty().bind(songModel.albumProperty());
    year.textProperty().bind(songModel.yearProperty());
    albumCover.imageProperty().bind(songModel.albumCoverProperty());
    
    final GridPane gp = new GridPane();
    gp.setPadding(new Insets(10));
    gp.setHgap(20);
    gp.add(albumCover, 0, 0, 1, GridPane.REMAINING);
    gp.add(title, 1, 0);
    gp.add(artist, 1, 1);
    gp.add(album, 1, 2);
    gp.add(year, 1, 3);
    
    final ColumnConstraints c0 = new ColumnConstraints();
    final ColumnConstraints c1 = new ColumnConstraints();
    c1.setHgrow(Priority.ALWAYS);
    gp.getColumnConstraints().addAll(c0, c1);
    
    final RowConstraints r0 = new RowConstraints();
    r0.setValignment(VPos.TOP);
    gp.getRowConstraints().addAll(r0, r0, r0, r0);
    
    return gp;
  }
  
  private Label createLabel(String id) {
    return LabelBuilder.create().id(id).build();
  }
  
  private ImageView createAlbumCover() {
    final Reflection reflection = new Reflection();
    reflection.setFraction(0.2);

    final ImageView albumCover = new ImageView();
    albumCover.setFitWidth(240);
    albumCover.setPreserveRatio(true);
    albumCover.setSmooth(true);
    albumCover.setEffect(reflection);
    
    return albumCover;
  }
}

/**
 * @author dean
 */
class PlayerControlsView extends AbstractView {
  private Image pauseImg;
  private Image playImg;
  private ImageView playPauseIcon;
  
  private StatusListener statusListener;
  private CurrentTimeListener currentTimeListener;
  
  private Node controlPanel;
  private Label statusLabel;
  private Label currentTimeLabel;
  private Label totalDurationLabel;
  private Slider volumeSlider;
  private Slider positionSlider;
  
  public PlayerControlsView(SongModel songModel) {
    super(songModel);
    
    songModel.mediaPlayerProperty().addListener(new MediaPlayerListener());
    
    statusListener = new StatusListener();
    currentTimeListener = new CurrentTimeListener();
    addListenersAndBindings(songModel.getMediaPlayer());
  }

  @Override
  protected Node initView() {
    final Button openButton = createOpenButton();
    controlPanel = createControlPanel();
    volumeSlider = createSlider("volumeSlider");
    statusLabel = createLabel("Buffering", "statusDisplay");
    positionSlider = createSlider("positionSlider");
    totalDurationLabel = createLabel("00:00", "mediaText");
    currentTimeLabel = createLabel("00:00", "mediaText");
    
    positionSlider.valueChangingProperty().addListener(new PositionListener());
    
    final ImageView volLow = new ImageView();
    volLow.setId("volumeLow");
    
    final ImageView volHigh = new ImageView();
    volHigh.setId("volumeHigh");
    
    final GridPane gp = new GridPane();
    gp.setHgap(1);
    gp.setVgap(1);
    gp.setPadding(new Insets(10));

    final ColumnConstraints buttonCol = new ColumnConstraints(100);
    final ColumnConstraints spacerCol = new ColumnConstraints(40, 80, 80);
    final ColumnConstraints middleCol = new ColumnConstraints();
    middleCol.setHgrow(Priority.ALWAYS);

    gp.getColumnConstraints().addAll(buttonCol, spacerCol, middleCol, 
                                     spacerCol, buttonCol);

    GridPane.setValignment(openButton, VPos.BOTTOM);
    GridPane.setHalignment(volHigh, HPos.RIGHT);
    GridPane.setValignment(volumeSlider, VPos.TOP);
    GridPane.setHalignment(statusLabel, HPos.RIGHT);
    GridPane.setValignment(statusLabel, VPos.TOP);
    GridPane.setHalignment(currentTimeLabel, HPos.RIGHT);
    
    gp.add(openButton, 0, 0, 1, 3);
    gp.add(volLow, 1, 0);
    gp.add(volHigh, 1, 0);
    gp.add(volumeSlider, 1, 1);
    gp.add(controlPanel, 2, 0, 1, 2);
    gp.add(statusLabel, 3, 1);
    gp.add(currentTimeLabel, 1, 2);
    gp.add(positionSlider, 2, 2);
    gp.add(totalDurationLabel, 3, 2);

    return gp;
  }

  private Button createOpenButton() {
    final Button openButton = new Button();
    openButton.setId("openButton");
    openButton.setOnAction(new OpenHandler());
    openButton.setPrefWidth(32);
    openButton.setPrefHeight(32);
    return openButton;
  }

  private Node createControlPanel() {
    final HBox hbox = new HBox();
    hbox.setAlignment(Pos.CENTER);
    hbox.setFillHeight(false);
    
    final Button playPauseButton = createPlayPauseButton();

    final Button seekStartButton = new Button();
    seekStartButton.setId("seekStartButton");
    seekStartButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        seekAndUpdatePosition(Duration.ZERO);
      }
    });

    final Button seekEndButton = new Button();
    seekEndButton.setId("seekEndButton");
    seekEndButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        final MediaPlayer mediaPlayer = songModel.getMediaPlayer();
        final Duration totalDuration = mediaPlayer.getTotalDuration();
        final Duration oneSecond = Duration.seconds(1);
        seekAndUpdatePosition(totalDuration.subtract(oneSecond));
      }
    });

    hbox.getChildren().addAll(seekStartButton, playPauseButton, seekEndButton);
    return hbox;
  }

  private Button createPlayPauseButton() {
    URL url = getClass().getResource("resources/pause.png");
    pauseImg = new Image(url.toString());

    url = getClass().getResource("resources/play.png");
    playImg = new Image(url.toString());

    playPauseIcon = new ImageView(playImg);

    final Button playPauseButton = new Button(null, playPauseIcon);
    playPauseButton.setId("playPauseButton");
    playPauseButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent arg0) {
        final MediaPlayer mediaPlayer = songModel.getMediaPlayer();
        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
          mediaPlayer.pause();
        } else {
          mediaPlayer.play();
        }
      }
    });
    return playPauseButton;
  }

  private Slider createSlider(String id) {
    final Slider slider = new Slider(0.0, 1.0, 0.1);
    slider.setId(id);
    slider.setValue(0);
    return slider;
  }

  private Label createLabel(String text, String styleClass) {
    final Label label = new Label(text);
    label.getStyleClass().add(styleClass);
    return label;
  }

  private void addListenersAndBindings(final MediaPlayer mp) {
    mp.statusProperty().addListener(statusListener);
    mp.currentTimeProperty().addListener(currentTimeListener);
    mp.totalDurationProperty().addListener(new TotalDurationListener());
    
    mp.setOnEndOfMedia(new Runnable() {
      @Override
      public void run() {
        songModel.getMediaPlayer().stop();
      }
    });
    
    volumeSlider.valueProperty().bindBidirectional(mp.volumeProperty());
  }

  private void removeListenersAndBindings(MediaPlayer mp) {
    volumeSlider.valueProperty().unbind();
    mp.statusProperty().removeListener(statusListener);
    mp.currentTimeProperty().removeListener(currentTimeListener);
  }

  private void seekAndUpdatePosition(Duration duration) {
    final MediaPlayer mediaPlayer = songModel.getMediaPlayer();

    if (mediaPlayer.getStatus() == Status.STOPPED) {
      mediaPlayer.pause();
    }

    mediaPlayer.seek(duration);
    
    if (mediaPlayer.getStatus() != Status.PLAYING) {
      updatePositionSlider(duration);
    }
  }
  
  private String formatDuration(Duration duration) {
    double millis = duration.toMillis();
    int seconds = (int) (millis / 1000) % 60;
    int minutes = (int) (millis / (1000 * 60));
    return String.format("%02d:%02d", minutes, seconds);
  }
  
  private void updateStatus(Status newStatus) {
    if (newStatus == Status.UNKNOWN || newStatus == null) {
      controlPanel.setDisable(true);
      positionSlider.setDisable(true);
      statusLabel.setText("Buffering");
    } else {
      controlPanel.setDisable(false);
      positionSlider.setDisable(false);
      
      statusLabel.setText(newStatus.toString());

      if (newStatus == Status.PLAYING) {
        playPauseIcon.setImage(pauseImg);
      } else {
        playPauseIcon.setImage(playImg);
      }
    }
  }

  private void updatePositionSlider(Duration currentTime) {
    if (positionSlider.isValueChanging())
      return;
    
    final MediaPlayer mediaPlayer = songModel.getMediaPlayer();
    final Duration total = mediaPlayer.getTotalDuration();

    if (total == null || currentTime == null) {
      positionSlider.setValue(0);
    } else {
      positionSlider.setValue(currentTime.toMillis() / total.toMillis());
    }
  }
  
  private class MediaPlayerListener implements ChangeListener<MediaPlayer> {
    @Override
    public void changed(ObservableValue<? extends MediaPlayer> observable,
                        MediaPlayer oldValue, MediaPlayer newValue) {
      if (oldValue != null) {
        removeListenersAndBindings(oldValue);
      }
      addListenersAndBindings(newValue);
    }
  }
  
  private class OpenHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent event) {
      FileChooser fc = new FileChooser();
      fc.setTitle("Pick a Sound File");
      File song = fc.showOpenDialog(viewNode.getScene().getWindow());
      if (song != null) {
        songModel.setURL(song.toURI().toString());
        songModel.getMediaPlayer().play();
      }
    }
  }
  
  private class StatusListener implements InvalidationListener {
    @Override
    public void invalidated(Observable observable) {
      Platform.runLater(new Runnable() {
        @Override
        public void run() {
          updateStatus(songModel.getMediaPlayer().getStatus());
        }
      });
    }
  }

private class CurrentTimeListener implements InvalidationListener {
  @Override
  public void invalidated(Observable observable) {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        final MediaPlayer mediaPlayer = songModel.getMediaPlayer();
        final Duration currentTime = mediaPlayer.getCurrentTime();
        currentTimeLabel.setText(formatDuration(currentTime));
        updatePositionSlider(currentTime);
      }
    });
  }
}

  private class TotalDurationListener implements InvalidationListener {
    @Override
    public void invalidated(Observable observable) {
      final MediaPlayer mediaPlayer = songModel.getMediaPlayer();
      final Duration totalDuration = mediaPlayer.getTotalDuration();
      totalDurationLabel.setText(formatDuration(totalDuration));
    }
  }

  private class PositionListener implements ChangeListener<Boolean> {
    @Override
    public void changed(ObservableValue<? extends Boolean> observable, 
                        Boolean oldValue, Boolean newValue) {
      if (oldValue && !newValue) {
        double pos = positionSlider.getValue();
        final MediaPlayer mediaPlayer = songModel.getMediaPlayer();
        final Duration seekTo = mediaPlayer.getTotalDuration().multiply(pos);
        seekAndUpdatePosition(seekTo);
      }
    }
  }
}

/**
 * @author dean
 */
 final class SongModel {
  private static final String DEFAULT_IMG_URL = 
          SongModel.class.getResource("resources/defaultAlbum.png").toString();
  
  private static final Image DEFAULT_ALBUM_COVER = 
          new Image(DEFAULT_IMG_URL.toString());

  private final StringProperty album = new SimpleStringProperty(this, "album");
  private final StringProperty artist = new SimpleStringProperty(this,"artist");
  private final StringProperty title = new SimpleStringProperty(this, "title");
  private final StringProperty year = new SimpleStringProperty(this, "year");
  
  private final ObjectProperty<Image> albumCover = 
          new SimpleObjectProperty<Image>(this, "albumCover");
  
  private final ReadOnlyObjectWrapper<MediaPlayer> mediaPlayer = 
          new ReadOnlyObjectWrapper<MediaPlayer>(this, "mediaPlayer");

  public SongModel() {
    resetProperties();
  }

  public void setURL(String url) {
    if (mediaPlayer.get() != null) {
      mediaPlayer.get().stop();
    }
    
    initializeMedia(url);
  }

  public String getAlbum() { return album.get(); }
  public void setAlbum(String value) { album.set(value); }
  public StringProperty albumProperty() { return album; }

  public String getArtist() { return artist.get(); }
  public void setArtist(String value) { artist.set(value); }
  public StringProperty artistProperty() { return artist; }

  public String getTitle() { return title.get(); }
  public void setTitle(String value) { title.set(value); }
  public StringProperty titleProperty() { return title; }

  public String getYear() { return year.get(); }
  public void setYear(String value) { year.set(value); }
  public StringProperty yearProperty() { return year; }

  public Image getAlbumCover() { return albumCover.get(); }
  public void setAlbumCover(Image value) { albumCover.set(value); }
  public ObjectProperty<Image> albumCoverProperty() { return albumCover; }

  public MediaPlayer getMediaPlayer() { return mediaPlayer.get(); }
  public ReadOnlyObjectProperty<MediaPlayer> mediaPlayerProperty() { 
    return mediaPlayer.getReadOnlyProperty();
  }

  private void resetProperties() {
    setArtist("");
    setAlbum("");
    setTitle("");
    setYear("");
    
    setAlbumCover(DEFAULT_ALBUM_COVER);
  }

  private void initializeMedia(String url) {
    resetProperties();
    
    try {
      final Media media = new Media(url);
      media.getMetadata().addListener(new MapChangeListener<String, Object>() {
        @Override
        public void onChanged(Change<? extends String, ? extends Object> ch) {
          if (ch.wasAdded()) {
            handleMetadata(ch.getKey(), ch.getValueAdded());
          }
        }
      });

      mediaPlayer.setValue(new MediaPlayer(media));
      mediaPlayer.get().setOnError(new Runnable() {
        @Override
        public void run() {
          String errorMessage = mediaPlayer.get().getError().getMessage();
          // Handle errors during playback
          System.out.println("MediaPlayer Error: " + errorMessage);
        }
      });
    } catch (RuntimeException re) {
      // Handle construction errors
      System.out.println("Caught Exception: " + re.getMessage());
    }
  }

  private void handleMetadata(String key, Object value) {
    if (key.equals("album")) {
      setAlbum(value.toString());
    } else if (key.equals("artist")) {
      setArtist(value.toString());
    } if (key.equals("title")) {
      setTitle(value.toString());
    } if (key.equals("year")) {
      setYear(value.toString());
    } if (key.equals("image")) {
      setAlbumCover((Image)value);
    }
  }
}


/**
 * @author dean
 */
 abstract class AbstractView {
  protected final SongModel songModel;
  protected final Node viewNode;

  public AbstractView(SongModel songModel) {
    this.songModel = songModel;
    this.viewNode = initView();
  }

  public Node getViewNode() {
    return viewNode;
  }

  protected abstract Node initView();
}
