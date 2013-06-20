package de.faustedition.facsimile;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class MockFacsimileStore implements FacsimileStore {

    @Override
    public FacsimileMetadata metadata(String path) throws IOException, IllegalArgumentException {
        return new FacsimileMetadata(9 * TILE_SIZE, 12 * TILE_SIZE, 3, TILE_SIZE);
    }

    @Override
    public BufferedImage tile(String path, int zoom, int x, int y) throws IOException, IllegalArgumentException {
        final BufferedImage tile = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = (Graphics2D) tile.getGraphics();
        g.setColor(Color.LIGHT_GRAY);
        g.setPaint(Color.LIGHT_GRAY);
        g.setBackground(Color.BLACK);
        g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{2.0f}, 0.0f));
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(g.getFont().deriveFont(TILE_SIZE / 10.0f));

        final FontMetrics fm = g.getFontMetrics();
        final String title = String.format("%d: [%d,%d]", zoom, x, y);

        g.draw(new Rectangle(TILE_SIZE - 1, TILE_SIZE - 1));
        g.drawString(title, (TILE_SIZE - fm.stringWidth(title)) / 2, (TILE_SIZE - fm.getHeight()) / 2 + fm.getAscent());

        return tile;
    }
}
