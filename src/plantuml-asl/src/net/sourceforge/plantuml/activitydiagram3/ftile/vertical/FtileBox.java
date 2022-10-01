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
package net.sourceforge.plantuml.activitydiagram3.ftile.vertical;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import net.sourceforge.plantuml.ISkinParam;
import net.sourceforge.plantuml.LineBreakStrategy;
import net.sourceforge.plantuml.SkinParamColors;
import net.sourceforge.plantuml.activitydiagram3.LinkRendering;
import net.sourceforge.plantuml.activitydiagram3.ftile.AbstractFtile;
import net.sourceforge.plantuml.activitydiagram3.ftile.BoxStyle;
import net.sourceforge.plantuml.activitydiagram3.ftile.Ftile;
import net.sourceforge.plantuml.activitydiagram3.ftile.FtileGeometry;
import net.sourceforge.plantuml.activitydiagram3.ftile.Swimlane;
import net.sourceforge.plantuml.awt.geom.XDimension2D;
import net.sourceforge.plantuml.creole.CreoleMode;
import net.sourceforge.plantuml.creole.Parser;
import net.sourceforge.plantuml.creole.Sheet;
import net.sourceforge.plantuml.creole.SheetBlock1;
import net.sourceforge.plantuml.creole.SheetBlock2;
import net.sourceforge.plantuml.creole.Stencil;
import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.cucadiagram.Stereotype;
import net.sourceforge.plantuml.graphic.FontConfiguration;
import net.sourceforge.plantuml.graphic.HorizontalAlignment;
import net.sourceforge.plantuml.graphic.Rainbow;
import net.sourceforge.plantuml.graphic.StringBounder;
import net.sourceforge.plantuml.graphic.TextBlock;
import net.sourceforge.plantuml.graphic.UDrawable;
import net.sourceforge.plantuml.graphic.color.Colors;
import net.sourceforge.plantuml.style.ClockwiseTopRightBottomLeft;
import net.sourceforge.plantuml.style.PName;
import net.sourceforge.plantuml.style.SName;
import net.sourceforge.plantuml.style.Style;
import net.sourceforge.plantuml.style.StyleSignatureBasic;
import net.sourceforge.plantuml.ugraphic.UGraphic;
import net.sourceforge.plantuml.ugraphic.UStroke;
import net.sourceforge.plantuml.ugraphic.UTranslate;
import net.sourceforge.plantuml.ugraphic.color.HColor;
import net.sourceforge.plantuml.ugraphic.color.HColors;

public class FtileBox extends AbstractFtile {

	private final ClockwiseTopRightBottomLeft padding;
	private final ClockwiseTopRightBottomLeft margin;

	private final TextBlock tb;
	private double roundCorner = 25;
	private final double shadowing;
	private final HorizontalAlignment horizontalAlignment;
	private double minimumWidth = 0;

	private final LinkRendering inRendering;
	private final Swimlane swimlane;
	private final BoxStyle boxStyle;

	private final HColor borderColor;
	private final HColor backColor;
	private final Style style;

	static public StyleSignatureBasic getStyleSignature() {
		return StyleSignatureBasic.of(SName.root, SName.element, SName.activityDiagram, SName.activity);
	}

	static public StyleSignatureBasic getStyleSignatureArrow() {
		return StyleSignatureBasic.of(SName.root, SName.element, SName.activityDiagram, SName.arrow);
	}

	final public LinkRendering getInLinkRendering() {
		return inRendering;
	}

	public Set<Swimlane> getSwimlanes() {
		if (swimlane == null) {
			return Collections.emptySet();
		}
		return Collections.singleton(swimlane);
	}

	public Swimlane getSwimlaneIn() {
		return swimlane;
	}

	public Swimlane getSwimlaneOut() {
		return swimlane;
	}

	class MyStencil implements Stencil {

		public double getStartingX(StringBounder stringBounder, double y) {
			return -padding.getLeft();
		}

		public double getEndingX(StringBounder stringBounder, double y) {
			final XDimension2D dim = calculateDimension(stringBounder);
			return dim.getWidth() - padding.getRight();
		}

	}

