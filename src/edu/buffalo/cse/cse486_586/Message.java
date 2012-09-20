package edu.buffalo.cse.cse486_586;

import edu.buffalo.cse.cse486_586.provider.MessageProvider;
import android.net.Uri;
/*
 * Base class for holding data for a message to be entered into content provider
 */
public class Message {
	Message() {
	}

	public static final Uri CONTENT_URI = Uri.parse("content://"+ MessageProvider.AUTHORITY);

	public static final String CONTENT_TYPE = "";

	public static final String MSG_ID = "_id";

	public static final String provider_key = "provider_key";

	public static final String provider_value = "provider_value";
}
