/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package musicplayer;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author DELL
 */
public class MusicPlayer extends Application {

	/**
	 * @param args
	 */
	
	private Button playButton;
	private Button stopButton;
	private Button nextButton;
	private Button backButton;
	
	private Button playListButton;
	private Button addFileButton;
	private Button addFolderButton;
	
	private ToggleButton randomButton;
	private ToggleButton muteButton;
	
	private Slider timeSlider;
	private Slider volumeSlider;
	
	private Text totalTimeText;
	private Text currentTimeText;
	
	private MediaPlayer player;
	private Media media;
	
	private Text titleText;
	
	private Stage primaryStage;
	
	private ListView<File> listView;
	private ObservableList<File> files;
	private int fileIndex;
	private VBox listBox;
	
	private double volume;
	
	private boolean isRandom;
	
	private ArrayList<Integer> playedFiles;
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {

		StackPane root = new StackPane();
	
		initControls();
		fileIndex = 0;
		player    = null;
		media     = null;
		volume    = 0.5;
		
		playListButton.setOnAction(new EventHandler<ActionEvent>(){

			public void handle(ActionEvent arg0) {
				TranslateTransition tt = new TranslateTransition(Duration.millis(1000),listBox);
				listBox.setTranslateX(-500);
			    tt.setByX(4);
			    tt.setToX(0);
			    tt.setCycleCount(1);
			    tt.setAutoReverse(true);
			    tt.play();
			}
			
		});
		
		addFileButton.setOnAction(new EventHandler<ActionEvent>(){

			public void handle(ActionEvent arg0) {
				FileChooser chooser = new FileChooser();
				chooser.setInitialDirectory(new File(System.getProperty("user.home")));
				chooser.setTitle("Selecciona un Archivo de Sonido");
				chooser.getExtensionFilters().addAll(
				         new ExtensionFilter("WAV,MP3", "*.wav", "*.mp3"));
				
				File selectedFile = chooser.showOpenDialog(primaryStage);
				 if (selectedFile != null) {
					 files.add(selectedFile);
					 if(player == null){
						 media = new Media(files.get(files.size()-1).toURI().toString());
						 loadMediaPlayer(media);
					 }
				 }
			}
			
		});
		
		addFolderButton.setOnAction(new EventHandler<ActionEvent>(){

			public void handle(ActionEvent arg0) {
				DirectoryChooser chooser = new DirectoryChooser();
				chooser.setInitialDirectory(new File(System.getProperty("user.home")));
				chooser.setTitle("Selecciona una carpeta");
				File selectedFolder = chooser.showDialog(primaryStage);
				 if (selectedFolder != null) {
					 searchAudio(selectedFolder);
					 if(player == null){
						 media = new Media(files.get(0).toURI().toString());
						 loadMediaPlayer(media);
					 }
				 }
			}
			
		});
		
		ImageView view    = new ImageView(new Image("C:\\Users\\Luchito Rojas\\Documents\\NetBeansProjects\\MusicPlayer\\src\\musicplayer\\background 2.png"));
		VBox controlBox   = getControlBox();
		listBox           = getPlayListBox();
		Rectangle topRec  = new Rectangle(500,50);
		Rectangle baseRec = new Rectangle(500,70);

		baseRec.setOpacity(0.3);
		topRec.setOpacity(0.3);
		//listBox.setOpacity(0.7);
		
		StackPane.setMargin(topRec, new Insets(0,0,450,0));
		StackPane.setMargin(baseRec, new Insets(427,0,0,0));
		StackPane.setMargin(controlBox, new Insets(427,0,0,0));
		StackPane.setMargin(titleText, new Insets(0,0,450,0));
		StackPane.setMargin(playListButton, new Insets(308,450,0,0));
		StackPane.setMargin(addFileButton, new Insets(308,0,0,450));
		StackPane.setMargin(addFolderButton, new Insets(308,0,0,370));
		StackPane.setMargin(listBox, new Insets(50,0,70,0));
		
		root.getChildren().add(view);
		root.getChildren().add(topRec);
		root.getChildren().add(titleText);
		root.getChildren().add(baseRec);
		root.getChildren().add(controlBox);
		root.getChildren().add(playListButton);
		root.getChildren().add(addFileButton);
		root.getChildren().add(addFolderButton);
		root.getChildren().add(listBox);
		
		Scene scene = new Scene(root,500,500);
		scene.getStylesheets().add("C:\\Users\\Luchito Rojas\\Documents\\NetBeansProjects\\MusicPlayer\\src\\musicplayer\\Player.css");
		primaryStage = stage;
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.setTitle("MP3 Player");
		primaryStage.show();
	}
	
