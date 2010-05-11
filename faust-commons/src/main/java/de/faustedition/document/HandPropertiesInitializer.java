package de.faustedition.document;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Service
public class HandPropertiesInitializer implements InitializingBean {

	@Autowired
	private SimpleJdbcTemplate jt;

	@Autowired
	private PlatformTransactionManager transactionManager;

	public void afterPropertiesSet() throws Exception {
		new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				if (jt.queryForInt("select count(*) from hand") == 0) {
					List<SqlParameterSource> handData = Lists.newArrayList();
					for (HandProperties hand : createHands()) {
						handData.add(hand.toSqlParameterSource());
					}
					jt.batchUpdate("insert into hand (scribe, material, style) values (:scribe, :material, :style)",//
							handData.toArray(new SqlParameterSource[handData.size()]));
				}
			}
		});
	}

	public List<HandProperties> createHands() {
		List<HandProperties> hands = Lists.newArrayList();

		SortedSet<Scribe> scribes = Sets.newTreeSet(Lists.newArrayList(Scribe.values()));
		SortedSet<WritingMaterial> materials = Sets.newTreeSet(Lists.newArrayList(WritingMaterial.values()));
		materials.remove(WritingMaterial.BLUE);

		List<FontStyle> styles = Arrays.asList(FontStyle.values());

		// Goethe
		for (WritingMaterial material : materials) {
			for (FontStyle style : styles) {
				hands.add(new HandProperties(Scribe.GOETHE, material, style));
			}
		}
		scribes.remove(Scribe.GOETHE);

		materials.remove(WritingMaterial.CHARCOAL);
		materials.remove(WritingMaterial.RUDDLE);

		// Goethe's scribes
		SortedSet<Scribe> other = Sets.newTreeSet(Sets.newHashSet(Scribe.CONTEMPORARY, Scribe.UNKNOWN_SCRIBE_1, Scribe.UNKNOWN_SCRIBE_2, Scribe.UNKNOWN_SCRIBE_3));
		scribes.removeAll(other);

		for (Scribe scribe : scribes) {
			for (WritingMaterial material : materials) {
				for (FontStyle style : styles) {
					hands.add(new HandProperties(scribe, material, style));
				}
			}
		}

		materials.add(WritingMaterial.BLUE);

		// misc. scribes
		for (Scribe scribe : other) {
			for (WritingMaterial material : materials) {
				for (FontStyle style : styles) {
					hands.add(new HandProperties(scribe, material, style));
				}
			}
		}

		return hands;
	}
}
