package com.emerson.world;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class SludgeDrip {
    private final Vector2 start;
    private final Vector2 end;
    private float duration; // How long the arc should last
    private final float MAX_DURATION = 1f; // 1 second

    public SludgeDrip(Vector2 start, Vector2 end) {
        this.start = new Vector2(start);
        this.end = new Vector2(end);
        this.duration = MAX_DURATION;
    }

    public void update(float deltaTime) {
        duration -= deltaTime;
    }

    public boolean isExpired() {
        return duration <= 0;
    }

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(0f, 0.8f, 1f, 1f);
        shapeRenderer.line(start.x, start.y, end.x, end.y);
    }
}
