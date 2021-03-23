/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2020, Arnaud Roques
 *
 * Project Info:  https://plantuml.com
 * 
 * If you like this project or if you find it useful, you can support us at:
 * 
 * https://plantuml.com/patreon (only 1$ per month!)
 * https://plantuml.com/paypal
 * 
 * This file is part of PlantUML.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Original Author:  Arnaud Roques
 */
package net.sourceforge.plantuml.png;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

import net.sourceforge.plantuml.Log;
import net.sourceforge.plantuml.security.ImageIO;

public class PngIOMetadata {

	private static final String copyleft = "Generated by http://plantuml.com";

	public static void writeWithMetadata(RenderedImage image, OutputStream os, String metadata, int dpi,
			String debugData) throws IOException {

//		Log.info("Cannot create com.sun.imageio.plugins.png.PNGMetadata");
//		PngIO.forceImageIO = true;
//		ImageIO.write(image, "png", os);

		writeInternal(image, os, metadata, dpi, debugData);
	}

	private static void writeInternal(RenderedImage image, OutputStream os, String metadata, int dpi, String debugData)
			throws IOException {

		final ImageWriter writer = javax.imageio.ImageIO.getImageWritersByFormatName("png").next();
		final ImageWriteParam writeParam = writer.getDefaultWriteParam();
		final ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier
				.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);

		final IIOMetadata meta = writer.getDefaultImageMetadata(typeSpecifier, writeParam);

		if (dpi != 96) {
			addDpi(meta, dpi);
		}

		if (debugData != null) {
			addText(meta, "debug", debugData);
		}
		addText(meta, "copyleft", copyleft);
		addiText(meta, "plantuml", metadata);

		Log.debug("PngIOMetadata pngMetadata=" + meta);

		// Render the PNG to file
		final IIOImage iioImage = new IIOImage(image, null, meta);
		Log.debug("PngIOMetadata iioImage=" + iioImage);
		// Attach the metadata
		final ImageWriter imagewriter = getImageWriter();
		Log.debug("PngIOMetadata imagewriter=" + imagewriter);

		// See
		// http://plantuml.sourceforge.net/qa/?qa=4367/sometimes-missing-response-headers-for-broken-png-images
		// Code provided by Michael Griffel
		synchronized (imagewriter) {
			final ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(os);
			imagewriter.setOutput(imageOutputStream);
			try {
				imagewriter.write(null /* default */, iioImage, null /* use default ImageWriteParam */);
			} finally {
				// os.flush();
				// Log.debug("PngIOMetadata finally 1");
				imageOutputStream.flush();
				// Log.debug("PngIOMetadata finally 2");
				imageOutputStream.close();
				// Log.debug("PngIOMetadata finally 3");
				imagewriter.reset();
				// Log.debug("PngIOMetadata finally 4");
				imagewriter.dispose();
				// Log.debug("PngIOMetadata finally 5");
			}
		}
	}

	private static void addDpi(IIOMetadata meta, double dpi) throws IIOInvalidTreeException {
		final IIOMetadataNode dimension = new IIOMetadataNode("Dimension");

		final IIOMetadataNode horizontalPixelSize = new IIOMetadataNode("HorizontalPixelSize");
		final double value = dpi / 0.0254 / 1000;
		horizontalPixelSize.setAttribute("value", Double.toString(value));
		dimension.appendChild(horizontalPixelSize);

		final IIOMetadataNode verticalPixelSize = new IIOMetadataNode("VerticalPixelSize");
		verticalPixelSize.setAttribute("value", Double.toString(value));
		dimension.appendChild(verticalPixelSize);

		final IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
		root.appendChild(dimension);
		
		meta.mergeTree("javax_imageio_1.0", root);

	}

	private static void addiText(IIOMetadata meta, String key, String value) throws IIOInvalidTreeException {
		final IIOMetadataNode text = new IIOMetadataNode("iTXt");
		final IIOMetadataNode entry = new IIOMetadataNode("iTXtEntry");
		entry.setAttribute("keyword", key);
		entry.setAttribute("compressionFlag", "true");
		entry.setAttribute("compressionMethod", "0");
		entry.setAttribute("languageTag", "");
		entry.setAttribute("translatedKeyword", "");
		entry.setAttribute("text", value);

		text.appendChild(entry);
		final IIOMetadataNode root = new IIOMetadataNode("javax_imageio_png_1.0");
		root.appendChild(text);
		
		meta.mergeTree("javax_imageio_png_1.0", root);

	}

	private static void addText(IIOMetadata meta, String key, String value) throws IIOInvalidTreeException {
		final IIOMetadataNode text = new IIOMetadataNode("tEXt");
		final IIOMetadataNode entry = new IIOMetadataNode("tEXtEntry");
		entry.setAttribute("keyword", key);
		entry.setAttribute("value", value);

		text.appendChild(entry);
		final IIOMetadataNode root = new IIOMetadataNode("javax_imageio_png_1.0");
		root.appendChild(text);
		
		meta.mergeTree("javax_imageio_png_1.0", root);
	}

	private static ImageWriter getImageWriter() {
		final Iterator<ImageWriter> iterator = ImageIO.getImageWritersBySuffix("png");
		for (final Iterator<ImageWriter> it = ImageIO.getImageWritersBySuffix("png"); it.hasNext();) {
			final ImageWriter imagewriter = iterator.next();
			Log.debug("PngIOMetadata countImageWriter = " + it.next());
			if (imagewriter.getClass().getName().equals("com.sun.imageio.plugins.png.PNGImageWriter")) {
				Log.debug("PngIOMetadata Found sun PNGImageWriter");
				return imagewriter;
			}

		}
		Log.debug("Using first one");
		return ImageIO.getImageWritersBySuffix("png").next();
	}

}
