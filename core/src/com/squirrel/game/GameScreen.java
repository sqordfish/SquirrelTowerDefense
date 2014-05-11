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

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.loaders.resolvers.ExternalFileHandleResolver;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.tiled.TideMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class GameScreen implements Screen {
	//The rest of the game scales off these variables
	static final int WIDTH = 1280;
	static final int HEIGHT = 720;
	static final int TILE_SIZE = 80;
	static final int SPAWN_X = 0;
	static final int SPAWN_Y = ScreenInfo.HEIGHT / 2 - ScreenInfo.TILE_SIZE;
	static final int GOAL_X = ScreenInfo.WIDTH - 2 * ScreenInfo.TILE_SIZE; 
	static final int GOAL_Y = ScreenInfo.HEIGHT / 2;
	static final int DIFFICULTY_MODIFIER = 1;
	
	
	//Instance fields
	SpriteBatch batch;
	Texture squirrelImage;
	Texture stickImage;
	Texture towerImage;
	Texture projectileImage;
	OrthographicCamera camera;
	Array<Enemy> squirrels;
	Array<Rectangle> sticks;
	Array<Tower> towers;
	Squirrel squirrel;
	TiledMap map;
	private OrthogonalTiledMapRenderer renderer;
	Texture wallImage;
	PathFinder pathFinder;
	private long lastSpawnTime;
	
	WaveOne wave;
	Array<Wave> waves;
	Array<Trap> traps;
	Texture trapImage;
	Game game;
	Stage stage;
	Texture mapImage;
	private Sprite mapSprite;
	
	public GameScreen(Game game) {
		this.game = game;
		show();
	}
	
	@Override
	public void show () {
		batch = new SpriteBatch();
		
		//Create textures for the images 
		squirrelImage = new Texture(Gdx.files.internal("squirrel.png"));
		stickImage = new Texture(Gdx.files.internal("stick.gif"));
		wallImage = new Texture(Gdx.files.internal("wall.png"));
		towerImage = new Texture(Gdx.files.internal("Tower.png"));
		projectileImage = new Texture(Gdx.files.internal("Projectile.png"));
		trapImage = new Texture(Gdx.files.internal("Trap.png"));
//		mapImage = new Texture(Gdx.files.internal("level1final.png"));
		
		//Setup camera, will be static for this game
		camera = new OrthographicCamera();
		camera.setToOrtho(false, ScreenInfo.WIDTH, ScreenInfo.HEIGHT);
		
		//Lists to hold objects that are created  
		squirrels = new Array<Enemy>();
		sticks = new Array<Rectangle>();
		
		waves = new Array<Wave>();
		traps = new Array<Trap>();
		towers = new Array<Tower>();
		
		map = new TmxMapLoader().load("level1final.tmx");
//		mapSprite = new Sprite(mapImage);
//		mapSprite.setSize(ScreenInfo.WIDTH, ScreenInfo.HEIGHT);
		
		//The map layer that will hold created objects and detect collisions
//		collisionLayer = createCollisionLayer();
		map.getLayers().add(new TiledMapTileLayer(WIDTH/TILE_SIZE, HEIGHT/TILE_SIZE, TILE_SIZE, TILE_SIZE));
		pathFinder = new PathFinder((TiledMapTileLayer) map.getLayers().get(0));
		
		//Spawn a test squirrel
		spawnSquirrel();
		
		//Setup the renderer
		renderer = new OrthogonalTiledMapRenderer(map);
		renderer.setView(camera);
		
	}

	@Override
	public void render (float delta) {
//		super.render(delta);
		
		//Clears the screen and sets a background;
		Gdx.gl.glClearColor(255, 255, 255, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		//Needed
		camera.update();
		renderer.render();
		
		/*
		 * BEGINS RENDERING
		 * Draw all of the objects here
		 */
		renderer.getSpriteBatch().begin();
//		mapSprite.draw(renderer.getSpriteBatch());
		
		for (Tower t : towers) {
			t.draw(renderer.getSpriteBatch());
			t.updatePossibleTargets(squirrels);
		}
		
		for (Trap t : traps) {
			t.draw(renderer.getSpriteBatch());
			t.updateEnemies(squirrels);
		}
		
		for (Enemy s : squirrels) {
			s.draw(renderer.getSpriteBatch());
		}
		
		for (Wave w : waves) {
			w.draw(renderer.getSpriteBatch());
		}
		renderer.getSpriteBatch().end();
		/*
		 * RENDERING ENDED
		 */
		
		//Create a stick where the user touches
//		if (Gdx.input.isTouched()) {
//			spawnTrap();
//		}
		
		//Uncomment to spawn a squirrel every few seconds
//		if (TimeUtils.nanoTime() - lastSpawnTime > 1000000000) {
//			spawnSquirrel();
//		}
		
		//test wave if W is pressed
		if (Gdx.input.isKeyPressed(Keys.W)) {
			spawnWave();
		}
		//Spawn a squirrel when A is pressed
		if (Gdx.input.isKeyPressed(Keys.A)) {
			spawnSquirrel();
		}
		//test tower if t is pressed
		if (Gdx.input.isKeyPressed(Keys.T)) {
			spawnTower();
		}
		//test trap if R is pressed
		if (Gdx.input.isKeyPressed(Keys.R)) {
			spawnTrap();
		}
		
		//Remove squirrels that have reached the goal
		for (int i = 0; i < squirrels.size; i++) {
			if (squirrels.get(i).hasReachedGoal() || squirrels.get(i).isDead()) {
				squirrels.removeIndex(i);
			}
		}
		
		//remove destroyed traps
		for (int i = 0; i < traps.size; i++) {
			if (traps.get(i).isDestroyed()) {
				((TiledMapTileLayer) map.getLayers().get(0)).setCell(ScreenInfo.toMapCoordinate(traps.get(i).getX()),
						ScreenInfo.toMapCoordinate(traps.get(i).getY()), null);
				traps.removeIndex(i);
			}
		}
	}
	
	/*
	 * 
	 * IGNRORE THIS FOR NOW
	 * 
	 */
//	public TiledMapTileLayer createCollisionLayer() {
//		TiledMapTileLayer layer = new TiledMapTileLayer
//				(ScreenInfo.WIDTH/ScreenInfo.TILE_SIZE,
//				ScreenInfo.HEIGHT/ScreenInfo.TILE_SIZE, 
//				ScreenInfo.TILE_SIZE, ScreenInfo.TILE_SIZE);
//		
//		Cell cell;
//		TextureRegion region;
//		StaticTiledMapTile newTile;
//		
//		for (int x = 0; x < layer.getWidth(); x++) {
//			for (int y = 0; y < layer.getHeight(); y++) {
//				cell = new Cell();
//				region = new TextureRegion(new Texture(), ScreenInfo.TILE_SIZE, ScreenInfo.TILE_SIZE);
//				newTile = new StaticTiledMapTile(region);
//				newTile.getProperties().put("blocked", true);
//				newCell.setTile(newTile);
//				layer.setCell(x, y, new Cell)
//			}
//		}
//		
//		
//		
//		
//		return layer;
//	}
	
	/**
	 * Converts coordinate from screen WIDTH x HEIGHT to the tile coordinate
	 * @param coord
	 * @return the converted coordinate value
	 */
	static public int convertCoordinate(float coord) {
		return ((int) Math.round(coord / TILE_SIZE));
	}
	
	/**
	 * Creates a new squirrel
	 */
	private void spawnSquirrel() {
		Array<Vector2> path = new Array<Vector2>();
		
		//Update path finder with the updated map
		pathFinder = new PathFinder((TiledMapTileLayer) map.getLayers().get(0));
		
		//Find shortest path from Spawn to Goal.
		path = pathFinder.findShortestPath(
				new Vector2(convertCoordinate(SPAWN_X), convertCoordinate(SPAWN_Y)), 
				new Vector2(convertCoordinate(GOAL_X), convertCoordinate(GOAL_Y)));
		Squirrel newSquirrel = new Squirrel(SPAWN_X, SPAWN_Y, path);
		
		squirrels.add(newSquirrel);
		lastSpawnTime = TimeUtils.nanoTime();
	}
	
	private void spawnWave() {
		waves.add(new WaveOne((TiledMapTileLayer) map.getLayers().get(0),
				new Vector2(convertCoordinate(SPAWN_X), convertCoordinate(SPAWN_Y)), 
				new Vector2(convertCoordinate(GOAL_X), convertCoordinate(GOAL_Y))));
	}
	
	private void spawnTrap() {
		Rectangle stick = new Rectangle();
		Vector3 touchPos = new Vector3();
		
		//Get the spot the user touched
		touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		
		//Convert the units to our camera units
		camera.unproject(touchPos);
		
		//Set in middle of tile
		stick.x = touchPos.x - TILE_SIZE / 2;
		stick.y = touchPos.y - TILE_SIZE / 2;
		
		TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(0);
		Cell cell = layer.getCell(convertCoordinate(stick.x), convertCoordinate(stick.y));
		
		//If there is not already a something there
		if (cell == null) {
			//Create new wall and put it at that spot
			Cell newCell = new Cell();
			StaticTiledMapTile newTile = new StaticTiledMapTile(new TextureRegion(trapImage, TILE_SIZE, TILE_SIZE));
			newCell.setTile(newTile);
			layer.setCell(convertCoordinate(stick.x), convertCoordinate(stick.y), newCell);
			float trapX = convertCoordinate(stick.x)*TILE_SIZE;
			float trapY = convertCoordinate(stick.y)*TILE_SIZE;
			traps.add(new StickTrap(trapX, trapY, squirrels));
		}
	}
	
	/**
	 * Creates a new stick (acts as a wall)
	 */
	private void spawnStick() {
		Rectangle stick = new Rectangle();
		Vector3 touchPos = new Vector3();
		
		//Get the spot the user touched
		touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		
		//Convert the units to our camera units
		camera.unproject(touchPos);
		
		//Set in middle of tile
		stick.x = touchPos.x - TILE_SIZE / 2;
		stick.y = touchPos.y - TILE_SIZE / 2;
		
		TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(0);
		Cell cell = layer.getCell(convertCoordinate(stick.x), convertCoordinate(stick.y));
		
		//If there is not already a something there
		if (cell == null) {
			//Create new wall and put it at that spot
			Cell newCell = new Cell();
			StaticTiledMapTile newTile = new StaticTiledMapTile(new TextureRegion(wallImage, TILE_SIZE, TILE_SIZE));
			newTile.getProperties().put("blocked", true);
			newCell.setTile(newTile);
			layer.setCell(convertCoordinate(stick.x), convertCoordinate(stick.y), newCell);
		}
	}

	/**
	 * Creates a new tower (acts as a wall)
	 */
	private void spawnTower() {
		Rectangle stick = new Rectangle();
		Vector3 touchPos = new Vector3();
		
		//Get the spot the user touched
		touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		
		//Convert the units to our camera units
		camera.unproject(touchPos);
		
		//Set in middle of tile
		stick.x = touchPos.x - TILE_SIZE / 2;
		stick.y = touchPos.y - TILE_SIZE / 2;
		
		TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(0);
		Cell cell = layer.getCell(convertCoordinate(stick.x), convertCoordinate(stick.y));
		
		//If there is not already a something there
		if (cell == null || !(cell.getTile().getProperties().containsKey("blocked"))) {
			//Create new wall and put it at that spot
			Cell newCell = new Cell();
			TextureRegion region = new TextureRegion(towerImage, TILE_SIZE, TILE_SIZE);
			StaticTiledMapTile newTile = new StaticTiledMapTile(region);
			newTile.getProperties().put("blocked", true);
			newCell.setTile(newTile);
			layer.setCell(convertCoordinate(stick.x), convertCoordinate(stick.y), newCell);
			
			float towerX = convertCoordinate(stick.x)*TILE_SIZE;
			float towerY = convertCoordinate(stick.y)*TILE_SIZE;
			towers.add(new StickTower(towerX, towerY, squirrels));
		}	
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
}
