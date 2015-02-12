package cz.metacentrum.perun.wui.json.managers;

import com.google.gwt.http.client.Request;
import cz.metacentrum.perun.wui.json.JsonClient;
import cz.metacentrum.perun.wui.json.JsonEvents;

/**
 * Manager with standard callbacks to Perun's API (utils).
 * <p/>
 * Each callback returns unique Request used to make call. Such call can be removed
 * while processing to prevent any further actions based on it's {@link cz.metacentrum.perun.wui.json.JsonEvents JsonEvents}.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class UtilsManager {

	private static final String UTILS_MANAGER = "utils/";

	/**
	 * Logout from Perun. Destroys session on server side.
	 *
	 * In order to logout locally in browser, Utils.clearFederationCookies() must be called.
	 * If using Kerberos authz, user must close browser in order to logout.
	 *
	 * @param events events done on callback
	 * @return Request unique request
	 */
	public static Request logout(JsonEvents events) {

		JsonClient client = new JsonClient(events);
		return client.call(UTILS_MANAGER+"logout");

	}

	/**
	 * Get GUIs configuration defined by Perun instance.
	 *
	 * @param events events done on callback
	 * @return Request unique request
	 */
	public static Request getGuiConfiguration(JsonEvents events) {

		JsonClient client = new JsonClient(events);
		return client.call(UTILS_MANAGER+"getGuiConfiguration");

	}

	/**
	 * Get list of pending requests, which are currently processed by the server side of Perun.
	 *
	 * @param events events done on callback
	 * @return Request unique request
	 */
	public static Request getPendingRequests(JsonEvents events) {

		JsonClient client = new JsonClient(events);
		return client.call("getPendingRequests");

	}

	/**
	 * Get pending request by it's unique name. Response is NULL if no such request is present.
	 *
	 * @param callbackName unique name of callback
	 * @param events events done on callback
	 * @return Request unique request
	 */
	public static Request getPendingRequest(String callbackName, JsonEvents events) {

		JsonClient client = new JsonClient(events);
		client.put("callbackId", callbackName);
		return client.call("getPendingRequests");

	}

	/**
	 * Get list of string with status information about Perun instance, like Java version, DB driver and type etc.
	 *
	 * @param events events done on callback
	 * @return Request unique request
	 */
	public static Request getPerunStatus(JsonEvents events) {

		JsonClient client = new JsonClient(events);
		return client.call(UTILS_MANAGER+"getPerunStatus");

	}

}