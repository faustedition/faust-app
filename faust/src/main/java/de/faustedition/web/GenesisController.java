package de.faustedition.web;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.imagemap.ImageMapUtilities;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.AbstractView;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Controller
public class GenesisController {
	@RequestMapping("/genesis/")
	public String overview(HttpServletRequest request, ModelMap model) throws IOException {
		StringWriter imageMap = new StringWriter();
		new GenesisExampleChart().render(new ByteArrayOutputStream(), new PrintWriter(imageMap), request.getContextPath() + "/Witness/", "genesisChart");

		model.addAttribute("imageMap", imageMap.toString());
		model.addAttribute("paralipomena", PARALIPOMENA_REFS);
		model.addAttribute("urfaust", URFAUST_REF);

		return "genesis";
	}

	@RequestMapping("/genesis/chart.png")
	public ModelAndView renderChart() throws IOException {
		return new ModelAndView(new AbstractView() {

			@SuppressWarnings("unchecked")
			@Override
			protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
				response.setContentType("image/png");
				ServletOutputStream outputStream = response.getOutputStream();
				new GenesisExampleChart().render(outputStream, new PrintWriter(new StringWriter()), "genesisChart", "");
				outputStream.flush();
			}
		});
	}

	private static class LineInterval {
		private int start;
		private int end;
		private String portfolio;
		private String manuscript;

		private LineInterval(String portfolio, String manuscript, int start, int end) {
			this.portfolio = portfolio;
			this.manuscript = manuscript;
			this.start = start;
			this.end = end;
		}
	}

	public static class ParalipomenonReference implements Serializable {
		private String name;
		private String portfolio;
		private String manuscript;

		ParalipomenonReference(String name, String portfolio, String manuscript) {
			this.name = name;
			this.portfolio = portfolio;
			this.manuscript = manuscript;
		}

		public String getName() {
			return name;
		}

		public String getPortfolio() {
			return portfolio;
		}

		public String getManuscript() {
			return manuscript;
		}
	}

	private static final List<String> WITNESSES = Lists.newArrayList("V.H15", "V.H13v", "V.H14", "V.H18", "V.H17r", "V.H2", "V.H16", "V.H");

	private static final Map<String, LineInterval[]> GENESIS_DATASET = Maps.newLinkedHashMap();

	static {
		GENESIS_DATASET.put("V.H15", new LineInterval[] { new LineInterval("391506", "0002", 11519, 11526) });
		GENESIS_DATASET.put("V.H13v", new LineInterval[] { new LineInterval("391027", "0004", 11511, 11530) });
		GENESIS_DATASET.put("V.H14", new LineInterval[] { new LineInterval("391505", "0002", 11511, 11530) });
		GENESIS_DATASET.put("V.H18", new LineInterval[] { new LineInterval("390757", "0002", 11595, 11603) });
		GENESIS_DATASET.put("V.H17r", new LineInterval[] { new LineInterval("391510", "0002", 11593, 11595) });
		GENESIS_DATASET.put("V.H2", new LineInterval[] { new LineInterval("390883", "0013", 11511, 11530),//
				new LineInterval("390883", "0014", 11539, 11590),//
				new LineInterval("390883", "0015", 11593, 11603) });
		GENESIS_DATASET.put("V.H16", new LineInterval[] { new LineInterval("391507", "0002", 11573, 11576) });
		GENESIS_DATASET.put("V.H", new LineInterval[] { new LineInterval("391098", "0360", 11511, 11522),//
				new LineInterval("391098", "0361", 11523, 11543),//
				new LineInterval("391098", "0362", 11544, 11562),//
				new LineInterval("391098", "0363", 11563, 11586),//
				new LineInterval("391098", "0364", 11587, 11593),//
				new LineInterval("391098", "0365", 11594, 11619) });
	}

	private static final List<ParalipomenonReference> PARALIPOMENA_REFS = Lists.newArrayList(new ParalipomenonReference("P195", "391082", "0002"),//
			new ParalipomenonReference("P21", "390782", "0002"),//
			new ParalipomenonReference("P1", "390720", "0002"),//
			new ParalipomenonReference("P93/P95", "390882", "0002"),//
			new ParalipomenonReference("P91", "391314", "0002"),//
			new ParalipomenonReference("P92a", "390781", "0002"),//
			new ParalipomenonReference("P92b", "390826", "0002"),//
			new ParalipomenonReference("P96", "390050", "0002"),//
			new ParalipomenonReference("P97", "390777", "0002"),//
			new ParalipomenonReference("P98a", "390705", "0002"),//
			new ParalipomenonReference("P98b", "390705", "0003"));

	private static final ParalipomenonReference URFAUST_REF = new ParalipomenonReference("Urfaust-Schluss", "390028", "0095");

	public static class GenesisExampleChart extends AbstractXYDataset implements IntervalXYDataset {
		private boolean transposed;

		public void render(OutputStream imageStream, PrintWriter imageMapWriter, final String manuscriptBaseUrl, String mapId) throws IOException {
			JFreeChart chart = ChartFactory.createXYBarChart(null, "Handschrift", false, "Vers", this, PlotOrientation.HORIZONTAL, false, false, false);
			chart.setBackgroundPaint(Color.white);

			XYPlot plot = (XYPlot) chart.getPlot();
			SymbolAxis xAxis = new SymbolAxis("Handschrift", WITNESSES.toArray(new String[WITNESSES.size()]));
			xAxis.setGridBandsVisible(false);
			plot.setDomainAxis(xAxis);

			XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
			renderer.setUseYInterval(true);
			renderer.setURLGenerator(new XYURLGenerator() {

				public String generateURL(XYDataset dataset, int series, int item) {
					LineInterval interval = GENESIS_DATASET.get(WITNESSES.get(series))[item];
					return manuscriptBaseUrl + String.format("GSA/%s/%s.xml", interval.portfolio, interval.manuscript);
				}
			});

			NumberAxis yAxis = new NumberAxis("Vers");
			yAxis.setAutoRange(true);
			yAxis.setAutoRangeIncludesZero(false);
			DecimalFormat df = new DecimalFormat();
			df.setGroupingUsed(false);
			df.setMaximumFractionDigits(0);
			df.setMinimumFractionDigits(0);
			yAxis.setNumberFormatOverride(df);
			plot.setRangeAxis(yAxis);

			ChartUtilities.applyCurrentTheme(chart);
			ChartRenderingInfo chartRenderingInfo = new ChartRenderingInfo();
			ChartUtilities.writeChartAsPNG(imageStream, chart, 800, 300, chartRenderingInfo);
			ImageMapUtilities.writeImageMap(imageMapWriter, mapId, chartRenderingInfo);
		}

		public boolean isTransposed() {
			return this.transposed;
		}

		public void setTransposed(boolean transposed) {
			this.transposed = transposed;
			fireDatasetChanged();
		}

		public int getSeriesCount() {
			return WITNESSES.size();
		}

		@SuppressWarnings("unchecked")
		public Comparable getSeriesKey(int series) {
			return WITNESSES.get(series);
		}

		public int getItemCount(int series) {
			return GENESIS_DATASET.get(WITNESSES.get(series)).length;
		}

		public double getXValue(int series, int item) {
			if (!this.transposed) {
				return getSeriesValue(series);
			} else {
				return getItemValue(series, item);
			}
		}

		public double getStartXValue(int series, int item) {
			if (!this.transposed) {
				return getSeriesStartValue(series);
			} else {
				return getItemStartValue(series, item);
			}
		}

		public double getEndXValue(int series, int item) {
			if (!this.transposed) {
				return getSeriesEndValue(series);
			} else {
				return getItemEndValue(series, item);
			}
		}

		public Number getX(int series, int item) {
			return new Double(getXValue(series, item));
		}

		public Number getStartX(int series, int item) {
			return new Double(getStartXValue(series, item));
		}

		public Number getEndX(int series, int item) {
			return new Double(getEndXValue(series, item));
		}

		public double getYValue(int series, int item) {
			if (!this.transposed) {
				return getItemValue(series, item);
			} else {
				return getSeriesValue(series);
			}
		}

		public double getStartYValue(int series, int item) {
			if (!this.transposed) {
				return getItemStartValue(series, item);
			} else {
				return getSeriesStartValue(series);
			}
		}

		public double getEndYValue(int series, int item) {
			if (!this.transposed) {
				return getItemEndValue(series, item);
			} else {
				return getSeriesEndValue(series);
			}
		}

		public Number getY(int series, int item) {
			return new Double(getYValue(series, item));
		}

		public Number getStartY(int series, int item) {
			return new Double(getStartYValue(series, item));
		}

		public Number getEndY(int series, int item) {
			return new Double(getEndYValue(series, item));
		}

		private double getSeriesValue(int series) {
			return series;
		}

		private double getSeriesStartValue(int series) {
			return series - 0.4;
		}

		private double getSeriesEndValue(int series) {
			return series + 0.4;
		}

		private double getItemValue(int series, int item) {
			LineInterval interval = GENESIS_DATASET.get(WITNESSES.get(series))[item];
			return (interval.start + interval.end) / 2.0;
		}

		private double getItemStartValue(int series, int item) {
			return GENESIS_DATASET.get(WITNESSES.get(series))[item].start;
		}

		private double getItemEndValue(int series, int item) {
			return GENESIS_DATASET.get(WITNESSES.get(series))[item].end;
		}

		public void datasetChanged(DatasetChangeEvent event) {
			fireDatasetChanged();
		}

		/**
		 * Tests this dataset for equality with an arbitrary object.
		 * 
		 * @param obj
		 *                the object (<code>null</code> permitted).
		 * 
		 * @return A boolean.
		 */
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof GenesisExampleChart)) {
				return false;
			}
			GenesisExampleChart that = (GenesisExampleChart) obj;
			if (this.transposed != that.transposed) {
				return false;
			}
			return true;
		}

		/**
		 * Returns a clone of this dataset.
		 * 
		 * @return A clone of this dataset.
		 * 
		 * @throws CloneNotSupportedException
		 *                 if there is a problem cloning.
		 */
		public Object clone() throws CloneNotSupportedException {
			return (GenesisExampleChart) super.clone();
		}
	}
}
