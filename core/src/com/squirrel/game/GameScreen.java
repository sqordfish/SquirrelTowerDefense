/**
 * This is the main game screen for Squirrel Defense. All of the 
 * game's main logic should be put into the render function here.
 */
/*
 * 
 * CURRENTLY JUST BEING USED FOR TESTING. WILL CHANGE A LOT
 * 
 *
 * 
 */
package com.squirrel.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.squirrel.game.enemy.Enemy;
import com.squirrel.game.structure.BuffTower;
import com.squirrel.game.structure.LifeTower;
import com.squirrel.game.structure.MaxTower;
import com.squirrel.game.structure.ResourceTower;
import com.squirrel.game.structure.StickTower;
import com.squirrel.game.structure.StickTrap;
import com.squirrel.game.structure.Structure;
import com.squirrel.game.structure.Tower;
import com.squirrel.game.structure.Trap;
import com.squirrel.game.wave.Wave;
import com.squirrel.game.wave.WaveFive;
import com.squirrel.game.wave.WaveFour;
import com.squirrel.game.wave.WaveOne;
import com.squirrel.game.wave.WaveThree;
import com.squirrel.game.wave.WaveTwo;

public class GameScreen implements Screen {
	//The rest of the game scales off these variables
	static final int SPAWN_X = 0;
	static final int SPAWN_Y = ScreenInfo.HEIGHT / 2 - ScreenInfo.TILE_SIZE;
	static final int GOAL_X = ScreenInfo.WIDTH - 2 * ScreenInfo.TILE_SIZE; 
	static final int GOAL_Y = ScreenInfo.HEIGHT / 2;
	private static double damageMultiplier = 1;
	public static double resourceMultiplier = 1;
	static int lifeTowers = 0;
	static int difficulty  = 100;
	static boolean hasWon = false;
	
	//Textures for the images
	SpriteBatch batch;
	Texture squirrelImage;
	Texture stickImage;
	Texture towerImage;
	Texture projectileImage;
	Texture mapImage;
	Texture trapImage;
	Texture wallImage;
	private Sprite mapSprite;
	TextureRegion library;
	Image goalImage;
	
	//For rendering and configuring the map and view
	OrthographicCamera camera;
	TiledMap map;
	private OrthogonalTiledMapRenderer renderer;
	Game game;
	Stage stage;
	
	//GUI variables
	Skin skin;
	Table table;
	SelectBox<String> structureSelect;
	String[] structureList = {"Stick Tower (15)", 
			"Buff Tower (35)", 
			"Life Tower (75)", 
			"Resource Tower (75)", 
			"Stick Trap (5)",  
			"Professor Max (500)"};
	Label stoneDisplay, woodDisplay, lifeDisplay, errorMessage, waveOutput;
	TextButton deleteTowerButton;
	TextButton deleteTrapButton;
	TextButton deleteButton;
	TextButton nextButton;
	
	//Variables for updating the game world
	Array<Enemy> enemies;
	Array<Tower> towers;
	Array<Trap> traps;
	Array<Wave> waves;
	Wave currentWave;
	boolean waveInProgress;
	Player player;
	Vector2 spawn;
	Vector2 goal;
	Structure selectedStructure;
	Tower selectedTower;
	Trap selectedTrap;
	PathFinder pathFinder;	
	TiledMapTileLayer mainLayer;

	//Variables for sound
	Sound waveAudio;
	Sound needMoreAudio;
	
	public GameScreen(Game game) {
		this.game = game;
		if(StartMenuScreen.difficulty.getSelection().toString().equals("{Normal (100 Lives)}")){
			difficulty  = 100;
		}
		else if(StartMenuScreen.difficulty.getSelection().toString().equals("{Hard (20 Lives)}")){
			difficulty  = 20;
		}
		else if(StartMenuScreen.difficulty.getSelection().toString().equals("{Vincent (1 Life)}")){
			difficulty  = 1;
		}
		setDamageMultiplier(1);
		resourceMultiplier = 1;
		lifeTowers = 0;
		hasWon = false;
		show();
	}
	
	public GameScreen(Game game, boolean replay) {
		this.game = game;
		if(EndScreen.difficulty.getSelection().toString().equals("{Normal (100 Lives)}")){
			difficulty  = 100;
		}
		else if(EndScreen.difficulty.getSelection().toString().equals("{Hard (20 Lives)}")){
			difficulty  = 20;
		}
		else if(EndScreen.difficulty.getSelection().toString().equals("{Vincent (1 Life)}")){
			difficulty  = 1;
		}
		setDamageMultiplier(1);
		resourceMultiplier = 1;
		lifeTowers = 0;
		hasWon = false;
		show();
	}

