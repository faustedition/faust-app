/*
 * Copyright (c) 2014 Faust Edition development team.
 *
 * This file is part of the Faust Edition.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.faustedition.facsimile;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
public class FacsimileTileResource extends ServerResource {

    private final Ehcache cache;
    private final FacsimileTile tile;

    public FacsimileTileResource(Ehcache cache, FacsimileTile tile) {
        super();
        this.cache = cache;
        this.tile = tile;
    }

    @Get("jpg")
    public Representation image() throws IOException {
        final Element cacheElement = cache.get(tile);
        if (cacheElement != null) {
            if (cacheElement.getCreationTime() >= tile.getFile().lastModified()) {
                return new FacsimileTileRepresentation((byte[]) cacheElement.getObjectValue());
            }
            cache.remove(tile);
        }

        final ByteArrayOutputStream tileDataStream = new ByteArrayOutputStream();
        ImageReader reader = null;
        try {
            reader = tile.createImageReader();
            final int imageIndex = Math.max(0, Math.min(tile.getZoom(), reader.getNumImages(true) - 1));
            final int imageWidth = reader.getWidth(imageIndex);
            final int imageHeight = reader.getHeight(imageIndex);
            final int x = tile.getX() * FacsimileTile.SIZE;
            final int y = tile.getY() * FacsimileTile.SIZE;
            final ImageReadParam parameters = reader.getDefaultReadParam();
            final Rectangle tile = new Rectangle(
                    Math.min(x, imageWidth),
                    Math.min(y, imageHeight),
                    Math.min(FacsimileTile.SIZE, Math.max(0, imageWidth - x)),
                    Math.min(FacsimileTile.SIZE, Math.max(0, imageHeight - y))
            );
            parameters.setSourceRegion(tile);
            final BufferedImage tileImage = reader.read(imageIndex, parameters);

            ImageIO.write(tileImage, "JPEG", tileDataStream);
        } finally {
            if (reader != null) {
                reader.dispose();
            }
        }

        final byte[] tileData = tileDataStream.toByteArray();
        cache.put(new Element(tile, tileData));
        return new FacsimileTileRepresentation(tileData);
    }

    private class FacsimileTileRepresentation extends OutputRepresentation {

        private final byte[] data;

        public FacsimileTileRepresentation(byte[] data) {
            super(MediaType.IMAGE_JPEG);
            this.data = data;
            setModificationDate(new Date(tile.getFile().lastModified()));
        }

        @Override
        public void write(OutputStream outputStream) throws IOException {
            outputStream.write(data);
        }
    }
}
