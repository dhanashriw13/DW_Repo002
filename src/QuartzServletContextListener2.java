/**
 *  Copyright 2009. All rights reserved.
 *  Use is subject to license terms
 *  QuartzServletContextListener.java
 *  Author: Harish Nanda
 */

package com.ice.router.servlet;




import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Inet4Address;
import java.text.DateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import com.ice.comparison.runer.EngineInstance;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.xml.sax.SAXException;

import com.ice.bpel.AbortRuleRun;
import com.ice.common.ICEContext;
import com.ice.engine.LClient;
import com.ice.scheduler.MyJobListener;
import com.ice.scheduler.ScheduleJobs;
import com.ice.utils.CaseInsensitiveProperties;
import com.ice.utils.Constants;
import com.ice.utils.GenericConstants;
import com.ice.utils.GenericUtils;
import com.ice.utils.XMLUtils;

public class QuartzServletContextListener implements ServletContextListener, GenericConstants
{
    private static Log logger = LogFactory.getLog(QuartzServletContextListener.class);
    public static final String QUARTZ_FACTORY_KEY = "org.quartz.impl.StdSchedulerFactory.KEY";
    private ServletContext ctx = null;
    private StdSchedulerFactory factory = null;
    
    static LClient lc = null;
    /**
     * Called when the container is shutting down.
     * @param sce
     * @throws SAXException 
     * @throws ParserConfigurationException 
     */
    @SuppressWarnings("static-access")
	public void contextDestroyed(ServletContextEvent sce)
    {
        try {
              factory.getDefaultScheduler().shutdown();
//              updateRunningRules(); //Not required
              ICEContext.instance().closeUserLogs();
              
              ExecutorService es = ICEContext.instance().getRuleExecutor();
              if (es != null)
            	  es.shutdown();
        } catch (SchedulerException ex) {
              logger.error("Error stopping Quartz", ex);
        }
        catch (Exception ioe)
        {
        	ioe.printStackTrace();
        }
        
        
        //tlm.remove();
    }
    
    /**
     * Called when the container is first started.
     * @param sce
     */
    @SuppressWarnings("static-access")
	public void contextInitialized(ServletContextEvent sce)
    {
         ctx = sce.getServletContext();
         try
         {
        	String planFileDir = Constants.SSL_DIR;
        	if (planFileDir == null || "".equals(planFileDir.trim()) || "null".equalsIgnoreCase(planFileDir.trim()))
        		planFileDir = Constants.DATA_DIR;
 	   		File planFileDirFile = new File (planFileDir);
 	   		if (!planFileDirFile.exists())
 	   		{
 	   			planFileDirFile.mkdirs();
 	   		    GenericUtils.setFullPermission(planFileDir);
 	   		}
        	 
            factory = new StdSchedulerFactory();
            // Start the scheduler now
            org.quartz.Scheduler scheduler = factory.getDefaultScheduler();
            scheduler.getListenerManager().addJobListener(new MyJobListener());
            //scheduler.addGlobalJobListener(new MyJobListener());
            if (!factory.getScheduler().isStarted())
            {
                scheduler.start();
            }
            
            ScheduleAllJobs();
            logger.info("Storing QuartzScheduler Factory at" + QUARTZ_FACTORY_KEY);
            ctx.setAttribute(QUARTZ_FACTORY_KEY, factory);
         } catch (Exception ex) {
              logger.error("Quartz failed to initialize", ex);
         }
         
    }
    