	@Override
	public void show () {
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);
		batch = new SpriteBatch();
	    table = new Table();
	    table.setFillParent(true);
	    stage.addActor(table);

		//Create textures for the images 
		squirrelImage = new Texture(Gdx.files.internal("squirrel.png"));
		stickImage = new Texture(Gdx.files.internal("stick.gif"));
		wallImage = new Texture(Gdx.files.internal("wall.png"));
		towerImage = new Texture(Gdx.files.internal("Tower.png"));
		projectileImage = new Texture(Gdx.files.internal("Projectile.png"));
		trapImage = new Texture(Gdx.files.internal("Trap.png"));
		mapImage = new Texture(Gdx.files.internal("level1final.png"));
		
	    //Setup audio
//	    waveAudio = Gdx.audio.newSound(Gdx.files.internal("waveClip.wav"));
//	    needMoreAudio = Gdx.audio.newSound(Gdx.files.internal("resourcesClip.wav"));
	    
		//Setup camera, will be static for this game
		camera = new OrthographicCamera();
		camera.setToOrtho(false, ScreenInfo.WIDTH, ScreenInfo.HEIGHT);
		
		//Lists to hold game objects that are created and updated in main loop  
		waves = new Array<Wave>();
		traps = new Array<Trap>();
		towers = new Array<Tower>();
		enemies = new Array<Enemy>();
		
		player = new Player();
		
		//Create default map stuff
		map = new TmxMapLoader().load("level1final.tmx");
		mainLayer = (TiledMapTileLayer) map.getLayers().get(0);
		spawn = new Vector2(ScreenInfo.toMapCoordinate(SPAWN_X), ScreenInfo.toMapCoordinate(SPAWN_Y));
		goal = new Vector2(ScreenInfo.toMapCoordinate(GOAL_X), ScreenInfo.toMapCoordinate(GOAL_Y));
		mapSprite = new Sprite(mapImage);
		mapSprite.setSize(ScreenInfo.WIDTH, ScreenInfo.HEIGHT);
		pathFinder = new PathFinder(mainLayer);
		
		//Add each wave to the array of waves, reverse order
		waves.add(new WaveFive(mainLayer, player, spawn, goal));
		waves.add(new WaveFour(mainLayer, player, spawn, goal));
		waves.add(new WaveThree(mainLayer, player, spawn, goal));
		waves.add(new WaveTwo(mainLayer, player, spawn, goal));
		waves.add(new WaveOne(mainLayer, player, spawn, goal));
		waveInProgress = false;
		
		//Setup the library (the goal)
		library = new TextureRegion(new Texture(Gdx.files.internal("library.png")));
		goalImage = new Image(library);
		goalImage.setHeight(50f);
		goalImage.setWidth(50f);
		goalImage.setX(stage.getWidth()-goalImage.getWidth());
		goalImage.setY(stage.getHeight()/2);

		//Setup the renderer
		renderer = new OrthogonalTiledMapRenderer(map);
		renderer.setView(camera);
	
		//Creates the SelectBox for Tower Selection
		skin = new Skin(Gdx.files.internal("defaultskin.json"));
		structureSelect = new SelectBox<String>(skin);
	    structureSelect.setItems(structureList);
	    structureSelect.sizeBy(150, 5);
	    structureSelect.setX(stage.getWidth()-structureSelect.getWidth());	// USE TO CHANGE THE LOCATION OF THE SELECT BOX
	    structureSelect.setY(stage.getHeight()-structureSelect.getHeight());
	     
	    //Creates the "Next Wave" button
	    nextButton = new TextButton("Next Wave", skin);
	    nextButton.addListener(new ClickListener() {
	    	public void clicked(InputEvent event, float x, float y) {
	    		if (!waveInProgress) {
	    			currentWave = waves.pop();
	    			currentWave.updateMap(mainLayer);
	    			waveInProgress = true;
	    			enemies = currentWave.getSpawnedEnemies();
	    			errorMessage.setVisible(false);
//	    			 waveAudio.play();
	    		}
	    	}
	    });
	    nextButton.sizeBy(20, 20);
	    nextButton.setX(stage.getWidth()-nextButton.getWidth());
	    
