package com.gladiator.arena.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.gladiator.arena.GladiatorGame;
import com.gladiator.arena.decorator.ArmorDecorator;
import com.gladiator.arena.decorator.AttackSpeedDecorator;
import com.gladiator.arena.decorator.FireWeaponDecorator;
import com.gladiator.arena.decorator.PlayerStats;
import com.gladiator.arena.decorator.PoisonDecorator;
import com.gladiator.arena.decorator.ShieldDecorator;
import com.gladiator.arena.decorator.SpeedBootsDecorator;
import com.gladiator.arena.entities.Player;
import com.gladiator.arena.managers.GameManager;
import com.gladiator.arena.managers.GameStateManager;
import com.gladiator.arena.managers.LevelManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;

public class UpgradeScreen extends ScreenAdapter {
    private static final float CARD_WIDTH = 200f;
    private static final float CARD_HEIGHT = 150f;
    private static final float CARD_Y = 170f;
    private static final float CARD_START_X = 70f;
    private static final float CARD_GAP = 30f;

    private final GladiatorGame game;
    private final Player player;
    private final int clearedWave;
    private final int enemiesKilled;
    private final int score;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final List<UpgradeCard> selectedCards = new ArrayList<>();
    private final List<Rectangle> cardBounds = new ArrayList<>();
    private boolean disposed;
    private boolean transitioning;

    public UpgradeScreen(GladiatorGame game) {
        this(game, new Player(), 1, 0, 0);
    }

    public UpgradeScreen(GladiatorGame game, Player player, LevelManager.WaveSummary summary, int score) {
        this(
            game,
            player,
            summary == null ? 1 : summary.getWaveNumber(),
            summary == null ? 0 : summary.getEnemiesKilled(),
            score
        );
    }

    private UpgradeScreen(GladiatorGame game, Player player, int clearedWave, int enemiesKilled, int score) {
        this.game = game;
        this.player = player;
        this.clearedWave = clearedWave;
        this.enemiesKilled = enemiesKilled;
        this.score = score;
        selectRandomCards();
        createCardBounds();
    }

    @Override
    public void show() {
        GameManager.getInstance().getGameStateManager().set(GameStateManager.State.UPGRADE);
    }

    @Override
    public void render(float delta) {
        handleInput();
        if (transitioning) {
            return;
        }

        ScreenUtils.clear(0.07f, 0.2f, 0.1f, 1f);

        shapeRenderer.setProjectionMatrix(game.getBatch().getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < cardBounds.size(); i++) {
            Rectangle card = cardBounds.get(i);
            shapeRenderer.setColor(i == 0 ? Color.SCARLET : i == 1 ? Color.SKY : Color.GOLD);
            shapeRenderer.rect(card.x, card.y, card.width, card.height);
        }
        shapeRenderer.end();

        game.getBatch().begin();
        game.getFont().draw(game.getBatch(), "Wave " + clearedWave + " cleared!", 315f, 430f);
        game.getFont().draw(game.getBatch(), "Killed: " + enemiesKilled + " enemies    Score: " + score, 280f, 400f);
        game.getFont().draw(game.getBatch(), "Choose one upgrade", 315f, 365f);

        for (int i = 0; i < selectedCards.size(); i++) {
            Rectangle card = cardBounds.get(i);
            UpgradeCard upgrade = selectedCards.get(i);
            float textX = card.x + 14f;
            float textY = card.y + card.height - 22f;
            game.getFont().draw(game.getBatch(), (i + 1) + ". " + upgrade.name, textX, textY);
            game.getFont().draw(game.getBatch(), upgrade.description, textX, textY - 34f);
            game.getFont().draw(game.getBatch(), upgrade.stackNote, textX, textY - 64f);
            game.getFont().draw(game.getBatch(), "Click or press " + (i + 1), textX, textY - 108f);
        }

        game.getFont().draw(game.getBatch(), buildStatsLine(), 130f, 95f);
        game.getBatch().end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1)) {
            applyCard(0);
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_2)) {
            applyCard(1);
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_3)) {
            applyCard(2);
            return;
        }

        if (!Gdx.input.justTouched()) {
            return;
        }

        Vector2 touch = game.screenToWorld(Gdx.input.getX(), Gdx.input.getY());
        for (int i = 0; i < cardBounds.size(); i++) {
            if (cardBounds.get(i).contains(touch.x, touch.y)) {
                applyCard(i);
                return;
            }
        }
    }

    private void applyCard(int index) {
        if (transitioning || index < 0 || index >= selectedCards.size()) {
            return;
        }

        transitioning = true;
        player.applyUpgrade(selectedCards.get(index).upgradeFactory);
        game.setScreen(new GameScreen(game, player, clearedWave + 1, score));
        dispose();
    }

    private void selectRandomCards() {
        List<UpgradeCard> cards = new ArrayList<>();
        cards.add(new UpgradeCard("Fire Weapon", "+15 damage", "Stacks additively", FireWeaponDecorator::new));
        cards.add(new UpgradeCard("Poison Edge", "+10 damage", "No DoT, flat bonus", PoisonDecorator::new));
        cards.add(new UpgradeCard("Shield", "+30 max HP", "Also heals by +30", ShieldDecorator::new));
        cards.add(new UpgradeCard("Armor", "-20% incoming damage", "Stacks multiplicatively", ArmorDecorator::new));
        cards.add(new UpgradeCard("Speed Boots", "+25% move speed", "Stacks multiplicatively", SpeedBootsDecorator::new));
        cards.add(new UpgradeCard("Attack Speed", "-20% attack cooldown", "Stacks multiplicatively", AttackSpeedDecorator::new));

        Collections.shuffle(cards);
        selectedCards.addAll(cards.subList(0, 3));
    }

    private void createCardBounds() {
        for (int i = 0; i < 3; i++) {
            float x = CARD_START_X + (i * (CARD_WIDTH + CARD_GAP));
            cardBounds.add(new Rectangle(x, CARD_Y, CARD_WIDTH, CARD_HEIGHT));
        }
    }

    private String buildStatsLine() {
        float damageReduction = (1f - player.getIncomingDamageMultiplier()) * 100f;
        return String.format(
            Locale.ROOT,
            "Current stats: HP %.0f/%.0f | DMG %.0f | SPD %.0f | CD %.2fs | Armor %.0f%%",
            player.getHp(),
            player.getMaxHp(),
            player.getDamage(),
            player.getSpeed(),
            player.getAttackCooldown(),
            damageReduction
        );
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }

        disposed = true;
        shapeRenderer.dispose();
    }

    private static final class UpgradeCard {
        private final String name;
        private final String description;
        private final String stackNote;
        private final UnaryOperator<PlayerStats> upgradeFactory;

        private UpgradeCard(
            String name,
            String description,
            String stackNote,
            UnaryOperator<PlayerStats> upgradeFactory
        ) {
            this.name = name;
            this.description = description;
            this.stackNote = stackNote;
            this.upgradeFactory = upgradeFactory;
        }
    }
}
