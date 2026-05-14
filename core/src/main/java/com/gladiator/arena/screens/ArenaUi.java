package com.gladiator.arena.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

final class ArenaUi {
    static final float WORLD_WIDTH = 800f;
    static final float WORLD_HEIGHT = 480f;

    static final Color GOLD = new Color(0.95f, 0.64f, 0.24f, 1f);
    static final Color PALE_GOLD = new Color(1f, 0.82f, 0.48f, 1f);
    static final Color STONE = new Color(0.22f, 0.18f, 0.16f, 1f);
    static final Color DARK_STONE = new Color(0.09f, 0.08f, 0.09f, 1f);
    static final Color INK = new Color(0.04f, 0.035f, 0.035f, 1f);
    static final Color BONE = new Color(0.92f, 0.84f, 0.68f, 1f);
    static final Color RED = new Color(0.48f, 0.12f, 0.10f, 1f);
    static final Color BLUE = new Color(0.14f, 0.29f, 0.42f, 1f);
    static final Color GREEN = new Color(0.20f, 0.34f, 0.16f, 1f);

    private static final GlyphLayout GLYPH_LAYOUT = new GlyphLayout();

    private ArenaUi() {
    }

    static void drawArenaBackdrop(ShapeRenderer shapes) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.055f, 0.045f, 0.055f, 1f);
        shapes.rect(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);

        drawUniformMenuWall(shapes);
        drawBanners(shapes);
        drawTorches(shapes);
        drawVignette(shapes);
        shapes.end();
    }

    static void drawMenuBackdrop(ShapeRenderer shapes) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.055f, 0.045f, 0.055f, 1f);
        shapes.rect(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);

        drawUniformMenuWall(shapes);
        drawBanners(shapes);
        drawTorches(shapes);
        drawVignette(shapes);
        shapes.end();
    }

    static void drawPanel(ShapeRenderer shapes, Rectangle bounds, Color fill, Color border) {
        drawPanel(shapes, bounds.x, bounds.y, bounds.width, bounds.height, fill, border);
    }

    static void drawPanel(ShapeRenderer shapes, float x, float y, float width, float height, Color fill, Color border) {
        shapes.setColor(0.025f, 0.02f, 0.018f, 1f);
        shapes.rect(x + 5f, y - 5f, width, height);

        shapes.setColor(border);
        shapes.rect(x, y, width, height);
        shapes.setColor(0.035f, 0.03f, 0.032f, 1f);
        shapes.rect(x + 4f, y + 4f, width - 8f, height - 8f);
        shapes.setColor(fill);
        shapes.rect(x + 7f, y + 7f, width - 14f, height - 14f);

        shapes.setColor(lighten(fill, 1.28f));
        shapes.rect(x + 7f, y + height - 13f, width - 14f, 6f);
        shapes.setColor(darken(fill, 0.62f));
        shapes.rect(x + 7f, y + 7f, width - 14f, 6f);

        shapes.setColor(GOLD);
        float bolt = 5f;
        shapes.rect(x + 6f, y + 6f, bolt, bolt);
        shapes.rect(x + width - 11f, y + 6f, bolt, bolt);
        shapes.rect(x + 6f, y + height - 11f, bolt, bolt);
        shapes.rect(x + width - 11f, y + height - 11f, bolt, bolt);
    }

    static void drawButton(ShapeRenderer shapes, Rectangle bounds, Color fill, boolean hovered) {
        Color buttonFill = hovered ? lighten(fill, 1.22f) : fill;
        Color border = hovered ? PALE_GOLD : GOLD;
        drawPanel(shapes, bounds, buttonFill, border);
    }

    static void drawThinPanel(ShapeRenderer shapes, float x, float y, float width, float height, Color fill, Color border) {
        shapes.setColor(0.015f, 0.012f, 0.012f, 1f);
        shapes.rect(x + 3f, y - 3f, width, height);
        shapes.setColor(border);
        shapes.rect(x, y, width, height);
        shapes.setColor(0.055f, 0.045f, 0.042f, 1f);
        shapes.rect(x + 2f, y + 2f, width - 4f, height - 4f);
        shapes.setColor(fill);
        shapes.rect(x + 4f, y + 4f, width - 8f, height - 8f);
        shapes.setColor(lighten(fill, 1.22f));
        shapes.rect(x + 4f, y + height - 6f, width - 8f, 2f);
        shapes.setColor(darken(fill, 0.70f));
        shapes.rect(x + 4f, y + 4f, width - 8f, 2f);
    }

    static void drawProgressBar(
        ShapeRenderer shapes,
        float x,
        float y,
        float width,
        float height,
        float progress,
        Color fill
    ) {
        float clamped = Math.max(0f, Math.min(1f, progress));
        shapes.setColor(0.02f, 0.018f, 0.018f, 1f);
        shapes.rect(x - 3f, y - 3f, width + 6f, height + 6f);
        shapes.setColor(0.18f, 0.10f, 0.08f, 1f);
        shapes.rect(x, y, width, height);
        shapes.setColor(fill);
        shapes.rect(x, y, width * clamped, height);
        shapes.setColor(lighten(fill, 1.32f));
        shapes.rect(x, y + height - 4f, width * clamped, 4f);
    }

    static void drawCentered(
        BitmapFont font,
        SpriteBatch batch,
        String text,
        float centerX,
        float y,
        float scale,
        Color color
    ) {
        float oldScaleX = font.getData().scaleX;
        float oldScaleY = font.getData().scaleY;
        Color oldColor = font.getColor().cpy();

        font.getData().setScale(scale);
        font.setColor(color);
        GLYPH_LAYOUT.setText(font, text);
        font.draw(batch, text, centerX - GLYPH_LAYOUT.width / 2f, y);

        font.getData().setScale(oldScaleX, oldScaleY);
        font.setColor(oldColor);
    }

    static void drawText(
        BitmapFont font,
        SpriteBatch batch,
        String text,
        float x,
        float y,
        float scale,
        Color color
    ) {
        float oldScaleX = font.getData().scaleX;
        float oldScaleY = font.getData().scaleY;
        Color oldColor = font.getColor().cpy();

        font.getData().setScale(scale);
        font.setColor(color);
        font.draw(batch, text, x, y);

        font.getData().setScale(oldScaleX, oldScaleY);
        font.setColor(oldColor);
    }

    static void drawTitle(SpriteBatch batch, BitmapFont font, float titleY) {
        drawCentered(font, batch, "GLADIATOR", WORLD_WIDTH / 2f, titleY, 2.35f, PALE_GOLD);
        drawCentered(font, batch, "ARENA", WORLD_WIDTH / 2f, titleY - 38f, 1.55f, new Color(0.74f, 0.20f, 0.15f, 1f));
    }

    static void drawCrossedSwords(ShapeRenderer shapes, float centerX, float centerY) {
        shapes.setColor(0.72f, 0.68f, 0.58f, 1f);
        shapes.rectLine(centerX - 150f, centerY - 34f, centerX - 28f, centerY + 38f, 7f);
        shapes.rectLine(centerX + 150f, centerY - 34f, centerX + 28f, centerY + 38f, 7f);
        shapes.setColor(0.94f, 0.90f, 0.74f, 1f);
        shapes.triangle(centerX - 22f, centerY + 42f, centerX - 42f, centerY + 23f, centerX - 13f, centerY + 25f);
        shapes.triangle(centerX + 22f, centerY + 42f, centerX + 42f, centerY + 23f, centerX + 13f, centerY + 25f);
        shapes.setColor(0.48f, 0.22f, 0.12f, 1f);
        shapes.rectLine(centerX - 170f, centerY - 46f, centerX - 140f, centerY - 28f, 10f);
        shapes.rectLine(centerX + 170f, centerY - 46f, centerX + 140f, centerY - 28f, 10f);
        shapes.setColor(GOLD);
        shapes.rect(centerX - 148f, centerY - 38f, 22f, 5f);
        shapes.rect(centerX + 126f, centerY - 38f, 22f, 5f);
    }

    static void drawTitleSwords(ShapeRenderer shapes, float centerX, float centerY) {
        shapes.setColor(0.72f, 0.68f, 0.58f, 1f);
        shapes.rectLine(centerX - 360f, centerY - 26f, centerX - 150f, centerY + 34f, 8f);
        shapes.rectLine(centerX + 360f, centerY - 26f, centerX + 150f, centerY + 34f, 8f);

        shapes.setColor(0.94f, 0.90f, 0.74f, 1f);
        shapes.triangle(centerX - 136f, centerY + 40f, centerX - 164f, centerY + 21f, centerX - 146f, centerY + 14f);
        shapes.triangle(centerX + 136f, centerY + 40f, centerX + 164f, centerY + 21f, centerX + 146f, centerY + 14f);

        shapes.setColor(0.48f, 0.22f, 0.12f, 1f);
        shapes.rectLine(centerX - 392f, centerY - 34f, centerX - 338f, centerY - 19f, 11f);
        shapes.rectLine(centerX + 392f, centerY - 34f, centerX + 338f, centerY - 19f, 11f);

        shapes.setColor(GOLD);
        shapes.rect(centerX - 350f, centerY - 31f, 30f, 6f);
        shapes.rect(centerX + 320f, centerY - 31f, 30f, 6f);
    }

    private static void drawStoneWall(ShapeRenderer shapes) {
        shapes.setColor(0.13f, 0.10f, 0.10f, 1f);
        shapes.rect(0f, 170f, WORLD_WIDTH, 310f);
        for (int row = 0; row < 7; row++) {
            float y = 172f + row * 38f;
            float offset = row % 2 == 0 ? 0f : -24f;
            for (float x = offset; x < WORLD_WIDTH; x += 48f) {
                float shade = ((int) (x + row * 17f) % 3) * 0.018f;
                shapes.setColor(0.18f + shade, 0.13f + shade, 0.11f + shade, 1f);
                shapes.rect(x + 1f, y + 1f, 46f, 36f);
                shapes.setColor(0.075f, 0.055f, 0.052f, 1f);
                shapes.rect(x, y, 48f, 2f);
                shapes.rect(x, y, 2f, 38f);
            }
        }

        shapes.setColor(0.08f, 0.06f, 0.065f, 1f);
        shapes.rect(0f, 386f, WORLD_WIDTH, 94f);
        for (int i = 0; i < 110; i++) {
            float x = (i * 37f) % WORLD_WIDTH;
            float y = 392f + ((i * 19f) % 74f);
            float warm = (i % 4) * 0.03f;
            shapes.setColor(0.16f + warm, 0.10f + warm * 0.6f, 0.075f, 1f);
            shapes.rect(x, y, 9f + i % 6, 6f + i % 5);
        }
    }

    private static void drawUniformMenuWall(ShapeRenderer shapes) {
        shapes.setColor(0.105f, 0.075f, 0.065f, 1f);
        shapes.rect(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);

        float brickWidth = 72f;
        float brickHeight = 38f;
        for (int row = 0; row < 13; row++) {
            float y = row * brickHeight;
            float offset = row % 2 == 0 ? 0f : -brickWidth / 2f;
            for (float x = offset; x < WORLD_WIDTH; x += brickWidth) {
                float shade = row % 2 == 0 ? 0.006f : 0f;
                shapes.setColor(0.205f + shade, 0.155f + shade, 0.135f + shade, 1f);
                shapes.rect(x + 2f, y + 2f, brickWidth - 4f, brickHeight - 4f);
                shapes.setColor(0.055f, 0.043f, 0.04f, 1f);
                shapes.rect(x, y, brickWidth, 2f);
                shapes.rect(x, y, 2f, brickHeight);
            }
        }

        shapes.setColor(0.07f, 0.055f, 0.06f, 1f);
        shapes.rect(0f, 398f, WORLD_WIDTH, 82f);
        for (int i = 0; i < 70; i++) {
            float x = (i * 53f) % WORLD_WIDTH;
            float y = 406f + ((i * 23f) % 62f);
            shapes.setColor(0.18f, 0.12f, 0.075f, 1f);
            shapes.rect(x, y, 10f + i % 5, 6f + i % 4);
        }
    }

    private static void drawGate(ShapeRenderer shapes) {
        shapes.setColor(0.10f, 0.075f, 0.06f, 1f);
        shapes.rect(270f, 118f, 260f, 218f);
        shapes.setColor(0.05f, 0.04f, 0.04f, 1f);
        shapes.rect(308f, 112f, 184f, 206f);
        shapes.setColor(0.16f, 0.105f, 0.075f, 1f);
        shapes.rect(286f, 112f, 24f, 218f);
        shapes.rect(490f, 112f, 24f, 218f);
        shapes.rect(286f, 314f, 228f, 26f);

        shapes.setColor(0.18f, 0.13f, 0.10f, 1f);
        shapes.rect(78f, 120f, 42f, 218f);
        shapes.rect(680f, 120f, 42f, 218f);
        shapes.setColor(0.09f, 0.07f, 0.07f, 1f);
        shapes.rect(88f, 125f, 22f, 206f);
        shapes.rect(690f, 125f, 22f, 206f);
    }

    private static void drawBanners(ShapeRenderer shapes) {
        drawBanner(shapes, 48f, 260f);
        drawBanner(shapes, 704f, 260f);
        drawBanner(shapes, 142f, 114f);
        drawBanner(shapes, 624f, 114f);
    }

    private static void drawBanner(ShapeRenderer shapes, float x, float y) {
        shapes.setColor(0.11f, 0.045f, 0.04f, 1f);
        shapes.rect(x - 5f, y - 5f, 48f, 116f);
        shapes.setColor(RED);
        shapes.rect(x, y, 38f, 106f);
        shapes.triangle(x, y, x + 19f, y - 22f, x + 38f, y);
        shapes.setColor(0.73f, 0.50f, 0.20f, 1f);
        shapes.rectLine(x + 10f, y + 62f, x + 28f, y + 34f, 5f);
        shapes.rectLine(x + 28f, y + 62f, x + 10f, y + 34f, 5f);
    }

    private static void drawTorches(ShapeRenderer shapes) {
        drawTorch(shapes, 216f, 136f);
        drawTorch(shapes, 584f, 136f);
        drawTorch(shapes, 700f, 284f);
    }

    private static void drawTorch(ShapeRenderer shapes, float x, float y) {
        shapes.setColor(0.11f, 0.07f, 0.045f, 1f);
        shapes.rect(x - 8f, y - 45f, 16f, 50f);
        shapes.rect(x - 18f, y - 48f, 36f, 8f);
        shapes.setColor(0.90f, 0.43f, 0.11f, 1f);
        shapes.triangle(x - 14f, y - 1f, x, y + 38f, x + 14f, y - 1f);
        shapes.setColor(1f, 0.78f, 0.24f, 1f);
        shapes.triangle(x - 7f, y + 2f, x + 2f, y + 27f, x + 8f, y + 2f);
    }

    private static void drawFloor(ShapeRenderer shapes) {
        shapes.setColor(0.18f, 0.12f, 0.09f, 1f);
        shapes.rect(0f, 0f, WORLD_WIDTH, 168f);
        for (int row = 0; row < 5; row++) {
            float y = row * 34f;
            float offset = row % 2 == 0 ? 0f : -36f;
            for (float x = offset; x < WORLD_WIDTH; x += 72f) {
                shapes.setColor(0.23f, 0.15f, 0.10f, 1f);
                shapes.rect(x + 1f, y + 1f, 70f, 32f);
                shapes.setColor(0.105f, 0.075f, 0.065f, 1f);
                shapes.rect(x, y, 72f, 2f);
                shapes.rect(x, y, 2f, 34f);
            }
        }
    }

    private static void drawVignette(ShapeRenderer shapes) {
        shapes.setColor(0.035f, 0.03f, 0.04f, 1f);
        shapes.rect(0f, 0f, WORLD_WIDTH, 18f);
        shapes.rect(0f, WORLD_HEIGHT - 18f, WORLD_WIDTH, 18f);
        shapes.rect(0f, 0f, 18f, WORLD_HEIGHT);
        shapes.rect(WORLD_WIDTH - 18f, 0f, 18f, WORLD_HEIGHT);
    }

    private static Color lighten(Color color, float factor) {
        return new Color(
            Math.min(color.r * factor, 1f),
            Math.min(color.g * factor, 1f),
            Math.min(color.b * factor, 1f),
            color.a
        );
    }

    private static Color darken(Color color, float factor) {
        return new Color(color.r * factor, color.g * factor, color.b * factor, color.a);
    }
}