	    //Creates the "Delete tower" button
	    deleteTowerButton = new TextButton("Destroy Tower", skin);
	    deleteTowerButton.addListener(new ClickListener() {
	    	public void clicked(InputEvent event, float x, float y) {
	    		//Refund the player
	    		player.addWood(selectedTower.getCost());
	    		
	    		//delete the structure from map
	    		mainLayer.setCell(ScreenInfo.toMapCoordinate(selectedTower.getX()), 
	    				ScreenInfo.toMapCoordinate(selectedTower.getY()), null);
	    		
	    		//remove structure from the array of structures
	    		for (int i = 0; i < towers.size; i++) {
	    			if (towers.get(i).getX() == selectedTower.getX() &&
	    					towers.get(i).getY() == selectedTower.getY()) {
	    				towers.get(i).dispose();
	    				towers.removeIndex(i);
	    				break;
	    			}
	    		}
	    		
	    		deleteTowerButton.setVisible(false);
	    	}
	    });
	    deleteTowerButton.sizeBy(20, 20);
	    deleteTowerButton.setX((stage.getWidth() - deleteTowerButton.getWidth()) / 2);
	    deleteTowerButton.setVisible(false);

	    //Creates the "Delete trap" button
	    deleteTrapButton = new TextButton("Destroy Trap", skin);
	    deleteTrapButton.addListener(new ClickListener() {
	    	public void clicked(InputEvent event, float x, float y) {
	    		//Refund the player
	    		player.addWood(selectedTrap.getCost());
	    		
	    		//delete the structure from map
	    		mainLayer.setCell(ScreenInfo.toMapCoordinate(selectedTrap.getX()), 
	    				ScreenInfo.toMapCoordinate(selectedTrap.getY()), null);
	    		
	    		//remove structure from the array of structures
	    		for (int i = 0; i < traps.size; i++) {
	    			if (traps.get(i).getX() == selectedTrap.getX() &&
	    					traps.get(i).getY() == selectedTrap.getY()) {
	    				traps.get(i).dispose();
	    				traps.removeIndex(i);
	    				break;
	    			}
	    		}
	    		
	    		deleteTrapButton.setVisible(false);

	    	}
	    });
	    deleteTrapButton.sizeBy(20, 20);
	    deleteTrapButton.setX((stage.getWidth() - deleteTowerButton.getWidth()) / 2);
	    deleteTrapButton.setVisible(false);
	 
	    /*
	    stoneDisplay = new Label("Stone: " + player.getStone() + "", skin);
	    stoneDisplay.setX((stage.getWidth() - stoneDisplay.getWidth())/2);
	    stoneDisplay.setY(woodDisplay.getY() - stoneDisplay.getHeight());
	    table.addActor(stoneDisplay);
	     */
	    
	    //Setup the text displays
	    waveOutput = new Label(waves.peek().getMessage(), skin);
	    waveOutput.setY(stage.getHeight() - waveOutput.getHeight());
	    lifeDisplay = new Label("Lives: " + player.getLives(), skin);
	    lifeDisplay.setY(waveOutput.getY() - lifeDisplay.getHeight());
	    woodDisplay = new Label("Wood: " + player.getWood() + "", skin);
	    woodDisplay.setY(lifeDisplay.getY() - woodDisplay.getHeight());
	    errorMessage = new Label("Errors Go Here", skin);
	    errorMessage.setVisible(false);
		
