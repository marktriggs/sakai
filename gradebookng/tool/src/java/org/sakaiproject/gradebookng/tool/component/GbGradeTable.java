package org.sakaiproject.gradebookng.tool.component;

import java.io.UnsupportedEncodingException;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.protocol.http.WebApplication;

import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.component.cover.ServerConfigurationService;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.sakaiproject.gradebookng.tool.model.GbGradebookData;


public class GbGradeTable extends Panel implements IHeaderContributor {

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	private Component component;

	List<GbStudentGradeInfo> grades;
	List<Assignment> assignments;

	/*
	    - Students: id, first name, last name, netid
	    - Course grades column: is released?, course grade
	    - course grade value for each student (letter, percentage, points)
	    - assignment header: id, points, due date, category {id, name, color}, included in course grade?, external?
	      - categories: enabled?  weighted categories?  normal categories?  handle uncategorized
	    - scores: number, has comments?, extra credit? (> total points), read only?
	 */

	public GbGradeTable(String id, List<GbStudentGradeInfo> grades, List<Assignment> assignments) {
		super(id);
		
		this.grades = grades;
		this.assignments = assignments;
		
		component = new WebMarkupContainer("gradeTable").setOutputMarkupId(true);

		component.add(new AjaxEventBehavior("gbgradetable.action") {
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getDynamicExtraParameters().add("return [{\"name\": \"ajaxParams\", \"value\": JSON.stringify(attrs.event.extraData)}]");
			}

			@Override
			protected void onEvent(AjaxRequestTarget target) {
				System.err.println("GOT PARAMS: " + getRequest().getRequestParameters().getParameterValue("ajaxParams"));
			}
		});

		add(component);
	}

	public void renderHead(IHeaderResponse response) {
		final String version = ServerConfigurationService.getString("portal.cdn.version", "");

		response.render(
			JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/scripts/gradebook-gbgrade-table.js?version=%s", version)));

		response.render(
			JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/scripts/handsontable.full.min.js?version=%s", version)));

		response.render(CssHeaderItem.forUrl(String.format("/gradebookng-tool/styles/handsontable.full.min.css?version=%s", version)));

		GbGradebookData gradebookData = new GbGradebookData(
				grades,
				assignments,
				this.businessService.getGradebookCategories(),
				this.businessService.getGradebookSettings(),
				this);

		response.render(OnDomReadyHeaderItem.forScript(String.format("var tableData = %s", gradebookData.toScript())));

		response.render(OnDomReadyHeaderItem.forScript(String.format("GbGradeTable.renderTable('%s', tableData)",
									     component.getMarkupId())));
	}
}
