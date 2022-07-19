package kr.irm.fhir;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import kr.irm.fhir.util.MyResponseHandler;
import kr.irm.fhir.util.URIBuilder;
import org.apache.commons.cli.*;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MHDReferenceSearch extends UtilContext {
	private static final Logger LOG = LoggerFactory.getLogger(MHDReferenceSearch.class);

	public static void main(String[] args) {
		LOG.info("starting mhd reference search...");
		LOG.info("option args:{} ", Arrays.toString(args));
		Options opts = new Options();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		setOptions(opts);

		// parse options
		if (parseOptions(optionMap, opts, args)) {
			LOG.error("mhd reference search failed: invalid options");
			System.exit(1);
		}

		doSearch(optionMap);
	}

	private static void setOptions(Options opts) {
		// help
		opts.addOption("h", "help", false, "help");

		// Commons
		opts.addOption("o", OPTION_OAUTH_TOKEN, true, "OAuth Token");
		opts.addOption("s", OPTION_SERVER_URL, true, "FHIR Server Base URL");
		opts.addOption("ru", OPTION_REFERENCE_UUID, true, "DocumentReference.id (UUID)");
		opts.addOption("i", OPTION_ID, true, "id");
		opts.addOption("pu", OPTION_PATIENT_UUID, true, "Patient.id (UUID)");
		opts.addOption("pi", OPTION_PATIENT_IDENTIFIER, true, "patient.identifier");
		opts.addOption("st", OPTION_STATUS, true, "status");
		opts.addOption("id", OPTION_IDENTIFIER, true, "identifier");
		opts.addOption("dt", OPTION_DATE, true, "date");
		opts.addOption("af", OPTION_AUTHOR_FAMILY, true, "author.family");
		opts.addOption("ag", OPTION_AUTHOR_GIVEN, true, "author.given");
		opts.addOption("c", OPTION_CATEGORY, true, "category");
		opts.addOption("t", OPTION_TYPE, true, "type");
		opts.addOption("se", OPTION_SETTING, true, "setting");
		opts.addOption("p", OPTION_PERIOD, true, "period");
		opts.addOption("fa", OPTION_FACILITY, true, "facility");
		opts.addOption("e", OPTION_EVENT, true, "event");
		opts.addOption("sl", OPTION_SECURITY_LABEL, true, "security-label");
		opts.addOption("rf", OPTION_REFERENCE_FORMAT, true, "reference format (format)");
		opts.addOption("sr", OPTION_SORT, true, "sort");
		opts.addOption("of", OPTION_OFFSET, true, "offset");
		opts.addOption("co", OPTION_COUNT, true, "count");
		opts.addOption("f", OPTION_FORMAT, true, "Response Format (application/fhir+json or application/fhir+xml)");
	}

	private static boolean parseOptions(Map<String, Object> optionMap, Options opts, String[] args) {
		boolean error = false;
		CommandLineParser parser = new DefaultParser();

		try {
			CommandLine cl = parser.parse(opts, args);

			// HELP
			if (cl.hasOption("h") || args.length == 0) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp(
						"MHDReferenceSearch.sh [options]",
						"\nSearch Document Reference from MHD DocumentRecipient", opts,
						"Examples: $ ./MHDReferenceSearch.sh --manifest-uuid ...");
				System.exit(2);
			}

			// OAuth token (Required)
			if (cl.hasOption(OPTION_OAUTH_TOKEN)) {
				String oauth_token = cl.getOptionValue(OPTION_OAUTH_TOKEN);
				LOG.info("option {}={}", OPTION_OAUTH_TOKEN, oauth_token);

				optionMap.put(OPTION_OAUTH_TOKEN, oauth_token);
			}

			// FHIR
			// Server-url (Required)
			if (cl.hasOption(OPTION_SERVER_URL)) {
				String server_url = cl.getOptionValue(OPTION_SERVER_URL);
				LOG.info("option {}={}", OPTION_SERVER_URL, server_url);

				optionMap.put(OPTION_SERVER_URL, server_url);
			} else {
				error = true;
				LOG.error("option required: {}", OPTION_SERVER_URL);
			}

			// Reference ResourceId (UUID)
			if (cl.hasOption(OPTION_REFERENCE_UUID)) {
				String referenceUuid = cl.getOptionValue(OPTION_REFERENCE_UUID);
				LOG.info("option {}={}", OPTION_REFERENCE_UUID, referenceUuid);

				optionMap.put(OPTION_REFERENCE_UUID, referenceUuid);
			}

			// id (Reference UUID for Search)
			if (cl.hasOption(OPTION_ID)) {
				String id = cl.getOptionValue(OPTION_ID);
				LOG.info("option {}={}", OPTION_ID, id);

				optionMap.put(OPTION_ID, id);
			}

			// Patient UUID
			if (cl.hasOption(OPTION_PATIENT_UUID)) {
				String patientUuid = cl.getOptionValue(OPTION_PATIENT_UUID);
				LOG.info("option {}={}", OPTION_PATIENT_UUID, patientUuid);

				optionMap.put(OPTION_PATIENT_UUID, patientUuid);
			}

			// patient.identifier (ex. PatientIdValue^^^&AssignerId&AssignerIdType)
			if (cl.hasOption(OPTION_PATIENT_IDENTIFIER)) {
				String patientIdentifier = cl.getOptionValue(OPTION_PATIENT_IDENTIFIER);
				LOG.info("option {}={}", OPTION_PATIENT_IDENTIFIER, patientIdentifier);

				optionMap.put(OPTION_PATIENT_IDENTIFIER, patientIdentifier);
			}

			// status (Document Reference Status : current | superseded | entered-in-error)
			if (cl.hasOption(OPTION_STATUS)) {
				String[] component = cl.getOptionValue(OPTION_STATUS).split(",");
				List<String> statusList = getComponentList(component);
				LOG.info("option {}={}", OPTION_STATUS, statusList);

				optionMap.put(OPTION_STATUS, statusList);
			}

			// identifier (Other identifiers for the document)
			if (cl.hasOption(OPTION_IDENTIFIER)) {
				String[] component = cl.getOptionValue(OPTION_IDENTIFIER).split(",");
				List<String> identifierList = getComponentList(component);
				LOG.info("option {}={}", OPTION_IDENTIFIER, identifierList);

				optionMap.put(OPTION_IDENTIFIER, identifierList);
			}

			// date (When this document reference was created)
			if (cl.hasOption(OPTION_DATE)) {
				String[] component = cl.getOptionValue(OPTION_DATE).split(",");
				List<String> dateList = getComponentList(component);
				LOG.info("option {}={}", OPTION_DATE, dateList);

				optionMap.put(OPTION_DATE, dateList);
			}

			// author.family (Who and/or what authored the document)
			if (cl.hasOption(OPTION_AUTHOR_FAMILY)) {
				String[] component = cl.getOptionValue(OPTION_AUTHOR_FAMILY).split(",");
				List<String> authorFamilyList = getComponentList(component);
				LOG.info("option {}={}", OPTION_AUTHOR_FAMILY, authorFamilyList);

				optionMap.put(OPTION_AUTHOR_FAMILY, authorFamilyList);
			}

			// author.given (Who and/or what authored the document)
			if (cl.hasOption(OPTION_AUTHOR_GIVEN)) {
				String[] component = cl.getOptionValue(OPTION_AUTHOR_GIVEN).split(",");
				List<String> authorGivenList = getComponentList(component);
				LOG.info("option {}={}", OPTION_AUTHOR_GIVEN, authorGivenList);

				optionMap.put(OPTION_AUTHOR_GIVEN, authorGivenList);
			}

			// category (Categorization of document)
			if (cl.hasOption(OPTION_CATEGORY)) {
				String[] component = cl.getOptionValue(OPTION_CATEGORY).split(",");
				List<String> categoryList = getComponentList(component);
				LOG.info("option {}={}", OPTION_CATEGORY, categoryList);

				optionMap.put(OPTION_CATEGORY, categoryList);
			}

			// type (Kind of document (LOINC if possible))
			if (cl.hasOption(OPTION_TYPE)) {
				String[] component = cl.getOptionValue(OPTION_TYPE).split(",");
				List<String> typeList = getComponentList(component);
				LOG.info("option {}={}", OPTION_TYPE, typeList);

				optionMap.put(OPTION_TYPE, typeList);
			}

			// setting (Additional details about where the content was created (e.g. clinical specialty))
			if (cl.hasOption(OPTION_SETTING)) {
				String[] component = cl.getOptionValue(OPTION_SETTING).split(",");
				List<String> settingList = getComponentList(component);
				LOG.info("option {}={}", OPTION_SETTING, settingList);

				optionMap.put(OPTION_SETTING, settingList);
			}

			// period (Time of service that is being documented)
			if (cl.hasOption(OPTION_PERIOD)) {
				String[] component = cl.getOptionValue(OPTION_PERIOD).split(",");
				List<String> periodList = getComponentList(component);
				LOG.info("option {}={}", OPTION_PERIOD, periodList);

				optionMap.put(OPTION_PERIOD, periodList);
			}

			// facility (Kind of facility where patient was seen)
			if (cl.hasOption(OPTION_FACILITY)) {
				String[] component = cl.getOptionValue(OPTION_FACILITY).split(",");
				List<String> facilityList = getComponentList(component);
				LOG.info("option {}={}", OPTION_FACILITY, facilityList);

				optionMap.put(OPTION_FACILITY, facilityList);
			}

			// event (Main clinical acts documented)
			if (cl.hasOption(OPTION_EVENT)) {
				String[] component = cl.getOptionValue(OPTION_EVENT).split(",");
				List<String> eventList = getComponentList(component);
				LOG.info("option {}={}", OPTION_EVENT, eventList);

				optionMap.put(OPTION_EVENT, eventList);
			}

			// security-label (Document security-tags)
			if (cl.hasOption(OPTION_SECURITY_LABEL)) {
				String[] component = cl.getOptionValue(OPTION_SECURITY_LABEL).split(",");
				List<String> securityLabelList = getComponentList(component);
				LOG.info("option {}={}", OPTION_SECURITY_LABEL, securityLabelList);

				optionMap.put(OPTION_SECURITY_LABEL, securityLabelList);
			}

			// reference format (Format/content rules for the document)
			if (cl.hasOption(OPTION_REFERENCE_FORMAT)) {
				String[] component = cl.getOptionValue(OPTION_REFERENCE_FORMAT).split(",");
				List<String> formatList = getComponentList(component);
				LOG.info("option {}={}", OPTION_REFERENCE_FORMAT, formatList);

				optionMap.put(OPTION_REFERENCE_FORMAT, formatList);
			}

			// sort
			if (cl.hasOption(OPTION_SORT)) {
				String sort = cl.getOptionValue(OPTION_SORT);
				LOG.info("option {}={}", OPTION_SORT, sort);

				optionMap.put(OPTION_SORT, sort);
			}

			// offset
			if (cl.hasOption(OPTION_OFFSET)) {
				String offset = cl.getOptionValue(OPTION_OFFSET);
				LOG.info("option {}={}", OPTION_OFFSET, offset);

				optionMap.put(OPTION_OFFSET, offset);
			}

			// count
			if (cl.hasOption(OPTION_COUNT)) {
				String count = cl.getOptionValue(OPTION_COUNT);
				LOG.info("option {}={}", OPTION_COUNT, count);

				optionMap.put(OPTION_COUNT, count);
			}

			// format
			if (cl.hasOption(OPTION_FORMAT)) {
				String format = cl.getOptionValue(OPTION_FORMAT);
				LOG.info("option {}={}", OPTION_FORMAT, format);

				optionMap.put(OPTION_FORMAT, format);
			} else {
				String format = "application/fhir+json";
				optionMap.put(OPTION_FORMAT, format);
				LOG.info("option {}={}", OPTION_FORMAT, format);
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}

		return error;
	}

	private static String doSearch(Map<String, Object> optionMap) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		String httpResult = "";
		try {
			URIBuilder uriBuilder = new URIBuilder((String) optionMap.get(OPTION_SERVER_URL));
			uriBuilder.addPath("DocumentReference");

			if (optionMap.containsKey(OPTION_REFERENCE_UUID)) {
				uriBuilder.addPath((String) optionMap.get(OPTION_REFERENCE_UUID));
				uriBuilder.addParameter(OPTION_FORMAT, (String) optionMap.get(OPTION_FORMAT));
			} else {
				for (String key : optionMap.keySet()) {
					if (key != OPTION_OAUTH_TOKEN && key != OPTION_SERVER_URL && key != OPTION_REFERENCE_UUID) {
						if (optionMap.get(key) instanceof String) {
							uriBuilder.addParameter(key, (String) optionMap.get(key));
						} else if (optionMap.get(key) instanceof List) {
							for (String s : (List<String>) optionMap.get(key)) {
								uriBuilder.addParameter(key, s);
							}
						}
					}
				}
			}

			String searchUrl = uriBuilder.build().toString();
			LOG.info("search url : {}", searchUrl);

			HttpGet httpGet = new HttpGet(searchUrl);
			httpGet.setHeader("Authorization", "Bearer " + optionMap.get(OPTION_OAUTH_TOKEN));

			ResponseHandler<String> responseHandler = new MyResponseHandler();
			httpResult = httpClient.execute(httpGet, responseHandler);
			LOG.info("Response : \n{}", httpResult);
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		} finally {
			try {
				if (httpClient != null) httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return httpResult;
	}

	private static List<String> getComponentList(String[] component) {
		List<String> componentList = new ArrayList<>();
		for (String s : component) {
			componentList.add(s);
		}

		return componentList;
	}
}
