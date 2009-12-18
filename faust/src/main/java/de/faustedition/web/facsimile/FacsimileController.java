package de.faustedition.web.facsimile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Deque;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

import de.faustedition.model.ObjectNotFoundException;
import de.faustedition.model.manuscript.Facsimile;
import de.faustedition.model.manuscript.FacsimileImageDao;
import de.faustedition.model.manuscript.FacsimileImageResolution;
import de.faustedition.web.ControllerUtil;

@Controller
public class FacsimileController
{
	public static final String URL_PREFIX = "facsimile";
	@Autowired
	private FacsimileImageDao facsimileStore;

	@Autowired
	private SessionFactory dbSessionFactory;

	@RequestMapping("/facsimile/**")
	public void stream(WebRequest webRequest, HttpServletRequest request, HttpServletResponse response) throws ObjectNotFoundException, IOException
	{
		Deque<String> pathComponents = ControllerUtil.getPathComponents(request);
		if (!pathComponents.isEmpty() && "facsimile".equals(pathComponents.getFirst()))
		{
			pathComponents.removeFirst();
		}
		if (pathComponents.isEmpty())
		{
			throw new ObjectNotFoundException();
		}

		String filename = pathComponents.removeLast();
		FacsimileImageResolution facsimileResolution = null;
		for (FacsimileImageResolution resolution : FacsimileImageResolution.values())
		{
			if (resolution.matches(filename))
			{
				facsimileResolution = resolution;
				filename = StringUtils.removeEnd(filename, resolution.getSuffix());
			}
		}
		if (facsimileResolution == null || facsimileResolution == FacsimileImageResolution.HIGH)
		{
			throw new ObjectNotFoundException();
		}
		pathComponents.addLast(filename);

		Facsimile facsimile = ControllerUtil.foundObject(Facsimile.findByImagePath(dbSessionFactory.getCurrentSession(), StringUtils.join(pathComponents, "/")));
		File facsimileImageFile = ControllerUtil.foundObject(facsimileStore.findImageFile(facsimile, facsimileResolution));
		response.setContentType(facsimileResolution.getMimeType());
		response.setContentLength((int) facsimileImageFile.length());

		if (webRequest.checkNotModified(facsimileImageFile.lastModified()))
		{
			return;
		}

		ServletOutputStream responseStream = response.getOutputStream();
		InputStream imageStream = null;
		try
		{
			IOUtils.copy(imageStream = new FileInputStream(facsimileImageFile), responseStream);
			responseStream.flush();
		}
		finally
		{
			IOUtils.closeQuietly(imageStream);
		}
	}
}
