package config.properties;

import utils.ClassPathUtils;
import org.apache.log4j.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Property {
	
	private static Logger log = Logger.getLogger(Property.class);

	private Pattern formulaPattern = Pattern.compile("\\{[^\\{\\}][^\\{\\}]*\\}");
    private ScriptEngine javaScriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
    private String key;
	private boolean calculate;
	private String defaultValue;
	private String description;
	private PropertyStatus status = PropertyStatus.CODE_ONLY;
	private boolean registered = false;

	private Class<?> definingClass = null;

	//cached objects - MAKE SURE YOU ADD THEM TO THE clearCache() method
	private String textEnvVarExpression = null;
    private boolean isEnvVarExpression = false;

	private boolean isCachedString = false;
	private String cachedStringVal = null;
	private boolean isCachedBoolean = false;
	private boolean cachedBoolVal = false;
	private boolean isCachedLong = false;
	private long cachedLongVal = 0;
	private boolean isCachedInt = false;
	private boolean isCachedDouble = false;
	private int cachedIntVal = 0;
	private double cachedDoubleVal = 0;
	private boolean isCachedStringArray = false;
	private String[] cachedStringArrayVal = null;
	private boolean isCachedStringFromFile=false;
	private String cachedStringFromFile=null;

	private boolean isCachedStringSetVal = false;
	private Set<String> cachedStringSetVal = null;


	public void clearCache()
	{
		textEnvVarExpression = null;
	    isEnvVarExpression = false;
		isCachedString = false;
		cachedStringVal = null;
		isCachedBoolean = false;
		cachedBoolVal = false;
		isCachedLong = false;
		cachedLongVal = 0;
		isCachedInt = false;
		cachedIntVal = 0;
		isCachedDouble = false;
		cachedDoubleVal = 0;
		isCachedStringArray = false;
		cachedStringArrayVal = null;
		isCachedStringSetVal=false;
		cachedStringSetVal=null;
		isCachedStringFromFile=false;
		cachedStringFromFile=null;
	}
	
	
	public enum PropertyStatus
	{
		CODE_ONLY(1, "Code"), PROPERTIES_FILE_ONLY(2, "File"), BOTH(3, "Code and File");
		
		private int val;
		private String desc;
		
		private PropertyStatus(int value, String description) { val= value; desc=description; }

		public int getVal() {
			return val;
		}

		public String getDesc() {
			return desc;
		}
		
		
		
	}
	
     
	
	
    public String getDefaultValue() {
		return defaultValue;
	}


	public PropertyStatus getStatus() {
		return status;
	}


	public void setStatus(PropertyStatus status) {
		this.status = status;
	}


	
	//===================
	
	/*public Property(String key) {
		
		this.key = key;
		this.defaultValue = null;
	}*/

        
     public Class<?> getDefiningClass() {
		return definingClass;
	}


	public void setRegistered(boolean registered) {
		this.registered = registered;
	}


	public void setDefiningClass(Class<?> definingClass) {
		this.definingClass = definingClass;
	}


	/**
	 * Note Property can only be created as a member of a clss that implements PropertyDefinitions
	 * If it is not done that way, it will not be registered and will throw exception when it will be used
	 * @param key
	 * @param defaultValue  - can be null if no default value applies can also be pointing to an environment variable :  ${env var name}[:default value if env var does not exist]
	 * 		For example:    ${bis_monitor_ip}   or ${bis_monitor_ip}:147.11.88.13
	 * @param description
	 */
	public Property(String key, String defaultValue, String description) {
		this(key, defaultValue, description, false);
	}



	/**
      * Note Property can only be created as a member of a clss that implements PropertyDefinitions
      * If it is not done that way, it will not be registered and will throw exception when it will be used
      * @param key
      * @param defaultValue  - can be null if no default value applies can also be pointing to an environment variable :  ${env var name}[:default value if env var does not exist]
      * 		For example:    ${bis_monitor_ip}   or ${bis_monitor_ip}:147.11.88.13
      * @param description
      */
	public Property(String key, String defaultValue, String description, boolean calculate) {

		this.calculate = calculate;
		this.key = key;
		this.defaultValue = defaultValue;
		
		
		
		
		
		if(description == null || (description=description.trim()).length() == 0)
		{
			throw new RuntimeException("Property must have a description");
		}
			
		this.description = description;
	}
	
	
	
	/**
	 * 
	 * @param key
	 * @param defaultValue - can be null if no default value applies
	 * @param description
	 * @param status
	 */
	public Property(String key, String defaultValue,String description, PropertyStatus status ) {
		
		this(key,defaultValue,description);
		this.status = status;
	}
	
	
    public String getKey() {
		return key;
	}

    private void throwNotRegistered()
    {
    	
    	throw new RuntimeException("Property object with key <" + key + "> was not registerd in ConfigurationManager. Property must be declared as a member of a class that implements config.properties.PropertyDefinitions and ConfigurationManager must be initialized before using the property (ConfigurationManager.init(..) )");
    }
    
    
    
    public boolean getIsEnvVarExpression()
    {
    	return isEnvVarExpression;
    }
    
    private String getTextValue()
    {
    	if(isCachedString)
    	{
    		return cachedStringVal;
    	}
    	
    	textEnvVarExpression = null;
    	isEnvVarExpression = false;
    	
    	if(!registered) throwNotRegistered();
    	
    	String value = System.getProperty(key);
    	if(value == null)
    	{
    		value = defaultValue;
    	}
    	
    	if(value == null)
    	{
    		cachedStringVal = null;
    		isCachedString = true;
    		return null;
    	}
    	
    	if(value.startsWith("${") )
    	{
    		int pos = value.indexOf("}", 2);
    		if(pos< 0)
    		{
    			throw new RuntimeException("Property named " +  this.getKey() + " contains an invalid definition for environment variable: " + value);
    		}
    		
    		textEnvVarExpression = value;
    		isEnvVarExpression = true;
    		
    		String envVarKey = value.substring(2, pos);
    			
    		String envVarVal = System.getenv(envVarKey);
    		if(envVarVal == null)
    		{
    			//we want to look for default value in env var declaration only if we have }: and additional characters
    			if(value.length() > (pos+2)   && value.charAt(pos+1) == ':' )
    			{
    				envVarVal = value.substring(pos+2);
    			}
    		}
    		
    		cachedStringVal = envVarVal;
    		isCachedString = true;
    		return envVarVal;
    	}
    	else
    	{
    		cachedStringVal = value;
    		isCachedString = true;
    		return value;
    	}
    	
    }
    
	//===================        
	public String getValue() {
		String value = getTextValue();
		if(calculate && value.contains("{")){
			value = calculate(value);
		}
		cachedStringVal = value;
		return value;
	}
	
	
	

        //===================        
	public  int getIntValue() {
		
		if(isCachedInt) return cachedIntVal;

		String value = getTextValue().trim();
		cachedIntVal =  (value == null) ? 0 : (Integer.parseInt(value));
		isCachedInt = true;
		return cachedIntVal;
	}
	
	public  long getLongValue() {
		
		if(isCachedLong) return cachedLongVal;
		
		String value = getTextValue().trim();
		cachedLongVal =  (value == null) ? 0 : (Long.parseLong(value));
		isCachedLong = true;
		return cachedLongVal;
	}

	public  double getDoubleValue() {

		if(isCachedDouble) return cachedDoubleVal;

		String value = getTextValue().trim();
		cachedDoubleVal =  (value == null) ? 0 : (Double.parseDouble(value));
		isCachedDouble = true;
		return cachedDoubleVal;
	}
        //===================        
	public boolean getBooleanValue() {
		
		if(isCachedBoolean) return cachedBoolVal;
		
		
		String value = getTextValue();
		cachedBoolVal =  (value == null) ? false : (Boolean.parseBoolean(value.trim()));
		isCachedBoolean = true;
		return cachedBoolVal;
	}

    public String calculate(String value){
		String valueAfterDependencies = calculateDependencies(value);

		if(valueAfterDependencies.startsWith("<javascript>")) {
			String javascript = valueAfterDependencies.replaceFirst("<javascript>", "");
			try {
				javascript = "\"\" + (" + javascript + ")";
				return (String) javaScriptEngine.eval(javascript);
			} catch (ScriptException e) {
				throw new RuntimeException(String.format("script failed : %s", javascript));
			}
		}

		return valueAfterDependencies;
    }

	String calculateDependencies(String value) {
		Matcher matcher = formulaPattern.matcher(value);
		String valueAfterDependencies = value;
		while (matcher.find()) {
            String propertyName = matcher.group(0);
            propertyName = propertyName.replace("{", "").replace("}", "");
            String propertyValue = System.getProperty(propertyName, "");

			if(propertyValue != null && propertyValue.contains("{")) {
				propertyValue = calculateDependencies(propertyValue);
			}

            if(propertyValue == null){
                throw new RuntimeException(String.format("property dependency: %s is missing", propertyName));
            }
            valueAfterDependencies = valueAfterDependencies.replace("{" + propertyName + "}", propertyValue);
        }
		return valueAfterDependencies;
	}

	public String[] getStringArray() {

    	if(isCachedStringArray) return cachedStringArrayVal;
    	
    	String value = getTextValue();
    	
    	cachedStringArrayVal =  (value == null || value.trim().length() == 0) ? new String[0] : value.trim().split(",");
    	isCachedStringArray = true;
    	
    	return cachedStringArrayVal;
    }
    
    private static final String CLASS_PATH_PREFIX = "classpath:";
    public String getFileString()
    {
    	if(isCachedStringFromFile) return cachedStringFromFile;
    	
    	String fileName = getTextValue();
    	
    	String fileContent = null;
    	if(fileName != null && !fileName.trim().isEmpty())
    	{
    		//readFile from classpath
    		if(fileName.startsWith(CLASS_PATH_PREFIX))
    		{
    			if(fileName.length() > CLASS_PATH_PREFIX.length())
    			{
    				fileName = fileName.substring(CLASS_PATH_PREFIX.length());
    				try {
						fileContent = ClassPathUtils.readFileFromClasspath(fileName);
					} catch (IOException | URISyntaxException e) {
						log.error("Could not read file from property " + this.key,e);
					}
    			}
    			
    		}
    		//read file from file system
    		else
    		{
    			//regular file
    			try {
					fileContent = new String (Files.readAllBytes(new File(fileName).toPath()) ,StandardCharsets.UTF_8);
				} catch (IOException e) {
					log.error("Could not read file from property " + this.key,e);
				}
    		}
    	}
    	
    	cachedStringFromFile = fileContent;
    	isCachedStringFromFile=true;
    	if(fileContent == null)
    	{
    		//
    		throw new RuntimeException("Could not read file from property " + this.key);
    	}
    	
    	return fileContent;
    }

    public Set<String> getStringSet() {

    	if(isCachedStringSetVal) return cachedStringSetVal;
    	
    	String value = getTextValue();
    	
    	cachedStringSetVal =  new HashSet<String>( Arrays.asList(  (value == null || value.trim().length() == 0) ? new String[0] : value.trim().split(",")   )   );
    	isCachedStringSetVal = true;
    	
    	return cachedStringSetVal;
    }
    
    
    

	@Override
	public String toString() {
		
		return new StringBuilder().append(key).append(  "> with value <" ).append(getValue()).append("> ").append(" and description <").append(getDescription()).append(">. Default value is <").append(defaultValue==null?"":defaultValue).append(">").toString();
		
	}
	
	public String getDescription() {
		
		if(this.definingClass == null)
		{
			return description;
		}
		
		return new StringBuilder(description ).append(" ( Defined in: ").append(definingClass.getName()).append("). Default value is <").append(defaultValue==null?"":defaultValue).append(">").toString();
	}

	public String toPropertiesFileString()
	{
		return new StringBuilder().append(key).append("=" ).append(textEnvVarExpression == null ? getValue() : textEnvVarExpression).toString();
	}
	

	
}
