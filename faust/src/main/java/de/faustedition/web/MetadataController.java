package de.faustedition.web;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.faustedition.metadata.EncodingStatus;
import de.faustedition.metadata.EncodingStatusManager;

@Controller
@RequestMapping("/metadata/")
public class MetadataController {
	private static final Logger LOG = LoggerFactory.getLogger(MetadataController.class);

	@Autowired
	private EncodingStatusManager encodingStatusManager;

	@Autowired
	private MessageSource messageSource;

	@RequestMapping("encoding/**")
	public void encodingStatus(HttpServletRequest request, ModelMap model, Locale locale) {
		String path = ControllerUtil.getPath(request, "metadata/encoding");

		LOG.debug("Delivering transcription status of '{}'", path);
		SortedMap<EncodingStatus, Integer> statusMap = encodingStatusManager.statusOf(path);
		List<Map<String, Object>> statusList = Lists.newArrayListWithCapacity(statusMap.size());
		for (EncodingStatus status : EncodingStatus.values()) {
			Map<String, Object> row = Maps.newHashMap();
			row.put("status", messageSource.getMessage("encodingStatus." + status.toString(), null, locale));
			row.put("count", statusMap.containsKey(status) ? statusMap.get(status) : 0);
			statusList.add(row);
		}
		model.addAttribute("statusList", statusList);
	}

//	@RequestMapping("fields/**")
//	public void metadata(HttpServletRequest request, HttpServletResponse response, ModelMap model, Locale locale)
//			throws IOException {
//		String path = ControllerUtil.getPath(request, "metadata/fields");
//		LOG.debug("Delivering metadata for '{}'", path);
//
//		MetadataRecord metadata = null;
//		// TODO: get metadata resource via XQuery
//		if (metadata == null) {
//			response.sendError(HttpServletResponse.SC_NOT_FOUND);
//			return;
//		}
//
//		List<Map<String, Object>> fields = Lists.newArrayList();
//		SortedMap<MetadataFieldGroup, MetadataRecord> metadataByField = MetadataFieldDefinition
//				.createStructuredMetadata(metadata);
//		for (MetadataFieldGroup fieldGroup : metadataByField.keySet()) {
//			List<Map<String, String>> groupFields = Lists.newArrayList();
//			MetadataRecord groupRecord = metadataByField.get(fieldGroup);
//			for (String fieldKey : groupRecord.keySet()) {
//				Map<String, String> field = Maps.newHashMap();
//				field.put("field", messageSource.getMessage("metadata." + fieldKey, null, locale));
//				field.put("value", groupRecord.get(fieldKey));
//				groupFields.add(field);
//			}
//
//			Map<String, Object> group = Maps.newHashMap();
//			group.put("group", messageSource.getMessage("metadata_group." + fieldGroup.toString().toLowerCase(), null,
//					locale));
//			group.put("fields", groupFields);
//			fields.add(group);
//		}
//
//		model.put("fields", fields);
//	}
}
