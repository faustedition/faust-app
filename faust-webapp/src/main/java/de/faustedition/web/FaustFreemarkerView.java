package de.faustedition.web;

import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import de.faustedition.model.security.HasRoleTemplateMethod;

public class FaustFreemarkerView extends FreeMarkerView
{
	public FaustFreemarkerView()
	{
		addStaticAttribute("hasRole", new HasRoleTemplateMethod());
		addStaticAttribute("encodePath", new URLPathEncoder());
	}
}
