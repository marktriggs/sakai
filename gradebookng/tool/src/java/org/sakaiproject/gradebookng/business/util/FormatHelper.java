package org.sakaiproject.gradebookng.business.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import lombok.extern.apachecommons.CommonsLog;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;
import org.sakaiproject.tool.gradebook.Gradebook;

@CommonsLog
public class FormatHelper {

	/**
	 * The value is a double (ie 12.34542) that needs to be formatted as a percentage with two decimal places precision. And drop off any .0
	 * if no decimal places.
	 *
	 * @param score as a double
	 * @return double to decimal places
	 */
	public static String formatDoubleToTwoDecimalPlaces(final Double score) {
		final NumberFormat df = NumberFormat.getInstance();
		df.setMinimumFractionDigits(0);
		df.setMaximumFractionDigits(2);
		df.setRoundingMode(RoundingMode.HALF_DOWN);

		return formatGrade(df.format(score));
	}

	/**
	 * The value is a double (ie 12.34) that needs to be formatted as a percentage with two decimal places precision.
	 *
	 * @param score as a double
	 * @return percentage to decimal places with a '%' for good measure
	 */
	public static String formatDoubleAsPercentage(final Double score) {
		// TODO does the % need to be internationalised?
		return formatDoubleToTwoDecimalPlaces(score) + "%";
	}

	/**
	 * Format the given string as a percentage with two decimal precision. String should be something that can be converted to a number.
	 * 
	 * @param string string representation of the number
	 * @return percentage to decimal places with a '%' for good measure
	 */
	public static String formatStringAsPercentage(final String string) {
		if (StringUtils.isBlank(string)) {
			return null;
		}

		final BigDecimal decimal = new BigDecimal(string).setScale(2, RoundingMode.HALF_DOWN);

		return formatDoubleAsPercentage(decimal.doubleValue());
	}

	/**
	 * Format a grade, e.g. 00 => 0 0001 => 1 1.0 => 1 1.25 => 1.25
	 *
	 * @param grade
	 * @return
	 */
	public static String formatGrade(final String grade) {
		if (StringUtils.isBlank(grade)) {
			return "";
		}

		String s = null;
		try {
			final Double d = Double.parseDouble(grade);

			final DecimalFormat df = new DecimalFormat();
			df.setMinimumFractionDigits(0);
			df.setGroupingUsed(false);

			s = df.format(d);
		} catch (final NumberFormatException e) {
			log.debug("Bad format, returning original string: " + grade);
			s = grade;
		}

		return StringUtils.removeEnd(s, ".0");
	}

	/**
	 * Format a date e.g. MM/dd/yyyy
	 *
	 * @param date
	 * @return
	 */
	private static String formatDate(final Date date) {
		final SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy"); // TODO needs to come from i18n
		return df.format(date);
	}

	/**
	 * Format a date but return ifNull if null
	 *
	 * @param date
	 * @param ifNull
	 * @return
	 */
	public static String formatDate(final Date date, final String ifNull) {
		if (date == null) {
			return ifNull;
		}

		return formatDate(date);
	}

	/**
	 * Format a date with a time e.g. MM/dd/yyyy HH:mm
	 *
	 * @param date
	 * @return
	 */
	public static String formatDateTime(final Date date) {
		final SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm"); // TODO needs to come from i18n
		return df.format(date);
	}

	/**
	 * Format a Course Grade based on the current settings
	 *  e.g. A+ (95%) [133/140]
	 *
	 * @param courseGrade
	 * @param showLetter
	 * @param showOverride
	 * @param showPercentage
	 * @param showPoints
	 * 
	 * @return String
	 */
	public static String formatCourseGrade(final CourseGrade courseGrade, final boolean showLetter, final boolean showOverride, final boolean showPercentage, final boolean showPoints) {
		final List<String> parts = new ArrayList<>();

		// letter grade
		if (showLetter) {
			String letterGrade = null;
			if (showOverride && StringUtils.isNotBlank(courseGrade.getEnteredGrade())) {
				letterGrade = courseGrade.getEnteredGrade();
			} else {
				letterGrade = courseGrade.getMappedGrade();
			}

			if (StringUtils.isNotBlank(letterGrade)) {
				parts.add(letterGrade);
			}
		}

		// percentage
		if (showPercentage) {
			final String calculatedGrade = FormatHelper.formatStringAsPercentage(courseGrade.getCalculatedGrade());
			if (StringUtils.isNotBlank(calculatedGrade)) {
				if (parts.isEmpty()) {
					parts.add(new StringResourceModel("coursegrade.display.percentage-first", null,
						new Object[] { calculatedGrade }).getString());
				} else {
					parts.add(new StringResourceModel("coursegrade.display.percentage-second", null,
						new Object[] { calculatedGrade }).getString());
				}
			}
		}

		// requested points
		if (showPoints) {
			final Double pointsEarned = courseGrade.getPointsEarned();
			final Double totalPointsPossible = courseGrade.getTotalPointsPossible();

			if (totalPointsPossible > 0) {
				if (parts.isEmpty()) {
					parts.add(new StringResourceModel("coursegrade.display.points-first", null,
						new Object[]{pointsEarned, totalPointsPossible}).getString());
				} else {
					parts.add(new StringResourceModel("coursegrade.display.points-second", null,
						new Object[]{pointsEarned, totalPointsPossible}).getString());
				}
			}
		}

		// if parts is empty, there are no grades, display a -
		if (parts.isEmpty()) {
			parts.add(new StringResourceModel("coursegrade.display.none", null).getString());
		}

		return String.join(" ", parts);
	}
}