	public VBox getPlayListBox(){
		VBox root           = new VBox();
		HBox box            = new HBox();
		Button closeButton  = new Button();
		Button deleteButton = new Button();
		Button selectAll    = new Button();
		
		deleteButton.setGraphic(new ImageView(new Image("C:\\Users\\Luchito Rojas\\Documents\\NetBeansProjects\\MusicPlayer\\src\\musicplayer\\delete.png",17,17,true,true,true)));
		selectAll.setGraphic(new ImageView(new Image("C:\\Users\\Luchito Rojas\\Documents\\NetBeansProjects\\MusicPlayer\\src\\musicplayer\\selectAll.png",17,17,true,true,true)));
	
		closeButton.setGraphic(new ImageView(new Image("resources/back.png",20,20,true,true,true)));
		
		listView = new ListView<File>();
		listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		files    = FXCollections.observableArrayList(); 
		listView.setOpacity(0.7);
		listView.setItems(files);
		
		deleteButton.setOnAction(new EventHandler<ActionEvent>(){

			public void handle(ActionEvent arg0) {
				files.removeAll(listView.getSelectionModel().getSelectedItems());
			}
			
		});
		
		selectAll.setOnAction(new EventHandler<ActionEvent>(){

			public void handle(ActionEvent arg0) {
				if(listView.getSelectionModel().isEmpty()){
					listView.getSelectionModel().selectAll();
				}
				else{
					listView.getSelectionModel().clearSelection();
				}
			}
			
		});
		
		closeButton.setOnAction(new EventHandler<ActionEvent>(){

			public void handle(ActionEvent arg0) {
				TranslateTransition tt = new TranslateTransition(Duration.millis(500),listBox);
				listBox.setTranslateX(0);
			    tt.setByX(10);
			    tt.setToX(-500);
			    tt.setCycleCount(1);
			    tt.setAutoReverse(true);
			    tt.play();
			}
			
		});
		
		box.setAlignment(Pos.CENTER);
		box.setSpacing(5);
		box.setMinHeight(45);
		box.setBackground(new Background(new BackgroundFill(Color.BLACK,new CornerRadii(0),new Insets(0,0,0,0))));
		box.getChildren().add(closeButton);
		box.getChildren().add(selectAll);
		box.getChildren().add(deleteButton);
		HBox.setMargin(closeButton, new Insets(0,0,0,7));
		
		root.getChildren().add(listView);
		root.getChildren().add(box);
		root.setTranslateX(-500);
		
		return root;
	}
	
	public VBox getControlBox(){
		
		VBox root = new VBox();
		HBox hbox = new HBox();
		
		HBox hbox2 = new HBox();
		hbox2.getChildren().add(totalTimeText);
		hbox2.getChildren().add(timeSlider);
		hbox2.getChildren().add(currentTimeText);
		
		hbox.getChildren().add(randomButton);
		hbox.getChildren().add(backButton);
		hbox.getChildren().add(stopButton);
		hbox.getChildren().add(playButton);
		hbox.getChildren().add(nextButton);
		hbox.getChildren().add(muteButton);
		hbox.getChildren().add(volumeSlider);
		
		root.getChildren().add(hbox2);
		root.getChildren().add(hbox);
		
		hbox.setSpacing(5);
		hbox.setAlignment(Pos.CENTER);
		hbox2.setAlignment(Pos.CENTER);
		root.setSpacing(5);
		root.setAlignment(Pos.CENTER);
		return root;
	}
	
