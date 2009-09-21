package de.faustedition.web.facsimile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

import de.faustedition.model.ObjectNotFoundException;
import de.faustedition.model.manuscript.Facsimile;
import de.faustedition.model.manuscript.FacsimileImageDao;
import de.faustedition.model.manuscript.FacsimileImageResolution;
import de.faustedition.util.ReadOnlyTransactionTemplate;
import de.faustedition.web.FaustPathUtils;

@Controller
public class FacsimileController
{
	public static final String URL_PREFIX = "facsimile";
	@Autowired
	private FacsimileImageDao facsimileStore;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private SessionFactory dbSessionFactory;

	@RequestMapping("/" + URL_PREFIX + "/**")
	public void stream(WebRequest webRequest, HttpServletRequest request, HttpServletResponse response) throws ObjectNotFoundException, IOException
	{
		String path = FaustPathUtils.getPath(request);

		FacsimileImageResolution facsimileResolution = null;
		for (FacsimileImageResolution resolution : FacsimileImageResolution.values())
		{
			if (resolution.matches(path))
			{
				facsimileResolution = resolution;
				path = StringUtils.removeEnd(path, resolution.getSuffix());
			}
		}

		if (facsimileResolution == null || facsimileResolution == FacsimileImageResolution.HIGH)
		{
			throw new ObjectNotFoundException();
		}

		final String imagePath = path;
		Facsimile facsimile = (Facsimile) new ReadOnlyTransactionTemplate(transactionManager).execute(new TransactionCallback()
		{

			@Override
			public Object doInTransaction(TransactionStatus status)
			{
				return Facsimile.findByImagePath(dbSessionFactory.getCurrentSession(), imagePath);
			}
		});
		final File facsimileImageFile = facsimileStore.findImageFile(facsimile, facsimileResolution);
		if (facsimile == null)
		{
			throw new ObjectNotFoundException(path);
		}

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
		} finally
		{
			IOUtils.closeQuietly(imageStream);
		}
	}
}
