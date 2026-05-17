package com.gladiator.arena.managers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.gladiator.arena.events.EnemyDamagedEvent;
import com.gladiator.arena.events.EventBus;
import com.gladiator.arena.events.EventListener;
import com.gladiator.arena.events.GameEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DamageNumberManager {
    private static final float LIFETIME = 0.72f;
    private static final float RISE_SPEED = 42f;
    private static final float SPREAD_X = 10f;
    private static final float BASE_SCALE = 0.82f;
    private static final Color DAMAGE_COLOR = new Color(1f, 0.78f, 0.28f, 1f);
    private static final Color SHADOW_COLOR = new Color(0f, 0f, 0f, 0.65f);

    private final EventBus eventBus;
    private final EventListener enemyDamagedListener;
    private final List<FloatingText> texts = new ArrayList<>();
    private final GlyphLayout layout = new GlyphLayout();
    private boolean disposed;

    public DamageNumberManager(EventBus eventBus) {
        this.eventBus = eventBus;
        enemyDamagedListener = this::handleEnemyDamaged;
        eventBus.subscribe(GameEvent.Type.ENEMY_DAMAGED, enemyDamagedListener);
    }

    public void update(float delta) {
        Iterator<FloatingText> iterator = texts.iterator();
        while (iterator.hasNext()) {
            FloatingText text = iterator.next();
            text.update(delta);
            if (text.isDone()) {
                iterator.remove();
            }
        }
    }

    public void render(SpriteBatch batch, BitmapFont font) {
        float oldScaleX = font.getData().scaleX;
        float oldScaleY = font.getData().scaleY;
        Color oldColor = font.getColor().cpy();

        for (FloatingText text : texts) {
            float alpha = text.getAlpha();
            float scale = BASE_SCALE + 0.18f * (1f - text.getLifePercent());
            font.getData().setScale(scale);
            layout.setText(font, text.value);
            float drawX = text.x - layout.width / 2f;

            SHADOW_COLOR.a = 0.65f * alpha;
            font.setColor(SHADOW_COLOR);
            font.draw(batch, text.value, drawX + 1f, text.y - 1f);

            DAMAGE_COLOR.a = alpha;
            font.setColor(DAMAGE_COLOR);
            font.draw(batch, text.value, drawX, text.y);
        }

        font.getData().setScale(oldScaleX, oldScaleY);
        font.setColor(oldColor);
        DAMAGE_COLOR.a = 1f;
        SHADOW_COLOR.a = 0.65f;
    }

    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        eventBus.unsubscribe(GameEvent.Type.ENEMY_DAMAGED, enemyDamagedListener);
    }

    private void handleEnemyDamaged(GameEvent event) {
        if (!(event.getPayload() instanceof EnemyDamagedEvent)) {
            return;
        }

        EnemyDamagedEvent damaged = (EnemyDamagedEvent) event.getPayload();
        texts.add(new FloatingText(
            Integer.toString(MathUtils.round(damaged.getAmount())),
            damaged.getX() + MathUtils.random(-SPREAD_X, SPREAD_X),
            damaged.getY() + 12f
        ));
    }

    private static final class FloatingText {
        private final String value;
        private final float x;
        private float y;
        private float timer;

        private FloatingText(String value, float x, float y) {
            this.value = value;
            this.x = x;
            this.y = y;
            timer = LIFETIME;
        }

        private void update(float delta) {
            timer -= delta;
            y += RISE_SPEED * delta;
        }

        private boolean isDone() {
            return timer <= 0f;
        }

        private float getAlpha() {
            return MathUtils.clamp(timer / LIFETIME, 0f, 1f);
        }

        private float getLifePercent() {
            return MathUtils.clamp(timer / LIFETIME, 0f, 1f);
        }
    }
}