    @SuppressWarnings("static-access")
	public static void ScheduleAllJobs() throws Exception
    {
//    	String folderName = "webapps"+FILE_SEPERATOR + "ice" + FILE_SEPERATOR + ICE_DBCONFIG_FOLDER_NAME;
//    	String filePath = folderName + FILE_SEPERATOR + ICE_DBCONFIG_FILE_NAME;
//    	File fName = new File(filePath);
//    	if (!fName.exists())
//    	{
//    		filePath = ".."+FILE_SEPERATOR+filePath;
//    	}
//    	
    	//Comment For Folder ReStructure 
    	/*String userDir = Constants.USER_DIR;
    	//System.out.println("system user dir = "+userDir);
    	if (userDir.endsWith("bin"))
    		userDir += File.separator+"..";
		userDir = userDir+FILE_SEPERATOR+"webapps"+FILE_SEPERATOR+"ice"+ FILE_SEPERATOR + ICE_DBCONFIG_FOLDER_NAME;
		if (!new File(userDir).exists())
			userDir = Constants.SERVICE_DIR+FILE_SEPERATOR+"webapps"+FILE_SEPERATOR+"ice"+ FILE_SEPERATOR + ICE_DBCONFIG_FOLDER_NAME;
		
		String filePath = userDir+FILE_SEPERATOR+ICE_DBCONFIG_FILE_NAME;*/
    	
    	//New Logic Based On Folder Restructure
    	String IcedqClient = System.getenv("ICEDQ_CONFIG");
		File tomcatDirFile = new File(IcedqClient);
		String appFolder =tomcatDirFile +FILE_SEPERATOR+Constants.ICEDQ_CONFIGS+FILE_SEPERATOR+ICE_DBCONFIG_FOLDER_NAME;
		String filePath = appFolder+FILE_SEPERATOR+ICE_DBCONFIG_FILE_NAME;
		String ipAdd = "NA";
		try
		{
			ipAdd = Inet4Address.getLocalHost().getHostAddress();
		}catch(Exception e) 
		{
			ipAdd = "NA";
		}
		String hostName ="NA";
		try 
		{
			hostName = Inet4Address.getLocalHost().getHostName();
		}catch(Exception e)
		{
			hostName="NA";
		}
//		String	ipAdd = Inet4Address.getLocalHost().getHostAddress();
//		String  hostName = Inet4Address.getLocalHost().getHostName(); // removed spaces for file encoding issue
		//System.out.println("App Folder Name"+appFolder);
    	//End 
		
		//Comment For Folder ReStructure 
		/*String configDir = Constants.USER_DIR;
		if (configDir.endsWith("bin"))
			configDir += File.separator+"..";
		configDir = configDir+File.separator+"webapps"+File.separator+"ice";
		if (!new File(configDir).exists())
			configDir = Constants.SERVICE_DIR+Constants.FILE_SEPERATOR+"webapps"+Constants.FILE_SEPERATOR+"ice";*/
		
		//New Logic Based On Folder Restructure
		
		String configDir = System.getenv("ICEDQ_CONFIG");
    	Properties props = new CaseInsensitiveProperties(); // handled case-insensitive key issue
		try (FileInputStream FileInputStream = new FileInputStream(configDir + Constants.FILE_SEPERATOR
				+ Constants.ICEDQ_CONFIGS + Constants.FILE_SEPERATOR + "ice_server.properties")) 
		{
			props.load(FileInputStream);
		} 
		catch (FileNotFoundException e) 
		{
			System.out.println(e.getMessage());
		} 
		catch (IOException e) 
		{
			System.out.println(e.getMessage());
		} 
		catch (Exception e) 
		{
			System.out.println(e.getMessage());
		}
		//System.out.println(" ====================== server.primary = "+props.getProperty("server.primary", "false"));
		//String isPrimary = props.getProperty("server.primary", "false");
		String tempfolder = Constants.TEMP_DIR +"output/";
        com.ice.xml.dbconfig.DBConfig dbConfigObj = new com.ice.xml.dbconfig.DBConfig();
        dbConfigObj = (com.ice.xml.dbconfig.DBConfig)XMLUtils.transformXMLFileToObject(dbConfigObj,new File(filePath));
    	if (dbConfigObj == null)
    		return;
    	 AbortRuleRun arr =new  AbortRuleRun();
        ScheduleJobs scheduleJobs = new ScheduleJobs();
        arr.delTempFile(tempfolder);
        for (int i = 0; i < dbConfigObj.getDB().size(); i++)
        {
        	 com.ice.xml.dbconfig.DB db = (com.ice.xml.dbconfig.DB) dbConfigObj.getDB().get(i);
//            scheduleJobs.scheduleAllJobs(db, isPrimary);
        	arr.abortAllrule(db,ipAdd,hostName);//Added function to abort all rune which is in running status when tomcat is down and Updated string  against hostname and ipaddress as to update rule status only for a specific host
            scheduleJobs.scheduleAllJobs(db);
        }
    }
    
    //Not required
//    @SuppressWarnings("static-access")
//	private static void updateRunningRules ()
//    {
//        try
//        {
//        	ICEContext iceContext = ICEContext.instance();
//        	HashMap<Long, Object> content = iceContext.getIceContext();
//        	Iterator<Long> itr = content.keySet().iterator();
//        	while (itr.hasNext())
//        	{
//        		long riid = itr.next();
//        		EngineInstance.getInstance().setStop(riid, true);
//        	}
//            Thread thread = new Thread();
//            try
//            {
//            	thread.sleep(5000);
//            } catch (Exception ee)
//            {
//            	ee.printStackTrace();
//            }
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//    }
    
    public static java.util.Date toUtilDate(String dateStr) {
	    Date date = null;
	    DateFormat formater;
	    try {
	      formater = new java.text.SimpleDateFormat("yyyy-MM-dd"); 
	      date = formater.parse(dateStr);
	    } catch (Exception e) {
	      e.printStackTrace();
	      try {
		formater = new java.text.SimpleDateFormat("MM/dd/yyyy"); 
		date = formater.parse(dateStr);
	      } catch (Exception e2) {
		e2.printStackTrace();
		//give up!
	      }
	    }
	    return date;
	  }
}

