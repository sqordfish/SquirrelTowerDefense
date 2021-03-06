/**
 * An enemy is a sprite that follows a path towards a goal point.
 * It has health, movement speed, and a path to follow.
 * (The path is followed in reverse order)
 * 
 * @author Jacob Rust
 */

package com.squirrel.game.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.squirrel.game.PathFinder;
import com.squirrel.game.ScreenInfo;

public abstract class Enemy extends Sprite {
	
	//These values should be changed in the base classes
	private float speed;
	private float health;
	private int reward;
	
	//These should not be changed for balance
	private Vector2 velocity;
	private Array<Vector2> path;
	private Vector2 next;
	private boolean reachedGoal;
	private boolean dead;
	private Vector2 goal;

	//For animation
	private TextureAtlas textureAtlas;
	private Animation animation;
	private float elapsedTime = 0;
	
	/**
	 * Constructs a new enemy with a sprite, x and y coordinates, and
	 * a path to follow. The path will be traveled in the reverse of the 
	 * order that it is passed in.  
	 * PRECONDITIONS: The x and y coordinates should be within the map the 
	 * enemy will be drawn to. The path should contain vectors that are
	 * in the map as well. If not, an exception will be thrown
	 * @param sprite Sprite representing the enemy
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param path Path the enemy will follow in reverse order
	 * @param fileName Name of file the sprite sheet is stored in
	 */
	public Enemy(Sprite sprite, int x, int y, float health, float speed, int reward,
			Vector2 spawn, Vector2 goal, Array<Vector2> path, String fileName) {
		super(sprite);
		
		textureAtlas = new TextureAtlas(Gdx.files.internal(fileName));
		animation = new Animation(1/30f, textureAtlas.getRegions());

		//Set the x and y position using the Sprite methods
		setX(x);
		setY(y);

		this.health = health;
		this.speed = speed;
		this.setReward(reward);
		this.goal = goal;

		velocity = new Vector2();
		this.path = new Array<Vector2>();

		//Get the path ready to be traveled
		setPath(path);
		next = path.peek();
		reachedGoal = false;
	}
	
	@Override
	public void draw(Batch batch) {
		//Update the enemy based off the time between 
		//this frame and the last frame
		update(Gdx.graphics.getDeltaTime());
		super.draw(batch);
		
	}

	/**
	 * Updates the health, path, velocity, and position
	 * of the enemy
	 * @param delta Time step
	 */
	public void update(float delta) {
		//Check if reachedTarget
		if (health <= 0) {
			dead = true;
			return;
		}
		
		//Change next target position
		if (atNext()) {
			if (path.size == 0) {
				reachedGoal = true;
				return;
			} else {
				next = path.pop();
			}
		}
		
		//Set x velocity 
		if (Math.abs(getX() - next.x) < 1) {
			velocity.x = 0;
		} else if (getX() < next.x) {
			velocity.x = speed;
		} else if (getX() > next.x) {
			velocity.x = -speed;
		}
		
		//Set y velocity
		if (Math.abs(getY() - next.y) < 1) {
			velocity.y = 0;
		} else if (getY() < next.y) {
			velocity.y = speed;
		} else if (getY() > next.y) {
			velocity.y = -speed;
		} 

		//Update position
		setX(getX() + velocity.x * delta);
		setY(getY() + velocity.y * delta);

		//Update animation
		elapsedTime += Gdx.graphics.getDeltaTime();
		setRegion(animation.getKeyFrame(elapsedTime, true));
	}
	
	/**
	 * Updates the squirrels path
	 * @param mapLayer Layer of a map the squirrel is traveling on
	 */
	public void updatePath(TiledMapTileLayer mapLayer, PathFinder pathFinder) {
		Vector2 currentPosition = new Vector2 (
				ScreenInfo.toMapCoordinate(getX()),
				ScreenInfo.toMapCoordinate(getY()));
		setPath(pathFinder.findShortestPath(currentPosition, goal));
		
		//If the next cell would be blocked, update the next position
		Cell cell = mapLayer.getCell(ScreenInfo.toMapCoordinate(next.x),
				ScreenInfo.toMapCoordinate(next.y));
		if (cell != null && (cell.getTile()
				.getProperties().containsKey("blocked")) && path.size > 0) {
			next = path.peek();
		}
	}
	
	/**
	 * Updates the squirrels path
	 */
	public void updatePath(Array<Vector2> path) {
		this.path = path;
	}
	
	/**
	 * Determines if the enemy has reached its goal
	 * @return True if it has reached, else false
	 */
	public boolean hasReachedGoal() {
		return reachedGoal;
	}
	
	/**
	 * Determines if the enemy is dead
	 * @return True if dead, else false
	 */
	public boolean isDead() {
		return dead;
	}
	
	public void takeDamage(float damage) {
		health -= damage;
	}
	
	/**
	 * Determines if the enemy has reached the next
	 * spot in its path
	 * @return True if at the next spot, else false
	 */
	public boolean atNext() {
		return ((Math.abs(getX() - next.x) < 2
				&& Math.abs(getY() - next.y) < 2) ? true : false); 
	}
	
	/**
	 * Sets the path of the enemy. The path will be
	 * traveled in reverse order. 
	 * @param newPath Path to be traveled
	 */
	public void setPath(Array<Vector2> newPath) {
		if (newPath != null) {
			path = newPath;
		} else {
			//Gdx.app.log("Enemy: ", "Path is null...");
		}
	}

	/**
	 * @return the reward
	 */
	public int getReward() {
		return reward;
	}

	/**
	 * @param reward the reward to set
	 */
	public void setReward(int reward) {
		this.reward = reward;
	}
	
	public String toString() {
		String out = "";
		out += "\nPath- "+path.toString();
		
		return out;
	}
	
	public void dispose() {
		textureAtlas.dispose();
		getTexture().dispose();
	}
	
}

