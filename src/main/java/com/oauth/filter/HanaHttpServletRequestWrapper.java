package com.oauth.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.*;

public class HanaHttpServletRequestWrapper extends HttpServletRequestWrapper {

	private Map<String, String> hanaHeader;
	private Map<String, String[]> hanaParams;

	public HanaHttpServletRequestWrapper(HttpServletRequest request) {
		super(request);
		this.hanaHeader = new HashMap<String, String>();
		this.hanaParams = new HashMap<String, String[]>(request.getParameterMap());
	}

	public void putHeader(String name, String value) {
		this.hanaHeader.put(name, value);
	}

	public void putParameter(String name, String value) {
		String[] singleValue = {value};
		this.hanaParams.put(name, singleValue);
	}
	public void putParameterValues(String name, String[] value){
		this.hanaParams.put(name, value);
	}

	public String getHeader(String name) {
		String headerValue = hanaHeader.get(name);

		if (headerValue != null) {
			return headerValue;
		}
		return ((HttpServletRequest) getRequest()).getHeader(name);
	}
	
	public Map<String, String[]> getParameterMap() {
		return Collections.unmodifiableMap(hanaParams);
	}
	
	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(hanaParams.keySet());
	}
	public String getParameter(String name){
		String returnValue = null;
		String[] paramArray = getParameterValues(name);
		if (paramArray != null && paramArray.length > 0){
			returnValue = paramArray[0];
		}
		return returnValue;
	}
	
	public String[] getParameterValues(String name) {
		String[] result = null;
		String[] temp = (String[]) hanaParams.get(name);
		if (temp != null) {
			result = new String[temp.length];
			System.arraycopy(temp, 0, result, 0, temp.length);
		}
		return result;
	}

	public Enumeration<String> getHeaderNames() {
		Set<String> set = new HashSet<String>(hanaHeader.keySet());

		Enumeration<String> e = ((HttpServletRequest) getRequest()).getHeaderNames();
		while (e.hasMoreElements()) {
			String n = e.nextElement();
			set.add(n);
		}
		return Collections.enumeration(set);
	}

}
