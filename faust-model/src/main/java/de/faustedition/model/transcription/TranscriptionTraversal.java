package de.faustedition.model.transcription;

import java.util.LinkedList;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import de.faustedition.model.repository.DataRepository;
import de.faustedition.model.repository.DataRepositoryTemplate;

public class TranscriptionTraversal {
	public static <T> List<T> execute(DataRepository repository, final TranscriptionVisitor<T> visitor) throws RepositoryException {
		return repository.execute(new DataRepositoryTemplate<List<T>>() {

			@Override
			public List<T> doInSession(Session session) throws RepositoryException {
				List<T> resultList = new LinkedList<T>();
				for (Repository repository : Repository.find(session)) {
					for (Portfolio portfolio : Portfolio.find(session, repository)) {
						for (Transcription transcription : Transcription.find(session, portfolio)) {
							T result = visitor.visit(session, transcription);
							if (result != null) {
								resultList.add(result);
							}
						}
					}
				}
				return resultList;
			}
		});
	}
	
	public interface TranscriptionVisitor<T> {
		T visit(Session session, Transcription transcription) throws RepositoryException;
	}
}