	public void initControls(){
		
		playButton      = new Button();
		stopButton      = new Button();
		nextButton      = new Button();
		backButton      = new Button();
		randomButton    = new ToggleButton();
		muteButton      = new ToggleButton();
		playListButton  = new Button();
		addFileButton   = new Button();
		addFolderButton = new Button();
		timeSlider      = new Slider(0.0,100.0,0);
		volumeSlider    = new Slider(0.0,1.0,0.5);
		titleText       = new Text();
		totalTimeText   = new Text("00:00:00");
		currentTimeText = new Text("00:00:00");
		
		isRandom = false;
		
		playButton.setEffect(new DropShadow());
		stopButton.setEffect(new DropShadow());
		nextButton.setEffect(new DropShadow());
		backButton.setEffect(new DropShadow());
		muteButton.setEffect(new DropShadow());
		
		playButton.setGraphic(new ImageView(new Image("C:\\Users\\Luchito Rojas\\Documents\\NetBeansProjects\\MusicPlayer\\src\\musicplayer\\play.png",34,34,true,true,true)));
		nextButton.setGraphic(new ImageView(new Image("C:\\Users\\Luchito Rojas\\Documents\\NetBeansProjects\\MusicPlayer\\src\\musicplayer\\next.png",20,20,true,true,true)));
		backButton.setGraphic(new ImageView(new Image("C:\\Users\\Luchito Rojas\\Documents\\NetBeansProjects\\MusicPlayer\\src\\musicplayer\\back.png",20,20,true,true,true)));
		stopButton.setGraphic(new ImageView(new Image("C:\\Users\\Luchito Rojas\\Documents\\NetBeansProjects\\MusicPlayer\\src\\musicplayer\\stop.png",17,17,true,true,true)));
		randomButton.setGraphic(new ImageView(new Image("C:\\Users\\Luchito Rojas\\Documents\\NetBeansProjects\\MusicPlayer\\src\\musicplayer\\random.png",30,30,true,true,true)));
		muteButton.setGraphic(new ImageView(new Image("C:\\Users\\Luchito Rojas\\Documents\\NetBeansProjects\\MusicPlayer\\src\\musicplayer\\speakerNormal.png",25,25,true,true,true)));
		addFileButton.setGraphic(new ImageView(new Image("C:\\Users\\Luchito Rojas\\Documents\\NetBeansProjects\\MusicPlayer\\src\\musicplayer\\addFile.png",20,20,true,true,true)));
		addFolderButton.setGraphic(new ImageView(new Image("C:\\Users\\Luchito Rojas\\Documents\\NetBeansProjects\\MusicPlayer\\src\\musicplayer\\addFolder.png",20,20,true,true,true)));
		playListButton.setGraphic(new ImageView(new Image("C:\\Users\\Luchito Rojas\\Documents\\NetBeansProjects\\MusicPlayer\\src\\musicplayer\\playList.png",20,20,true,true,true)));
		
		double r=18;
		playButton.setShape(new Circle(r));
		nextButton.setShape(new Circle(r));
		backButton.setShape(new Circle(r));
		stopButton.setShape(new Circle(r));
		muteButton.setShape(new Circle(r));
		randomButton.setShape(new Circle(r));
		
		volumeSlider.setMinWidth(200);
		timeSlider.setMinWidth(350);
		
		titleText.setFill(Color.WHITE);
		totalTimeText.setFill(Color.WHITE);
		currentTimeText.setFill(Color.WHITE);
	}
	