	    //Add gui elements
	    table.addActor(lifeDisplay);
	    table.addActor(woodDisplay);
		table.addActor(waveOutput);
	    table.addActor(errorMessage);
	    table.addActor(structureSelect);
	    table.addActor(nextButton);
	    table.addActor(deleteTowerButton);
	    table.addActor(deleteTrapButton);
		table.addActor(goalImage);
	}

	@Override
	public void render (float delta) {
		//Clears the screen and sets a background;
		Gdx.gl.glClearColor(255, 255, 255, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		//Needed
		camera.update();
		renderer.render();
		
//		pathFinder.updateMap(mainLayer);
		
		//Determine if wave is over
		if (waveInProgress && currentWave.isOver()) {
			//End game if it is over
			if (waves.size == 0) {
				hasWon = true;
				game.setScreen(new EndScreen(game));
			} else {
				waveInProgress = false;
				player.addWood(currentWave.getWoodReward());
				Label temp = new Label(waves.peek().getMessage(), skin);
				waveOutput.setText(waves.peek().getMessage());
				waveOutput.setY(stage.getHeight() - temp.getHeight());
				waveOutput.setVisible(true);
			}
		} else if (player.getLives() == 0) {
			game.setScreen(new EndScreen(game));
		}
		
		/*
		 * BEGINS RENDERING
		 * Draw all of the objects here
		 */
		renderer.getSpriteBatch().begin();
		mapSprite.draw(renderer.getSpriteBatch());
		
		for (Tower t : towers) {
			t.draw(renderer.getSpriteBatch());
			t.updatePossibleTargets(enemies);
		}
		
		for (Trap t : traps) {
			t.draw(renderer.getSpriteBatch());
			t.updateEnemies(enemies);
		}

		if (waveInProgress) {
			currentWave.draw(renderer.getSpriteBatch());
		}
		renderer.getSpriteBatch().end();
		/*
		 * RENDERING ENDED
		 */
		
		//Try to create structure where touched
		if (Gdx.input.justTouched()) {
			spawnStructure();
		}

		//remove destroyed traps
		for (int i = 0; i < traps.size; i++) {
			if (traps.get(i).isDestroyed()) {
				(mainLayer).setCell(ScreenInfo.toMapCoordinate(traps.get(i).getX()),
						ScreenInfo.toMapCoordinate(traps.get(i).getY()), null);
				traps.get(i).dispose();
				traps.removeIndex(i);
			}
		}
		
		//STUFF TO CREATE THE SELECTBOX
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
		Table.drawDebug(stage);
		
		lifeDisplay.setText("Lives: " + player.getLives());
		woodDisplay.setText("Wood: " + player.getWood());
		//stoneDisplay.setText("Stone: " + player.getStone());
		
	}

	/**
	 * Tries to spawn a new structure where the user clicked
	 */
	private void spawnStructure() {
		Vector3 touchPos = new Vector3();
		
		//Get the spot the user touched
		touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		
		//Convert the units to our camera units
		camera.unproject(touchPos);
		
		//Set in middle of tile
		float xPos = touchPos.x - ScreenInfo.TILE_SIZE / 2;
		float yPos = touchPos.y - ScreenInfo.TILE_SIZE / 2;

		Cell cell = mainLayer.getCell(ScreenInfo.toMapCoordinate(xPos), ScreenInfo.toMapCoordinate(yPos));
		
		
		if (cell == null || (!(cell.getTile().getProperties().containsKey("blocked")) && 
				!(cell.getTile().getProperties().containsKey("trap")))) {

			//Unselect whatever was selected
			selectedStructure = null;
			selectedTower = null;
			selectedTrap = null;
			deleteTowerButton.setVisible(false);
			deleteTrapButton.setVisible(false);

			
			String structureChosen = structureSelect.getSelected();
			float structX = ScreenInfo.toMapCoordinate(xPos) * ScreenInfo.TILE_SIZE;
			float structY = ScreenInfo.toMapCoordinate(yPos) * ScreenInfo.TILE_SIZE;		
			
			switch (structureChosen) {
			case "Stick Tower ("+StickTower.COST+")": spawnTower(xPos, yPos, new StickTower(structX, structY, enemies));
				break;
			case "Buff Tower ("+BuffTower.COST+")": spawnTower(xPos, yPos, new BuffTower(structX, structY, enemies));
				break;
			case "Life Tower ("+LifeTower.COST+")": spawnTower(xPos, yPos, new LifeTower(structX, structY, enemies));
				break;
			case "Resource Tower ("+ResourceTower.COST+")": spawnTower(xPos, yPos, new ResourceTower(structX, structY, enemies));
				String[] update =  {"Stick Tower (15)", 
						"Buff Tower (35)", 
						"Life Tower (75)", 
						"Stick Trap (5)",  
						"Professor Max (500)"};
			structureList = update;
			structureSelect.setItems(structureList);

				break;
			case "Stick Trap ("+StickTrap.COST+")": spawnTrap(xPos, yPos, new StickTrap(structX, structY, enemies));
				break;
			case "Professor Max ("+MaxTower.COST+")": spawnTower(xPos, yPos, new MaxTower(structX, structY, enemies));
				break;
		}

			//TODO WHAT IF PLAYER WANTS TO UPGRADE??
		} else if (cell.getTile().getProperties().containsKey("tower")) {	
			selectedTower = (Tower) cell.getTile().getProperties().get("tower");
			deleteTrapButton.setVisible(false);
			deleteTowerButton.setVisible(true);
		} else if (cell.getTile().getProperties().containsKey("trap")) {
			errorMessage.setText("Trap selected");
			errorMessage.setVisible(true);
			selectedTrap = (Trap) cell.getTile().getProperties().get("trap");
			deleteTowerButton.setVisible(false);
			deleteTrapButton.setVisible(true);

		}
	}
	
	/**
	 * Spawns a new tower
	 * @param x The x position of the tower
	 * @param y The y position of the tower
	 * @param tower The tower to be spawned
	 */
	private void spawnTower(float x, float y, Tower tower) {
		
		//Create new structure and put it at that spot
		Cell newCell = new Cell();
		Cell oldCell = mainLayer.getCell(ScreenInfo.toMapCoordinate(x), ScreenInfo.toMapCoordinate(y));
		TextureRegion region = new TextureRegion(tower.getTexture());
		StaticTiledMapTile newTile = new StaticTiledMapTile(region);
		newTile.getProperties().put("blocked", true);
		newTile.getProperties().put("tower", tower);
		newCell.setTile(newTile);
		mainLayer.setCell(ScreenInfo.toMapCoordinate(x), ScreenInfo.toMapCoordinate(y), newCell);

		//Check if this will block the path
		//Update pathFinder with the map layer
		pathFinder.updateMap(mainLayer);
		
		//If not enough resources OR
		//If no path exists to the goal, do not build the tower.
		if(player.getWood() < tower.getCost()){
			errorMessage.setText("Insufficient Wood: Cannot Build Tower");
			errorMessage.setVisible(true);
//			needMoreAudio.play();
			mainLayer.setCell(ScreenInfo.toMapCoordinate(x), ScreenInfo.toMapCoordinate(y), oldCell);
		}

		else if(pathFinder.findShortestPath(
				new Vector2(ScreenInfo.toMapCoordinate(SPAWN_X), ScreenInfo.toMapCoordinate(SPAWN_Y)), 
				new Vector2(ScreenInfo.toMapCoordinate(GOAL_X), ScreenInfo.toMapCoordinate(GOAL_Y))) == null){
			errorMessage.setText("Cannot build tower: You must leave the squirrels a path!");
			errorMessage.setVisible(true);
			mainLayer.setCell(ScreenInfo.toMapCoordinate(x), ScreenInfo.toMapCoordinate(y), oldCell);
			return;
		} 
		else {
			//Update game stuff
			towers.add(tower);
			errorMessage.setVisible(false);
			player.decreaseWood(tower.getCost());
			
			//Must update the pathfinder and enemy paths because
			//towers can block the paths
			updatePaths();
		}
	}

	/**
	 * Updates the pathfinder and enemies paths
	 */
	public void updatePaths() {
		if (currentWave != null) {
			currentWave.updateMap(mainLayer);
			currentWave.updateEnemyPaths();
		}
		pathFinder.updateMap(mainLayer);
	}
	
	/**
	 * Spawns a new trap
	 * @param x The x position of the trap
	 * @param y The y position of the trap
	 * @param trap The trap to be spawned
	 */
	private void spawnTrap(float x, float y, Trap trap) {
		//Don't let it be built if the player doesnt have enough wood
		//or there is already a trap in this cell
		Cell cell = mainLayer.getCell(ScreenInfo.toMapCoordinate(x), 
				ScreenInfo.toMapCoordinate(y));
		if (player.getWood() < trap.getCost()) {
			errorMessage.setText("Insufficient Wood: Cannot Build Trap");
			errorMessage.setVisible(true);
//			needMoreAudio.play();
			return;
		}
		else if(cell != null && cell.getTile().getProperties().containsKey("trap")){
			return;
		}
		
		//Create new structure and put it at that spot
		Cell newCell = new Cell();
		TextureRegion region = new TextureRegion(trap.getTexture());
		StaticTiledMapTile newTile = new StaticTiledMapTile(region);
		newTile.getProperties().put("trap", trap);
		newCell.setTile(newTile);
		mainLayer.setCell(ScreenInfo.toMapCoordinate(x), ScreenInfo.toMapCoordinate(y), newCell);

		//Update game stuff
		traps.add(trap);
		player.decreaseWood(trap.getCost());
		errorMessage.setVisible(false);
	}
	
	@Override
	public void dispose() {
		squirrelImage.dispose();
		stickImage.dispose();
		towerImage.dispose();
		wallImage.dispose();
		projectileImage.dispose();
		batch.dispose();
		renderer.dispose();
		stage.dispose();
	}
	
    @Override
    public void resize(int width, int height) {
	}

	@Override
	public void pause() {
    }

   	@Override
    public void resume() {
    }

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	public static double getDamageMultiplier() {
		return damageMultiplier;
	}

	public static void setDamageMultiplier(double damageMultiplier) {
		GameScreen.damageMultiplier = damageMultiplier;
	}

}
