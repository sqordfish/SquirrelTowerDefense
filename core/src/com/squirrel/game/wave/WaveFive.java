/**
 * The five wave of the game.
 * 
 * @author Jacob Rust
 */

package com.squirrel.game.wave;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.squirrel.game.Player;
import com.squirrel.game.ScreenInfo;
import com.squirrel.game.enemy.ArcticSquirrel;
import com.squirrel.game.enemy.BossSquirrel;
import com.squirrel.game.enemy.ChickfilASquirrel;
import com.squirrel.game.enemy.Enemy;
import com.squirrel.game.enemy.Squirrel;
import com.squirrel.game.enemy.WildSquirrel;

public class WaveFive extends Wave {
	//Change these for balancing
	static final int NUM_OF_SQUIRRELS = 20;
	static final int NUM_OF_WILD_SQUIRRELS = 20;
	static final int NUM_OF_ARCTIC_SQUIRRELS = 20;
	static final int NUM_OF_CHICK_SQUIRRELS = 20;
	static final int NUM_OF_BOSS_SQUIRRELS = 1;
	static final int WOOD_REWARD = 100;
	static final int STONE_REWARD = 10;
	static final String MESSAGE = "Final Wave: " + " 20 of every squirrel you have seen!" +
				" With maybe a surprise...";

	/**
	 * Constructs an object representing the first wave
	 * @param mapLayer The map layer the enemies will move on
	 * @param spawn Position that the enemies will spawn at IN MAP TILES COORDINATES
	 * @param goal Position that the enemies are trying to reach IN MAP TILES COORDINATES
	 */
	public WaveFive(TiledMapTileLayer mapLayer, Player player, Vector2 spawn, Vector2 goal) {
		super(mapLayer, player, MESSAGE, WOOD_REWARD, STONE_REWARD, spawn, goal);
		createEnemies();
	}
	
	/**
	 * Creates and sets the enemies for this wave
	 */
	protected void createEnemies() {
		Array<Enemy> enemies = new Array<Enemy>();
		
		//Create the appropriate amount of boss squirrels
		for (int i = 0; i < NUM_OF_BOSS_SQUIRRELS; i++) {
			//The squirrels position must be converted back to screen coordinates
			enemies.add(new BossSquirrel(ScreenInfo.toScreenCoordinate(getSpawn().x), 
					ScreenInfo.toScreenCoordinate(getSpawn().y), getSpawn(),
					getGoal(), getPath()));
		}
		for (int i = 0; i < NUM_OF_WILD_SQUIRRELS/2; i++) {
			//The squirrels position must be converted back to screen coordinates
			enemies.add(new WildSquirrel(ScreenInfo.toScreenCoordinate(getSpawn().x), 
					ScreenInfo.toScreenCoordinate(getSpawn().y), getSpawn(),
					getGoal(), getPath()));
		}
		//Create the appropriate amount of chickfila squirrels
		for (int i = 0; i < NUM_OF_CHICK_SQUIRRELS; i++) {
			//The squirrels position must be converted back to screen coordinates
			enemies.add(new ChickfilASquirrel(ScreenInfo.toScreenCoordinate(getSpawn().x), 
					ScreenInfo.toScreenCoordinate(getSpawn().y), getSpawn(),
					getGoal(), getPath()));
		}
		//Create the appropriate amount of arctic squirrels
		for (int i = 0; i < NUM_OF_ARCTIC_SQUIRRELS; i++) {
			//The squirrels position must be converted back to screen coordinates
			enemies.add(new ArcticSquirrel(ScreenInfo.toScreenCoordinate(getSpawn().x), 
					ScreenInfo.toScreenCoordinate(getSpawn().y), getSpawn(),
					getGoal(), getPath()));
		}
		//Create the appropriate amount of wild squirrels
		for (int i = 0; i < NUM_OF_WILD_SQUIRRELS/2; i++) {
			//The squirrels position must be converted back to screen coordinates
			enemies.add(new WildSquirrel(ScreenInfo.toScreenCoordinate(getSpawn().x), 
					ScreenInfo.toScreenCoordinate(getSpawn().y), getSpawn(),
					getGoal(), getPath()));
		}
		//Create the appropriate amount of squirrels
		for (int i = 0; i < NUM_OF_SQUIRRELS; i++) {
			//The squirrels position must be converted back to screen coordinates
			enemies.add(new Squirrel(ScreenInfo.toScreenCoordinate(getSpawn().x), 
					ScreenInfo.toScreenCoordinate(getSpawn().y), getSpawn(),
					getGoal(), getPath()));
		}
		setEnemies(enemies);
	}
}
