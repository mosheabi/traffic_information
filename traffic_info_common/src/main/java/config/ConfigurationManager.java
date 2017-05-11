package config;



import com.google.common.collect.Lists;
import config.notifications.NotificationsManager;
import config.properties.Property;
import config.properties.PropertyDefinitions;
import constants.TrafficInfoCmdParams;
import monitoring.MonitoringManager;
import monitoring.jmx.PushJVMData;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import utils.ResourceUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.Map.Entry;

public class ConfigurationManager
{


	private static final String CONFIGURATION_FILES = "configurationFiles";
	private static NotificationsManager notificationManager = new NotificationsManager();
	// ***  END: Manage notifications when configuration changes   ***

	// the creation of this logger might cause unneeded (and empty) logs to be created, since config.properties like app.name might not be initiated yet
	private static Logger log = Logger.getLogger(ConfigurationManager.class);

	private static ConfigurationManager _instance = new ConfigurationManager();

	public static ConfigurationManager getInstance() { return _instance; }


	private Map<String, Property> configuration = new HashMap<>();

	Properties Properties = null;

	private static String processIdentifier = "";

	private static void calcProcessIdentifier()
	{
		try {
			processIdentifier = applicationName+ "." + InetAddress.getLocalHost().getHostName().replace('.', '_')+ "." + appProcessInstanceId;
		} catch ( IOException e) {
			log.error("Failed getting host name ", e);
			processIdentifier = applicationName + "." + "Unkown Address" +  "." + appProcessInstanceId;
			//new File(new File(".").getAbsolutePath()).getName();
		}

		//Will be used by 3rd parties like jmx-trans
		System.setProperty("bis.appplication.instance.name", processIdentifier);
	}

	private Set<Class<? extends PropertyDefinitions>> registeredClasses = new HashSet<>();

	@SuppressWarnings("unchecked")
    private void registerProperties(Class<? extends PropertyDefinitions> configurationClass )
	{
		if (configurationClass == null) {
			return;
		}

		//avoid same class to register more than once
		if(registeredClasses.contains(configurationClass))
		{
			if (log.isDebugEnabled())
			{
				log.debug("PropertiesDefinition class is registering again: " + configurationClass.getName());
			}
			return;
		}
		registeredClasses.add(configurationClass);


		if(initPropertiesClass == null)
		{
			initPropertiesClass = configurationClass;
		}

		List<Field> fields = getAllFields(configurationClass);
		if(fields == null)
		{
			return;
		}

		//register imported classes first - so the order is imported first - which allows overriding config.properties in the Application level
		for (Field f : fields) {
		    /*
		     * only hardcore!
		     *
			//noinspection unchecked
			Class<? extends PropertyDefinitions> memberClass = (Class<? extends PropertyDefinitions>)f.getGenericType();
			 */

            Class<?> memberClass = f.getType();
            if (PropertyDefinitions.class.isAssignableFrom(memberClass)) {
                registerProperties((Class<PropertyDefinitions>) memberClass);
            }
		}

		//register config.properties
        for (Field f : fields) {
            Class<?> memberClass = f.getType();

            if (Property.class.isAssignableFrom(memberClass)) {
                registerProperty(getMemberValue(f, configurationClass), configurationClass);
                continue;
            }
            if (Collection.class.isAssignableFrom(memberClass)) {
                Collection<Object> collection = (Collection<Object>) getMemberValue(f, configurationClass);
                if (CollectionUtils.isNotEmpty(collection)) {
                    for (Object p : collection) {
                        registerProperty(p, configurationClass);
                    }
                }
                continue;
            }
            if (Map.class.isAssignableFrom(memberClass)) {
                Map<Object, Object> map = (Map<Object, Object>) getMemberValue(f, configurationClass);
                if (MapUtils.isNotEmpty(map)) {
                    for (Entry<Object, Object> entry : map.entrySet()) {
                        registerProperty(entry.getKey(), configurationClass);
                        registerProperty(entry.getValue(), configurationClass);
                    }
                }
			}
        }
	}

	private List<Field> getAllFields(Class<?> startClass) {
		List<Field> currentClassFields = Lists.newArrayList(startClass.getDeclaredFields());
		Class<?> parentClass = startClass.getSuperclass();

		if (parentClass != null) {
			List<Field> parentClassFields = getAllFields(parentClass);
			currentClassFields.addAll(parentClassFields);
		}

		Class<?>[] interfaces = startClass.getInterfaces();
		for (Class<?> interfaceClass:interfaces){
			List<Field> interfaceClassFields = getAllFields(interfaceClass);
			currentClassFields.addAll(interfaceClassFields);
		}


		return currentClassFields;
	}

