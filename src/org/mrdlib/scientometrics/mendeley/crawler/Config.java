package org.mrdlib.scientometrics.mendeley.crawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.mrdlib.api.manager.Constants;
/**
 * 
 * @author Millah
 * 
 * This class reads and write to the mendeley configuration from a property file, which is defined in constants property file.
 */
public class Config {
	private int batchSize;
	private int lastSuccessfullId;
	private Date timestampOfLastSuccessfulId;
	private Constants constants;
	private String pathOfDownload;
	
	public Config() {
		constants = new Constants();
		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = getClass().getClassLoader().getResourceAsStream(constants.getMendeleyConfigPath());
			prop.load(input);
			
			this.batchSize = Integer.parseInt(prop.getProperty("batchSize"));
			this.lastSuccessfullId = Integer.parseInt(prop.getProperty("lastSuccessfullId"));
			DateFormat format = new SimpleDateFormat("yyyy.mm.dd HH:mm:ss");
			this.timestampOfLastSuccessfulId = format.parse(prop.getProperty("timestampOfLastSuccessfulId"));
			this.pathOfDownload = prop.getProperty("pathOfDownload");
			
		}  catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * this class writes the current process of the mendeley requests to the config file
	 * 
	 * @param lastSuccessfullId, which is the last successfull id which was asked for in mendeley
	 */
	public void writeMendeleyCrawlingProcessToConfigFile(int lastSuccessfullId) {
		//get the file
		Properties prop = new Properties();
        File file = new File(constants.getMendeleyConfigPath());

        //overwrite every parameter
		try {
            prop.setProperty("batchSize", this.batchSize+"");
            prop.setProperty("lastSuccessfullId", lastSuccessfullId+"");
            prop.setProperty("pathOfDownload", this.pathOfDownload);
            String timestamp = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new java.util.Date());
            prop.setProperty("timestampOfLastSuccessfulId", timestamp);

            //store it
            FileOutputStream fileOut = new FileOutputStream(file);
            prop.store(fileOut, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getPathOfDownload() {
		return pathOfDownload;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public int getLastSuccessfullId() {
		return lastSuccessfullId;
	}

	public Date getTimestampOfLastSuccessfulId() {
		return timestampOfLastSuccessfulId;
	}
	
	
}
