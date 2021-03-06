package org.dosomething.android.transfer;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class SFGGalleryItem implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private boolean adopted;
	private String bottomText;
	private String city;
	private int id;
	private String imageURL;
	private String name;
	private int shareCount;
	private String shelter;
	private String state;
	private String story;
	private String topText;
	
	public SFGGalleryItem(JSONObject obj) throws JSONException {
		
		// Required items
		imageURL = obj.getString("image");
		name = obj.getString("name");
		
		// Optional items
		adopted = obj.optBoolean("adopted", false);
		bottomText = obj.optString("bottom_text", null);
		id = obj.optInt("id", 0);
		shelter = obj.optString("shelter", null);
		city = obj.optString("city", null);
		shareCount = obj.optInt("share_count", 0);
		state = obj.optString("state", null);
		story = obj.optString("story", null);
		topText = obj.optString("top_text", null);
	}
	
	public boolean getAdopted() {
		return adopted;
	}
	
	public String getBottomText() {
		return bottomText;
	}
	
	public String getCity() {
		return city;
	}
	
	public int getId() {
		return id;
	}
	
	public String getImageURL() {
		return imageURL;
	}
	
	public String getName() {
		return name;
	}
	
	public int getShareCount() {
		return shareCount;
	}
	
	public String getShelter() {
		return shelter;
	}
	
	public String getState() {
		return state;
	}
	
	public String getStory() {
		return story;
	}
	
	public String getTopText() {
		return topText;
	}
}