	private void setControlEvents(){
		
		playButton.setOnAction(new EventHandler<ActionEvent>(){

			public void handle(ActionEvent arg0) {
				if(player.getStatus() == Status.PLAYING){
					player.pause();
					playButton.setGraphic(new ImageView(new Image("C:\\Users\\Luchito Rojas\\Documents\\NetBeansProjects\\MusicPlayer\\src\\musicplayer\\play.png",34,34,true,true,true)));
				}
				else{
					player.play();
					playButton.setGraphic(new ImageView(new Image("C:\\Users\\Luchito Rojas\\Documents\\NetBeansProjects\\MusicPlayer\\src\\musicplayer\\pause.png",34,34,true,true,true)));
				}			
				System.out.println("Clicked");
			}
			
		});
		
		stopButton.setOnAction(new EventHandler<ActionEvent>(){

			public void handle(ActionEvent arg0) {
				player.stop();
				playButton.setGraphic(new ImageView(new Image("C:\\Users\\Luchito Rojas\\Documents\\NetBeansProjects\\MusicPlayer\\src\\musicplayer\\play.png",34,34,true,true,true)));
			}
			
		});

		nextButton.setOnAction(new EventHandler<ActionEvent>(){

			public void handle(ActionEvent arg0) {
				if(player != null){
					if(player.getStatus() == Status.PLAYING){
						player.stop();
						if(fileIndex < files.size()-1){
							fileIndex++;
							media = new Media(files.get(fileIndex).toURI().toString());
							loadMediaPlayer(media);
							player.play();
						}
						else{
							playButton.setGraphic(new ImageView(new Image("C:\\Users\\Luchito Rojas\\Documents\\NetBeansProjects\\MusicPlayer\\src\\musicplayer\\play.png",34,34,true,true,true)));
						}
						timeSlider.setValue(0);
						currentTimeText.setText("00:00:00");
					}
					else{
						if(fileIndex < files.size()-1){
							fileIndex++;
							media = new Media(files.get(fileIndex).toURI().toString());
							loadMediaPlayer(media);
						}
					}
				}
			}
			
		});
		
		backButton.setOnAction(new EventHandler<ActionEvent>(){

			public void handle(ActionEvent arg0) {
				if(player != null){
					if(player.getStatus() == Status.PLAYING){
						player.stop();
						if(fileIndex > 0){
							fileIndex--;
							media = new Media(files.get(fileIndex).toURI().toString());
							loadMediaPlayer(media);
							player.play();
						}
						else{
							playButton.setGraphic(new ImageView(new Image("C:\\Users\\Luchito Rojas\\Documents\\NetBeansProjects\\MusicPlayer\\src\\musicplayer\\play.png",34,34,true,true,true)));
						}
						timeSlider.setValue(0);
						currentTimeText.setText("00:00:00");
					}
					else{
						if(fileIndex > 0){
							fileIndex--;
							media = new Media(files.get(fileIndex).toURI().toString());
							loadMediaPlayer(media);
						}
					}
				}
			}
			
		});
		
		timeSlider.valueChangingProperty().addListener(new ChangeListener<Boolean>(){

			public void changed(ObservableValue<? extends Boolean> arg0,Boolean oldValue, Boolean newValue) {
				if(player.getStatus() == Status.PLAYING){
					player.pause();
				}
				if(player.getStatus() == Status.PAUSED){
					double duration = player.getTotalDuration().toMillis();
					double percent = timeSlider.getValue();
					double value = ((duration*percent)/100);
					player.seek(new Duration(value));
					//System.out.println("Changing: ["+(int)value+" ms]"+" Total: ["+(int)duration+" ms] ["+(int)percent+" %]");
					player.play();
				}
			}
			
		});
		
		timeSlider.valueProperty().addListener(new ChangeListener<Number>(){

			public void changed(ObservableValue<? extends Number> arg0,Number oldValue, Number newValue) {
				//System.out.println(""+newValue+" %");
			}
			
		});
		
		volumeSlider.valueProperty().addListener(new ChangeListener<Number>(){

			public void changed(ObservableValue<? extends Number> arg0,Number oldValue, Number newValue) {
				player.setVolume(newValue.doubleValue());
				if(muteButton.isSelected()){
					muteButton.setGraphic(new ImageView(new Image("C:\\Users\\Luchito Rojas\\Documents\\NetBeansProjects\\MusicPlayer\\src\\musicplayer\\speakerNormal.png",25,25,true,true,true)));
					muteButton.setSelected(false);
					player.setMute(false);
				}
			}
			
		});
		
		muteButton.setOnAction(new EventHandler<ActionEvent>(){

			public void handle(ActionEvent arg0) {
				if(muteButton.isSelected()){
					player.setMute(true);
					muteButton.setGraphic(new ImageView(new Image("C:\\Users\\Luchito Rojas\\Documents\\NetBeansProjects\\MusicPlayer\\src\\musicplayer\\speakerMute.png",25,25,true,true,true)));
				}
				else{
					player.setMute(false);
					muteButton.setGraphic(new ImageView(new Image("C:\\Users\\Luchito Rojas\\Documents\\NetBeansProjects\\MusicPlayer\\src\\musicplayer\\speakerNormal.png",25,25,true,true,true)));
				}
			}
			
		});
		
		randomButton.selectedProperty().addListener(new ChangeListener<Boolean>(){

			public void changed(ObservableValue<? extends Boolean> arg0,Boolean oldValue, Boolean newValue) {
				isRandom = newValue;
			}
			
		});
		
		
	}
	