	public static FtileBox create(ISkinParam skinParam, Display label, Swimlane swimlane, BoxStyle boxStyle,
			Stereotype stereotype) {
		final Style style = getStyleSignature().withTOBECHANGED(stereotype)
				.getMergedStyle(skinParam.getCurrentStyleBuilder());
		final Style styleArrow = getStyleSignatureArrow().getMergedStyle(skinParam.getCurrentStyleBuilder());
		return new FtileBox(skinParam, label, swimlane, boxStyle, style, styleArrow);
	}

	private FtileBox(ISkinParam skinParam, Display label, Swimlane swimlane, BoxStyle boxStyle, Style style,
			Style styleArrow) {
		super(skinParam);
		this.style = style;
		this.boxStyle = boxStyle;
		this.swimlane = swimlane;

		this.inRendering = LinkRendering
				.create(Rainbow.build(styleArrow, getIHtmlColorSet()));
		Colors specBack = null;
		if (skinParam instanceof SkinParamColors)
			specBack = ((SkinParamColors) skinParam).getColors();

		style = style.eventuallyOverride(specBack);
		this.borderColor = style.value(PName.LineColor).asColor(getIHtmlColorSet());
		this.backColor = style.value(PName.BackGroundColor).asColor(getIHtmlColorSet());

		final FontConfiguration fc = style.getFontConfiguration(getIHtmlColorSet());

		this.horizontalAlignment = style.getHorizontalAlignment();
		this.padding = style.getPadding();
		this.margin = style.getMargin();
		this.roundCorner = style.value(PName.RoundCorner).asDouble();
		this.shadowing = style.value(PName.Shadowing).asDouble();

		final LineBreakStrategy wrapWidth = style.wrapWidth();
		this.minimumWidth = style.value(PName.MinimumWidth).asDouble();

		final Sheet sheet = Parser
				.build(fc, skinParam.getDefaultTextAlignment(horizontalAlignment), skinParam, CreoleMode.FULL)
				.createSheet(label);
		this.tb = new SheetBlock2(new SheetBlock1(sheet, wrapWidth, skinParam.getPadding()), new MyStencil(),
				new UStroke(1));
		this.print = label.toString();

	}

	final private String print;

	@Override
	public String toString() {
		return print;
	}

	public void drawU(UGraphic ug) {
		final XDimension2D dimTotal = calculateDimension(ug.getStringBounder());
		final double widthTotal = dimTotal.getWidth();
		final double heightTotal = dimTotal.getHeight();
		final UDrawable shape = boxStyle.getUDrawable(widthTotal, heightTotal, shadowing, roundCorner);

		final UStroke thickness = style.getStroke();

		if (borderColor == null)
			ug = ug.apply(HColors.none());
		else
			ug = ug.apply(borderColor);

		if (backColor == null)
			ug = ug.apply(HColors.none().bg());
		else
			ug = ug.apply(backColor.bg());

		ug = ug.apply(thickness);
		shape.drawU(ug);

		if (horizontalAlignment == HorizontalAlignment.LEFT) {
			tb.drawU(ug.apply(new UTranslate(padding.getLeft(), padding.getTop())));
		} else if (horizontalAlignment == HorizontalAlignment.RIGHT) {
			final XDimension2D dimTb = tb.calculateDimension(ug.getStringBounder());
			tb.drawU(ug.apply(
					new UTranslate(dimTotal.getWidth() - dimTb.getWidth() - padding.getRight(), padding.getBottom())));
		} else if (horizontalAlignment == HorizontalAlignment.CENTER) {
			final XDimension2D dimTb = tb.calculateDimension(ug.getStringBounder());
			tb.drawU(ug.apply(new UTranslate((dimTotal.getWidth() - dimTb.getWidth()) / 2, padding.getBottom())));
		}
	}

	@Override
	protected FtileGeometry calculateDimensionFtile(StringBounder stringBounder) {
		XDimension2D dimRaw = tb.calculateDimension(stringBounder);
		dimRaw = XDimension2D.delta(dimRaw, padding.getLeft() + padding.getRight(),
				padding.getBottom() + padding.getTop());
		dimRaw = XDimension2D.atLeast(dimRaw, minimumWidth, 0);
		return new FtileGeometry(dimRaw.getWidth() + boxStyle.getShield(), dimRaw.getHeight(), dimRaw.getWidth() / 2, 0,
				dimRaw.getHeight());
	}

	public Collection<Ftile> getMyChildren() {
		return Collections.emptyList();
	}

}
