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
    private static final float CARD_WIDTH = 216f;
    private static final float CARD_HEIGHT = 166f;
    private static final float CARD_Y = 146f;
    private static final float CARD_START_X = 62f;
    private static final float CARD_GAP = 14f;
    private static final Rectangle SUMMARY_PANEL = new Rectangle(206f, 396f, 388f, 62f);
    private static final Rectangle CHOOSE_PANEL = new Rectangle(256f, 334f, 288f, 38f);
    private static final Rectangle STATS_PANEL = new Rectangle(64f, 62f, 672f, 46f);

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

        ScreenUtils.clear(0.035f, 0.03f, 0.04f, 1f);

        shapeRenderer.setProjectionMatrix(game.getBatch().getProjectionMatrix());
        ArenaUi.drawArenaBackdrop(shapeRenderer);

        Vector2 mouse = game.screenToWorld(Gdx.input.getX(), Gdx.input.getY());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        ArenaUi.drawPanel(shapeRenderer, SUMMARY_PANEL, ArenaUi.INK, ArenaUi.GOLD);
        ArenaUi.drawPanel(shapeRenderer, CHOOSE_PANEL, ArenaUi.INK, ArenaUi.GOLD);
        ArenaUi.drawPanel(shapeRenderer, STATS_PANEL, ArenaUi.INK, ArenaUi.GOLD);

        for (int i = 0; i < cardBounds.size(); i++) {
            Rectangle card = cardBounds.get(i);
            UpgradeCard upgrade = selectedCards.get(i);
            ArenaUi.drawButton(shapeRenderer, card, upgrade.color, card.contains(mouse));
            drawUpgradeIcon(upgrade.icon, card.x + 44f, card.y + 82f);
        }
        shapeRenderer.end();

        game.getBatch().begin();
        ArenaUi.drawCentered(
            game.getFont(),
            game.getBatch(),
            "WAVE " + clearedWave + " CLEARED!",
            SUMMARY_PANEL.x + SUMMARY_PANEL.width / 2f,
            442f,
            1.35f,
            ArenaUi.PALE_GOLD
        );
        ArenaUi.drawCentered(
            game.getFont(),
            game.getBatch(),
            "KILLED: " + enemiesKilled + " ENEMIES     SCORE: " + score,
            SUMMARY_PANEL.x + SUMMARY_PANEL.width / 2f,
            414f,
            0.98f,
            ArenaUi.GOLD
        );
        ArenaUi.drawCentered(
            game.getFont(),
            game.getBatch(),
            "CHOOSE ONE UPGRADE",
            CHOOSE_PANEL.x + CHOOSE_PANEL.width / 2f,
            358f,
            1.05f,
            ArenaUi.PALE_GOLD
        );

        for (int i = 0; i < selectedCards.size(); i++) {
            Rectangle card = cardBounds.get(i);
            UpgradeCard upgrade = selectedCards.get(i);
            float centerX = card.x + card.width / 2f;
            ArenaUi.drawCentered(
                game.getFont(),
                game.getBatch(),
                (i + 1) + ". " + upgrade.name.toUpperCase(Locale.ROOT),
                centerX,
                card.y + card.height - 28f,
                1f,
                ArenaUi.PALE_GOLD
            );
            ArenaUi.drawText(
                game.getFont(),
                game.getBatch(),
                upgrade.description.toUpperCase(Locale.ROOT),
                card.x + 82f,
                card.y + 101f,
                0.82f,
                ArenaUi.BONE
            );
            ArenaUi.drawText(
                game.getFont(),
                game.getBatch(),
                upgrade.stackNote.toUpperCase(Locale.ROOT),
                card.x + 82f,
                card.y + 73f,
                0.76f,
                ArenaUi.BONE
            );
            ArenaUi.drawCentered(
                game.getFont(),
                game.getBatch(),
                "CLICK OR PRESS " + (i + 1),
                centerX,
                card.y + 30f,
                0.82f,
                ArenaUi.GOLD
            );
        }

        ArenaUi.drawCentered(
            game.getFont(),
            game.getBatch(),
            buildStatsLine(),
            STATS_PANEL.x + STATS_PANEL.width / 2f,
            92f,
            0.88f,
            ArenaUi.BONE
        );
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
        cards.add(new UpgradeCard("Fire Weapon", "+15 damage", "Stacks additively", UpgradeIcon.FIRE, ArenaUi.RED, FireWeaponDecorator::new));
        cards.add(new UpgradeCard("Poison Edge", "+10 damage", "No DoT, flat bonus", UpgradeIcon.BLADE, ArenaUi.GREEN, PoisonDecorator::new));
        cards.add(new UpgradeCard("Shield", "+30 max HP", "Also heals by +30", UpgradeIcon.SHIELD, ArenaUi.BLUE, ShieldDecorator::new));
        cards.add(new UpgradeCard("Armor", "-20% incoming damage", "Stacks multiplicatively", UpgradeIcon.ARMOR, ArenaUi.RED, ArmorDecorator::new));
        cards.add(new UpgradeCard("Speed Boots", "+25% move speed", "Stacks multiplicatively", UpgradeIcon.BOOT, ArenaUi.GREEN, SpeedBootsDecorator::new));
        cards.add(new UpgradeCard("Attack Speed", "-20% attack cooldown", "Stacks multiplicatively", UpgradeIcon.HOURGLASS, ArenaUi.BLUE, AttackSpeedDecorator::new));

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
            "HP %.0f/%.0f   |   DMG %.0f   |   SPD %.0f   |   CD %.2fs   |   ARMOR %.0f%%",
            player.getHp(),
            player.getMaxHp(),
            player.getDamage(),
            player.getSpeed(),
            player.getAttackCooldown(),
            damageReduction
        );
    }

    private void drawUpgradeIcon(UpgradeIcon icon, float centerX, float centerY) {
        if (icon == UpgradeIcon.SHIELD || icon == UpgradeIcon.ARMOR) {
            shapeRenderer.setColor(icon == UpgradeIcon.ARMOR ? ArenaUi.GOLD : new Color(0.54f, 0.32f, 0.14f, 1f));
            shapeRenderer.rect(centerX - 17f, centerY - 3f, 34f, 28f);
            shapeRenderer.triangle(centerX - 17f, centerY - 3f, centerX + 17f, centerY - 3f, centerX, centerY - 26f);
            shapeRenderer.setColor(0.82f, 0.66f, 0.38f, 1f);
            shapeRenderer.rect(centerX - 4f, centerY - 20f, 8f, 42f);
            shapeRenderer.rect(centerX - 14f, centerY + 2f, 28f, 6f);
            return;
        }

        if (icon == UpgradeIcon.BLADE) {
            shapeRenderer.setColor(0.86f, 0.84f, 0.76f, 1f);
            shapeRenderer.rectLine(centerX - 18f, centerY - 22f, centerX + 18f, centerY + 24f, 7f);
            shapeRenderer.setColor(ArenaUi.PALE_GOLD);
            shapeRenderer.triangle(centerX + 21f, centerY + 28f, centerX + 4f, centerY + 18f, centerX + 15f, centerY + 10f);
            shapeRenderer.setColor(0.45f, 0.18f, 0.11f, 1f);
            shapeRenderer.rectLine(centerX - 24f, centerY - 28f, centerX - 10f, centerY - 12f, 9f);
            return;
        }

        if (icon == UpgradeIcon.FIRE) {
            shapeRenderer.setColor(0.72f, 0.16f, 0.09f, 1f);
            shapeRenderer.triangle(centerX - 20f, centerY - 22f, centerX, centerY + 28f, centerX + 20f, centerY - 22f);
            shapeRenderer.setColor(1f, 0.70f, 0.20f, 1f);
            shapeRenderer.triangle(centerX - 10f, centerY - 18f, centerX + 5f, centerY + 17f, centerX + 13f, centerY - 18f);
            return;
        }

        if (icon == UpgradeIcon.BOOT) {
            shapeRenderer.setColor(0.45f, 0.26f, 0.10f, 1f);
            shapeRenderer.rect(centerX - 18f, centerY - 10f, 25f, 28f);
            shapeRenderer.rect(centerX - 18f, centerY - 24f, 44f, 15f);
            shapeRenderer.setColor(ArenaUi.GOLD);
            shapeRenderer.rect(centerX - 11f, centerY + 2f, 22f, 5f);
            return;
        }

        shapeRenderer.setColor(0.86f, 0.78f, 0.58f, 1f);
        shapeRenderer.rect(centerX - 14f, centerY + 20f, 28f, 5f);
        shapeRenderer.rect(centerX - 14f, centerY - 25f, 28f, 5f);
        shapeRenderer.rect(centerX - 5f, centerY - 20f, 10f, 40f);
        shapeRenderer.setColor(ArenaUi.BLUE);
        shapeRenderer.triangle(centerX - 14f, centerY + 15f, centerX + 14f, centerY + 15f, centerX, centerY);
        shapeRenderer.triangle(centerX - 14f, centerY - 15f, centerX + 14f, centerY - 15f, centerX, centerY);
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
        private final UpgradeIcon icon;
        private final Color color;
        private final UnaryOperator<PlayerStats> upgradeFactory;

        private UpgradeCard(
            String name,
            String description,
            String stackNote,
            UpgradeIcon icon,
            Color color,
            UnaryOperator<PlayerStats> upgradeFactory
        ) {
            this.name = name;
            this.description = description;
            this.stackNote = stackNote;
            this.icon = icon;
            this.color = color;
            this.upgradeFactory = upgradeFactory;
        }
    }

    private enum UpgradeIcon {
        ARMOR,
        BLADE,
        BOOT,
        FIRE,
        HOURGLASS,
        SHIELD
    }
}
