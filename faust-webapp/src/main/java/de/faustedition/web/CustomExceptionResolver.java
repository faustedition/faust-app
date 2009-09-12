package de.faustedition.web;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.AbstractView;

import de.faustedition.model.ObjectNotFoundException;

public class CustomExceptionResolver implements HandlerExceptionResolver {

	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		if (ex instanceof ObjectNotFoundException) {
			try {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, ex.getMessage());
				return new ModelAndView(new AbstractView() {
					
					@SuppressWarnings("unchecked")
					@Override
					protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
					}
				});
			} catch (IOException e) {
			}
		}
		return null;
	}

}
