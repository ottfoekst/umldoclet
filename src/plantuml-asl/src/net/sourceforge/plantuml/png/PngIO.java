/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2023, Arnaud Roques
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

import net.sourceforge.plantuml.Log;
import net.sourceforge.plantuml.quantization.Quantizer;
import net.sourceforge.plantuml.security.SFile;
import net.sourceforge.plantuml.security.SImageIO;
import net.sourceforge.plantuml.ugraphic.color.ColorMapper;

public class PngIO {

	private static final String copyleft = "Generated by http://plantuml.com";
	public static boolean USE_QUANTIZATION = false;

	public static void write(RenderedImage image, ColorMapper mapper, SFile file, String metadata, int dpi)
			throws IOException {
		try (OutputStream os = file.createBufferedOutputStream()) {
			write(image, mapper, os, metadata, dpi);
		}
		Log.debug("File is " + file);
		Log.debug("File size " + file.length());
		if (file.length() == 0) {
			Log.error("File size is zero: " + file);
			SImageIO.write(image, "png", file);
		}
	}

	public static void write(RenderedImage image, ColorMapper mapper, OutputStream os, String metadata, int dpi)
			throws IOException {
		write(image, mapper, os, metadata, dpi, null);
	}

	private static void write(RenderedImage image, ColorMapper mapper, OutputStream os, String metadata, int dpi,
			String debugData) throws IOException {

		if (USE_QUANTIZATION)
			image = Quantizer.quantizeNow(mapper, (BufferedImage) image);

		if (metadata == null)
			SImageIO.write(image, "png", os);
		else
			PngIOMetadata.writeWithMetadata(image, os, metadata, dpi, debugData);

	}

//	/** writes a BufferedImage of type TYPE_INT_ARGB to PNG using PNGJ */
//	public static void writeARGB(BufferedImage bi, OutputStream os, String metadata) {
//		// if (bi.getType() != BufferedImage.TYPE_INT_ARGB)
//		// throw new PngjException("This method expects  BufferedImage.TYPE_INT_ARGB");
//		ImageInfo imi = new ImageInfo(bi.getWidth(), bi.getHeight(), 8, false);
//		PngChunkTEXT chunkText = new PngChunkTEXT(imi, "copyleft", copyleft);
//		// PngChunkTEXT chunkTextDebug = new PngChunkTEXT(imi, "debug", "debugData");
//		PngChunkITXT meta = new PngChunkITXT(imi);
//		meta.setKeyVal("plantuml", metadata);
//		meta.setCompressed(true);
//
//		PngWriter pngw = new PngWriter(os, imi);
//		pngw.setCompLevel(9);// maximum compression, not critical usually
//		// pngw.setFilterType(FilterType.FILTER_ADAPTIVE_FAST); // see what you prefer here
//		// pngw.setFilterType(FilterType.FILTER_ADAPTIVE_MEDIUM); // see what you prefer here
//		pngw.setFilterType(FilterType.FILTER_ADAPTIVE_FULL); // see what you prefer here
//		pngw.queueChunk(chunkText);
//		// // pngw.queueChunk(chunkTextDebug);
//		pngw.queueChunk(meta);
//		DataBufferInt db = ((DataBufferInt) bi.getRaster().getDataBuffer());
//		SinglePixelPackedSampleModel samplemodel = (SinglePixelPackedSampleModel) bi.getSampleModel();
//		if (db.getNumBanks() != 1)
//			throw new PngjException("This method expects one bank");
//		ImageLineInt line = new ImageLineInt(imi);
//		for (int row = 0; row < imi.rows; row++) {
//			int elem = samplemodel.getOffset(0, row);
//			for (int col = 0, j = 0; col < imi.cols; col++) {
//				int sample = db.getElem(elem++);
//				line.scanline[j++] = (sample & 0xFF0000) >> 16; // R
//				line.scanline[j++] = (sample & 0xFF00) >> 8; // G
//				line.scanline[j++] = (sample & 0xFF); // B
//				// line.scanline[j++] = (((sample & 0xFF000000) >> 24) & 0xFF); // A
//			}
//			pngw.writeRow(line, row);
//		}
//		pngw.end();
//	}

}
