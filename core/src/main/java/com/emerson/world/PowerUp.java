package com.emerson.world;

import com.emerson.gameobjects.Ball;

public interface PowerUp {
    void activate(Ball ball, GameWorld gameWorld);
    void deactivate(Ball ball, GameWorld gameWorld);
    void update(float deltaTime, Ball ball, GameWorld gameWorld);
}
