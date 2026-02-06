/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gov.nist.oism.asd.empbc.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;


public class ReflectionUtil {
    
     private static final Logger LOG = Logger.getLogger(ReflectionUtil.class.getSimpleName());
     
	/** Initial character and characters preceeded by '_' */
	private static final Pattern SETTER_NAME_RE =
			Pattern.compile("(?:^|_)([a-z])");
	
	private static final Pattern GETTER_NAME_RE =
			Pattern.compile("^(?:is|get)([A-Z])(\\w*)");
	
	/** New {@value clz} instance */
	public static <T> T createInstance(Class<T> clz) 
			throws Exception
	{
		try {
			final var constructor = clz.getConstructor();
			
			try {
				return constructor.newInstance();
			} catch (Exception e) { // InstantiationException...
				LOG.log(Level.SEVERE, e.getMessage(), e);
			}
		} catch (NoSuchMethodException | SecurityException e) {
			// Should not happen
			LOG.log(Level.SEVERE, e.getMessage(), e);
                        return null;
		}
                return null;
	}
	
	/**
	 * Sets a {@value value} of {@value type} in {@value dest} with 
	 * name {@value name}.
	 * 
	 * @return {@value true} = successful
	 */
	public static <T> boolean setInObject(Object dest, final String name, 
			Class<T> type, T value) 
	{
		if (StringUtil.isEmpty(name)) {
			return false;
		}
		
		String setterName = "set" +
				SETTER_NAME_RE.matcher(name)
					.replaceAll(m -> m.group(1).toUpperCase());
			
		try {
			final Method sm = dest.getClass().getMethod(setterName, type);
			sm.invoke(dest, value);
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Extracts all properties that have a getter of {@value type}.
	 */
	public static <T> Map<String, T> getFromObject(Object src, Class<T> type)
	{
		var res = new HashMap<String, T>();
		
		for (var mtd : src.getClass().getDeclaredMethods()) {
			if ((mtd.getParameterCount() == 0) &&
				(mtd.getReturnType() == type))
			{
				var gnMatcher = GETTER_NAME_RE.matcher(mtd.getName());
				if (gnMatcher.find()) {
					final String name = gnMatcher.group(1).toLowerCase() +
							gnMatcher.group(2);

					try {
						@SuppressWarnings("unchecked")
						T value = (T)mtd.invoke(src);
						
						if (value != null) {
							res.put(name, value);
						}
					} catch (Exception e) {
						// Should not happen
					}
				}
			}
		}
		
		return res;
	}
}