    private Object getMemberValue(Field f, Class<? extends PropertyDefinitions> configurationClass) {
        Object value = null;
        try {
            value = f.get(null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            log.error(String.format("Failed to load configuraion definitions from class: %s", configurationClass.getName()), e);
        }
        return value;
    }

	private void registerProperty(Object o, Class<? extends PropertyDefinitions> configurationClass) {
	    if (o == null || !Property.class.isAssignableFrom(o.getClass())) {
            return;
	    }

	    Property p = (Property) o;
	    p.setDefiningClass(configurationClass);
        p.setRegistered(true);
        configuration.put(p.getKey(), p);
	}



	private static String lastConfigurationFile = null;
	private static String applicationName;
	private static String appProcessInstanceId=null;
	private static String queryKey = null;

	/**
	 * initialize the application using the command line arguments
	 * Alternative is a static init() that gets all the parameters (also those that suppose to come from the command line args)
	 * @throws Exception
	 */
	public static void init(String applicationName, String[] commandLineArgs, Class<? extends PropertyDefinitions> propertiesDeficitionsClass, Properties overridingProperties) throws Exception
	{
		//String x  = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath()).getName();
		//URLDecoder.decode(ClassLoader.getSystemClassLoader().getResource(".").getPath(‌​), "UTF-8");
		//File f = new File(MyClass.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		//System.out.println(f.getPath());
		//System.out.println("File name = " + x );

	    ArgumentParser parser = getDefaultArgParser(applicationName);
	    init(applicationName, commandLineArgs, parser, propertiesDeficitionsClass, overridingProperties);
	}

    public static Namespace init(String applicationName, String[] args, ArgumentParser parser, Class<? extends PropertyDefinitions> propertiesDeficitionsClass, Properties overridingProperties) throws Exception {
        if (!System.getProperty("file.encoding").equals("UTF-8")) {
            System.out.println("Execution must include \"-Dfile.encoding=UTF-8\" .");
            System.out.println(parser.formatHelp());
            System.exit(2);
        }

        Namespace parsedArgs = null;
        try {
            parsedArgs = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        String configurationFiles = parsedArgs.getString(CONFIGURATION_FILES);
        String logConfigurationFile = parsedArgs.getString("logConfigurationFile");
        String appInstnaceId = parsedArgs.getString("appInstnaceId");
        String queryKey = parsedArgs.getString(TrafficInfoCmdParams.QUERY_KEY);

        // publish the appInstance as system property to be used by log4j
        System.setProperty("processId", appInstnaceId);
        System.setProperty(TrafficInfoCmdParams.QUERY_KEY,queryKey);

        init(applicationName, appInstnaceId, propertiesDeficitionsClass, configurationFiles, logConfigurationFile, overridingProperties);

        GeneralCounters.startupCounter.set(1);

        return parsedArgs;
    }

	public static ArgumentParser getDefaultArgParser(String applicationName) {
        ArgumentParser parser = ArgumentParsers.newArgumentParser(applicationName).defaultHelp(true);//.//description("java -jar -server -Dfile.encoding=UTF-8 <Jar Name>.jar ..args" ).;
        parser.addArgument( "-processId" ).dest("appInstnaceId").required(true).help("Unique identifier of this instance of the application");
        parser.addArgument("-p").dest(CONFIGURATION_FILES).required(true).help("Path to the config.properties file");
        parser.addArgument("-log").dest("logConfigurationFile").required(false).help("Path to the log4j config.properties file").setDefault("log4j.properties");
        //parser.addArgument("-help").action(Arguments.help());
		parser.addArgument("-query_key").dest("queryKey").required(true).help("Query name to get DB data ");
        parser.usage(parser.formatUsage().replace("usage: ", "").replace(applicationName, "java -jar -server -Dfile.encoding=UTF-8 <Jar Name>.jar ") );
        // .usage("java -jar -server -Dfile.encoding=UTF-8 <Jar Name>.jar ..args") //java -jar -server -Dfile.encoding=UTF-8 <Jar Name>.jar

        return parser;
	}


	/**
	 * initialization method for configuration
	 * @param applicationName  unique application name
	 * @param appInstnaceId  unique identifier of this process in this machine
	 * @param propertiesDeficitionsClass the config.properties class of this application - mandatory
	 * @param configurationFiles path for config.properties file or null
	 * @param logConfigurationFile path for log4j.config.properties file or null to search for current directory: log4j.config.properties
	 * @throws Exception
	 */
	public  static void init(String applicationName, String appInstnaceId, Class<? extends PropertyDefinitions> propertiesDeficitionsClass, String configurationFiles, String logConfigurationFile, Properties overridingProperties ) throws Exception
	{
		appProcessInstanceId = appInstnaceId;
		System.setProperty("processId", appInstnaceId);
		registerShutdownHook();

		lastConfigurationFile = null;
		ConfigurationManager.applicationName= applicationName;
		if(applicationName == null)
		{
			throw new Exception("Application name is mandatory for configutration");
		}
		calcProcessIdentifier();


		//just to be on the safe side - creates a new singleton and notification manager
		_instance = new ConfigurationManager();
		notificationManager = new NotificationsManager();

		ConfigurationManager instance =  getInstance();
		instance.registerProperties(propertiesDeficitionsClass);

		//always register those config.properties
		instance.registerProperties(GeneralProperties.class);

		instance.initConfiguration(configurationFiles, logConfigurationFile,overridingProperties);

		lastConfigurationFile = configurationFiles;

		MonitoringManager.getInstance().init();

		//start JVM JMX push to graphite
		new PushJVMData().start();
	}



	/**
	 *
	 * @param configurationFile if null - will use last configuration file
	 * @throws Exception
	 */
	public  static void refresh(String configurationFile) throws Exception
	{
		refreshWithOverridingProperties( configurationFile, null);
	}

	private Class<? extends PropertyDefinitions> initPropertiesClass = null;

	/**
	 *
	 * param configurationFiles if null - will use last configuration file
	 * @param overridingProperties if null will be ignored, otherwise overrides config.properties from teh configuration file
	 * @throws Exception
	 */
	public  static void refreshWithOverridingProperties(String configurationFile, Properties overridingProperties) throws Exception
	{
		if(configurationFile == null)
		{
			configurationFile = lastConfigurationFile;
		}


		ConfigurationManager instance = new ConfigurationManager();
		instance.registerProperties(_instance.initPropertiesClass);

		//always register those config.properties
		instance.registerProperties(GeneralProperties.class);

		instance.initConfiguration(configurationFile, configurationFile,overridingProperties);


		_instance = instance;
		sendConfigurationChangeNotification();

	}

	private static void sendConfigurationChangeNotification()
	{
		notificationManager.publishConfigurationChange();
	}

	private void initConfiguration(String configurationFiles, String logConfigurationFile, Properties overridingProperties) throws Exception {
		final Properties props = createProperties(configurationFiles, overridingProperties);
		final InputStream logFis = getInputStream(logConfigurationFile!=null ? logConfigurationFile : "log4j.properties");

		try {
			initConfiguration(props, logFis);
		} catch(Exception e) {
			log.error("Exception when trying to load config.properties file " + configurationFiles + " and  log4j configuration file " + logConfigurationFile);
			throw e;
		}

		finally {
			if(logFis != null){
				try {
					logFis.close();
				} catch (IOException e) {
					log.error("error closing input stream", e);
				}
			}
		}


	}

	private Properties createProperties(String configurationFiles, Properties overridingProperties) {
		final Properties props = new Properties();
		for (final String configurationFile : configurationFiles.split(";")) {
			loadConfigurationFile(props, configurationFile);
		}

		if(overridingProperties != null) {
			props.putAll(overridingProperties);
		}
		return props;
	}

	private void loadConfigurationFile(Properties props, String configurationFile) {
		final InputStream propFis = getInputStream(configurationFile);

        if (propFis!=null) {
            final Properties tempProps = new Properties();
            loadProperties(propFis, tempProps);

            //trim all conf values
            for (Enumeration<?> propKeys = tempProps.propertyNames(); propKeys.hasMoreElements();) {
                // get the value, trim off the whitespace, then store it
                // in the received config.properties object.
                String tmpKey = (String) propKeys.nextElement();
                String tmpValue = tempProps.getProperty(tmpKey);
                tmpValue = tmpValue.trim();
                props.put(tmpKey, tmpValue);
            }
        }
	}

    private void loadProperties(InputStream propFis, Properties tempProps) {
        try {
            tempProps.load(propFis);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties from file");
        } finally {
            try {
                propFis.close();
            } catch (IOException e) {
                //noinspection ThrowFromFinallyBlock
                throw new RuntimeException("Failed to close InputStream", e);
            }
        }
    }

    private InputStream getInputStream(String file) {
        if (file!=null) {
            final InputStream propFis;
            File propConfig = new File(file);
            if (!propConfig.exists()) {
                propFis = ResourceUtils.findStream(file);
                if (propFis == null) {
                    throw new RuntimeException("File [" + file + "] could not be loaded");
                }
            } else {
                try {
                    propFis = new FileInputStream(propConfig);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("Should never happen. A file couldn't be found although we just saw it for file [" + file + "]");
                }
            }
            return propFis;
        }
        return null;
	}


	private void makeSureSomeLoggersAreAtLeastInInfoLevel(String ... loggerNames)
	{
		for(String loggerName:loggerNames)
		{
			Logger l = Logger.getLogger(loggerName);
			if( !l.isInfoEnabled())
			{
				l.setLevel(Level.INFO);
			}
		}
	}

	private void initConfiguration(Properties props,InputStream logFis) throws Exception {
		// load log4j confirguration
		PropertyConfigurator.configure(logFis);


		makeSureSomeLoggersAreAtLeastInInfoLevel(ConfigurationManager.class.getName());
		makeSureSomeLoggersAreAtLeastInInfoLevel(ConfigurationManager.class.getName());


		//if (log.isInfoEnabled()) log.info("***** Setting loading Properties *****");
		Set<Object> keys = props.keySet();



		//load all config.properties from file to the configuration object
		for (Object key : keys) {
			String keyStr = (String)key;

			String value = props.getProperty(keyStr);




			Property p = configuration.get(key);
			if(p == null)
			{
				p = new Property(keyStr,value,"Attribute Defined in peoprties file only", Property.PropertyStatus.PROPERTIES_FILE_ONLY);
				p.setRegistered(true);
				configuration.put(keyStr, p);
			}
			else
			{
				p.setStatus(Property.PropertyStatus.BOTH);
			}
		}

		//go Over configuration and update PropertiesObject with missing config.properties
		for(Property p:configuration.values())
		{
			String key = p.getKey();
			String val = p.getDefaultValue();

			//this is the case where a property exists in the code definitions but not in the config.properties file
			if(!props.containsKey(key))
			{



				//if it has a default value, we can put it in the config.properties object - otherwise we need to fail
				if(val == null)
				{
					throw new Exception("A property named "+ key +  " Exist in the code without default value and does not exist in the configuration file");
				}
				props.put(key, val);
			}

			System.setProperty(key, props.getProperty(key));
		}

		// get values after all config.properties are set
		for (Property property : configuration.values()) {
			property.getValue();
		}


		Properties = props;




		String stateStr = "";
		List<String> content = new ArrayList<>();
		for(Property p: getPropertiesOrderedByStatus() )
		{
			if(!p.getStatus().getDesc().equals(stateStr))
			{
				stateStr = p.getStatus().getDesc();
				content.add("");
				content.add("# " + stateStr);
				content.add("# -------------------");
			}

			//if this property is based on environment variable - the env expression is in the value - so put the calculated value in the description
			String descriptionPrefix = !p.getIsEnvVarExpression() ? "" :("(Value calculated from environment variable: " + p.getValue() + ") " );

			content.add("# " + descriptionPrefix + p.getDescription());

			content.add(p.toPropertiesFileString());
			//content.add("");
		}

		Files.write(new File("ActualConfiguration_" +appProcessInstanceId + ".config.properties").toPath(), content,StandardCharsets.UTF_8,StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE );

		if (log.isInfoEnabled())
		{

			log.info("Configuration state:");
			log.info("=====================");
			for(String s:content)
			{
				log.info(s);
			}
			log.info("");
		}

		Properties = props;
	}


	public List<Property> getPropertiesOrderedByStatus()
	{
		List<Property> ret = new ArrayList<>();
		ret.addAll( this.configuration.values()  );
		Collections.sort(ret , new Comparator<Property>()
		{

			@Override
			public int compare(Property o1, Property o2)
			{
				int ret =  o1.getStatus().compareTo(o2.getStatus());

				//if they are equal by status - so both of them have or do not have defining class
				if(ret == 0 && o1.getDefiningClass() != null)
				{
					ret = o1.getDefiningClass().getName().compareTo( o2.getDefiningClass().getName()  );
				}

				if(ret == 0)
				{
					ret = o1.getKey().compareTo(o2.getKey());
				}
				return ret;
			}

		}   );
		return ret;
	}

	/**
	 * @return the Properties as a Properties object
	 */
	public Properties getPropertiesObject()
	{
		return this.Properties;
	}


	public String getProccessIdentifier()
	{
		return processIdentifier;
	}


	public static NotificationsManager getNotificationsManager()
	{
		return notificationManager;
	}


	private static void registerShutdownHook()
	{

		//register shutdownhook
		Runtime.getRuntime().addShutdownHook(new Thread()
		{	@Override
			 public void run() {


				getInstance().close();
			}
		} );


	}

	/**
	 * should only be used in testing for specific cases that does not need refresh
	 */
	public void changePropertyForTestingOnly(String key, String val)
	{
		Property p =this.configuration.get(key);
		if( p != null)
		{

			System.setProperty(key, val);
			p.clearCache();
		}
	}

	public void close()
	{

		getNotificationsManager().publishStopEvent();

		//tODO maybe more closing
	}

	public  String getAppProcessInstanceId() {
		return appProcessInstanceId;
	}



}