	public void loadMediaPlayer(Media value){
		media = value;
		player = new MediaPlayer(media);
		
		player.currentTimeProperty().addListener(new ChangeListener<Duration>(){

			public void changed(ObservableValue<? extends Duration> observale,Duration oldVal, Duration newVal) {
				double time = newVal.toSeconds();
				double duration = player.getTotalDuration().toSeconds();
				int percent = (int) ((100*time)/duration);
				timeSlider.setValue(percent);
				currentTimeText.setText(formatTime(time));
			}
			
		});
		
		player.setOnEndOfMedia(new Runnable(){

			public void run() {
				player.stop();
				if(fileIndex < files.size()-1){
					fileIndex++;
					media = new Media(files.get(fileIndex).toURI().toString());
					loadMediaPlayer(media);
					player.play();
				}
				else{
					playButton.setGraphic(new ImageView(new Image("C:\\Users\\Luchito Rojas\\Documents\\NetBeansProjects\\MusicPlayer\\src\\musicplayer\\play.png",34,34,true,true,true)));
					fileIndex = 0;
					media = new Media(files.get(fileIndex).toURI().toString());
					loadMediaPlayer(media);
				}
				timeSlider.setValue(0);
				currentTimeText.setText("00:00:00");
			}
			
		});
		
		player.statusProperty().addListener(new ChangeListener<Status>(){

			public void changed(ObservableValue<? extends Status> arg0,Status oldStatus, Status newStatus) {
				if(newStatus == Status.READY){
					totalTimeText.setText(formatTime(player.getTotalDuration().toSeconds()));
					titleText.setText(new File(media.getSource()).getName().replace("%20", " "));
					player.setVolume(volume);
				}
			}
			
		});
		
		player.volumeProperty().addListener(new ChangeListener<Number>(){

			public void changed(ObservableValue<? extends Number> arg0,Number oldValue, Number newValue) {
				volume = newValue.doubleValue();
			}
			
		});
		
		setControlEvents();
	}
	
	public void searchAudio(File folder){
		System.out.println("Searching in: "+folder.toPath());
		File[] folderContent = folder.listFiles();
		int contentSize = folderContent.length;
		for(int i = 0; i < contentSize; i++){
			File tempFile = folderContent[i];
			System.out.println("Checking file: "+tempFile.toPath());
			if(!tempFile.isHidden()){
				if(tempFile.isDirectory() && !Files.isSymbolicLink(tempFile.toPath())){
					continue;
				}
				if(tempFile.isFile()){
					if( tempFile.getName().endsWith(".mp3") || 
						tempFile.getName().endsWith(".aac") || 
						tempFile.getName().endsWith(".aif") || 
						tempFile.getName().endsWith(".aiff")|| 
						tempFile.getName().endsWith(".mp4") || 
						tempFile.getName().endsWith(".m4a") || 
						tempFile.getName().endsWith(".m4v") || 
						tempFile.getName().endsWith(".wav") ||
						tempFile.getName().endsWith(".Mp3") || 
						tempFile.getName().endsWith(".Aac") || 
						tempFile.getName().endsWith(".Aif") || 
						tempFile.getName().endsWith(".Aiff")|| 
						tempFile.getName().endsWith(".Mp4") || 
						tempFile.getName().endsWith(".M4a") || 
						tempFile.getName().endsWith(".M4v") || 
						tempFile.getName().endsWith(".Wav")){
							
							System.out.println(" >> File added: "+tempFile.getPath());
							files.add(tempFile);
						}
				}
			}
		}
	}
	
	private String formatTime(double time){
		int hours   = 0;
		int minutes = 0;
		int seconds = 0;
		
		hours = (int) (time/3600);
		if(hours > 0){
			time -= 3600*hours;
		}
		minutes = (int) (time/60);
		if(minutes > 0){
			time -= 60*minutes;
		}
		seconds = (int) time;
		return String.format("%02d:%02d:%02d", hours,minutes,seconds);
	}

}
