package org.dosomething.android.transfer;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class PrizeItem implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String header;
	private String imageUrl;
	private String body;
	
	public PrizeItem() {}
	
	public PrizeItem(JSONObject obj) throws JSONException {
		
		header = obj.optString("item-header",null);
		imageUrl = obj.optString("item-image",null);
		body = obj.optString("item-body",null);	
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject obj = new JSONObject();
		
		obj.put("item-header", header);
		obj.put("item-iamge", imageUrl);
		obj.put("item-body", body);
		
		return obj;
	}
	
	public String getHeader() {
		return header;
	}
	public void setHeader(String header) {
		this.header = header;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
}
