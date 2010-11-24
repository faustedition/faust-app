package de.faustedition.genesis;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;

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
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;

import com.google.inject.Inject;
import com.google.inject.name.Named;

@SuppressWarnings("serial")
public class GenesisSampleChart extends AbstractXYDataset implements IntervalXYDataset {
	private boolean transposed;
	private final String contextPath;

	@Inject
	public GenesisSampleChart(@Named("ctx.path") String contextPath) {
		this.contextPath = contextPath;
	}

	public void render(OutputStream imageStream, PrintWriter imageMapWriter, final String mapId) throws IOException {
		JFreeChart chart = ChartFactory.createXYBarChart(null, "Handschrift", false, "Vers", this,
				PlotOrientation.HORIZONTAL, false, false, false);
		chart.setBackgroundPaint(Color.white);

		XYPlot plot = (XYPlot) chart.getPlot();
		SymbolAxis xAxis = new SymbolAxis("Handschrift",
				GenesisSampleResource.WITNESSES.toArray(new String[GenesisSampleResource.WITNESSES.size()]));
		xAxis.setGridBandsVisible(false);
		plot.setDomainAxis(xAxis);

		XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
		renderer.setUseYInterval(true);
		renderer.setURLGenerator(new XYURLGenerator() {

			public String generateURL(XYDataset dataset, int series, int item) {
				LineInterval interval = GenesisSampleResource.GENESIS_DATASET.get(GenesisSampleResource.WITNESSES
						.get(series))[item];
				return String.format("%s/document/%s#%s", contextPath, interval.portfolio, interval.manuscript);
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

	public int getSeriesCount() {
		return GenesisSampleResource.WITNESSES.size();
	}

	public Comparable<?> getSeriesKey(int series) {
		return GenesisSampleResource.WITNESSES.get(series);
	}

	public int getItemCount(int series) {
		return GenesisSampleResource.GENESIS_DATASET.get(GenesisSampleResource.WITNESSES.get(series)).length;
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
		LineInterval interval = GenesisSampleResource.GENESIS_DATASET.get(GenesisSampleResource.WITNESSES.get(series))[item];
		return (interval.start + interval.end) / 2.0;
	}

	private double getItemStartValue(int series, int item) {
		return GenesisSampleResource.GENESIS_DATASET.get(GenesisSampleResource.WITNESSES.get(series))[item].start;
	}

	private double getItemEndValue(int series, int item) {
		return GenesisSampleResource.GENESIS_DATASET.get(GenesisSampleResource.WITNESSES.get(series))[item].end;
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
		if (!(obj instanceof GenesisSampleChart)) {
			return false;
		}
		GenesisSampleChart that = (GenesisSampleChart) obj;
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
		return (GenesisSampleChart) super.clone();
	}
}