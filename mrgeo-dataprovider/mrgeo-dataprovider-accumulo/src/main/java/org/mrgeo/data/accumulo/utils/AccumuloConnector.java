/*
 * Copyright 2009-2015 DigitalGlobe, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.mrgeo.data.accumulo.utils;

import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.client.mock.MockInstance;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.apache.commons.codec.binary.Base64;
import org.mrgeo.core.MrGeoConstants;
import org.mrgeo.data.DataProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class AccumuloConnector {

	private static Logger log = LoggerFactory
			.getLogger(AccumuloConnector.class);

	private static Connector conn = null;
	private static Properties connectionInfo = null;
	private static Properties accumuloProperties;

	public static void initialize() throws DataProviderException
	{
		if (connectionInfo == null)
		{
			connectionInfo = getAccumuloProperties();
			if (connectionInfo == null)
			{
				throw new DataProviderException("No Accumulo connection properties available");
			}
		}
	}

	public static String encodeAccumuloProperties(String r) throws DataProviderException {
		StringBuffer sb = new StringBuffer();
		// sb.append(MrGeoAccumuloConstants.MRGEO_ACC_ENCODED_PREFIX);
		Properties props = getAccumuloProperties();
		props.setProperty(MrGeoAccumuloConstants.MRGEO_ACC_KEY_RESOURCE, r);
		Enumeration<?> e = props.keys();
		while (e.hasMoreElements()) {
			String k = (String) e.nextElement();
			String v = props.getProperty(k);
			if (sb.length() > 0) {
				sb.append(MrGeoAccumuloConstants.MRGEO_ACC_ENCODED_DELIM);
			}
			sb.append(k + "=" + v);
		}
		byte[] enc = Base64.encodeBase64(sb.toString().getBytes());
		String retStr = new String(enc);
		retStr = MrGeoAccumuloConstants.MRGEO_ACC_ENCODED_PREFIX + retStr;

		return retStr;
	} // end encodeAccumuloProperties

	public static Properties decodeAccumuloProperties(String s) {
		Properties retProps = new Properties();

		if (!s.startsWith(MrGeoAccumuloConstants.MRGEO_ACC_ENCODED_PREFIX)) {
			return retProps;
		}
		String enc = s.replaceFirst(
						MrGeoAccumuloConstants.MRGEO_ACC_ENCODED_PREFIX, "");
		byte[] decBytes = Base64.decodeBase64(enc.getBytes());
		String dec = new String(decBytes);
		String[] pairs = dec
				.split(MrGeoAccumuloConstants.MRGEO_ACC_ENCODED_DELIM);
		for (String p : pairs) {
			String[] els = p.split("=");
			if (els.length == 1) {
				retProps.setProperty(els[0], "");
			} else {
				retProps.setProperty(els[0], els[1]);
			}
		}

		return retProps;
	} // end decodeAccumuloProperties

	public static boolean isEncoded(String s) {
		return s.startsWith(MrGeoAccumuloConstants.MRGEO_ACC_ENCODED_PREFIX);
	}

	// TODO: need to make sure the path is correctly set - think about MRGEO
	// environment variables
	public static String getAccumuloPropertiesLocation() {
		String conf = System.getenv(MrGeoConstants.MRGEO_ENV_HOME);
		if (conf == null) {
			conf = "/opt/mrgeo";
		}
		if (conf != null) {
			if (!conf.endsWith(File.separator)) {
				conf += File.separator;
			}
		} else {
			conf = "";
		}
		conf += "conf" + File.separator
				+ MrGeoAccumuloConstants.MRGEO_ACC_CONF_FILE_NAME;
		log.info("looking for conf at: " + conf);
		File f = new File(conf);
		if (!f.exists()) {
			return null;
		}
		return conf;
	}

	public static void setAccumuloProperties(Map<String, String> props)
	{
		log.warn("Called setAccumuloProperties");
		accumuloProperties = new Properties();
		for (String key : props.keySet())
		{
			log.warn("  " + key + " = " + props.get(key));
			String value = props.get(key);
			if (value != null)
			{
				accumuloProperties.put(key, value);
			}
		}
	}

	public static Properties getAccumuloProperties() throws DataProviderException
	{
		if (accumuloProperties == null)
		{
			if (checkMock())
			{
				accumuloProperties = new Properties();
				accumuloProperties.setProperty(MrGeoAccumuloConstants.MRGEO_ACC_KEY_INSTANCE,
																			 System.getProperty(MrGeoAccumuloConstants.MRGEO_ACC_KEY_INSTANCE));
				accumuloProperties.setProperty(MrGeoAccumuloConstants.MRGEO_ACC_KEY_USER,
																			 System.getProperty(MrGeoAccumuloConstants.MRGEO_ACC_KEY_USER));
				accumuloProperties.setProperty(MrGeoAccumuloConstants.MRGEO_ACC_KEY_PASSWORD,
																			 System.getProperty(MrGeoAccumuloConstants.MRGEO_ACC_KEY_PASSWORD));
				accumuloProperties.setProperty(MrGeoAccumuloConstants.MRGEO_ACC_KEY_AUTHS,
																			 MrGeoAccumuloConstants.MRGEO_ACC_AUTHS_U);
				return accumuloProperties;
			}

			// find the config file
			String conf = getAccumuloPropertiesLocation();
			if (conf != null)
			{
				File f = new File(conf);
				if (!f.exists())
				{
					throw new DataProviderException("Unable to get accumulo connection properties");
				}
				log.debug("using " + f.getAbsolutePath());

				accumuloProperties = new Properties();
				try
				{
					log.warn("Loading accumulo properties from " + f.getAbsolutePath());
					accumuloProperties.load(new FileInputStream(f));
					log.warn("Successfully loaded properties");
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new DataProviderException(e);
				}
				return accumuloProperties;
			}
			throw new DataProviderException("Unable to determine accumulo connection properties location");
		}
		log.warn("In getAccumuloProperties, the properties are:");
		for (String key : accumuloProperties.stringPropertyNames())
		{
			log.warn("  " + key + " = " + accumuloProperties.getProperty(key));
		}
		return accumuloProperties;
	} // end getAccumuloProperties

	public static Map<String, String> getAccumuloPropertiesAsMap()
	{
		Map<String, String> result = null;
		try
		{
			result = new HashMap<String, String>();
			Properties props = AccumuloConnector.getAccumuloProperties();
			Set<String> keys = props.stringPropertyNames();
			for (String key : keys)
			{
				result.put(key, props.getProperty(key));
			}
		}
		catch (DataProviderException e)
		{
			e.printStackTrace();
		}
		return result;
	}

	public static Connector getConnector() throws DataProviderException {

		if (checkMock()) {
			return getMockConnector();
		}

		// find the config file
		try
		{
			Properties p = getAccumuloProperties();
			return getConnector(p);
		}
		catch(Exception e)
		{
			throw new DataProviderException(e);
		}
	} // end getConnector

	public static Connector getConnector(Properties p)
			throws DataProviderException {
		String pw = p
				.getProperty(MrGeoAccumuloConstants.MRGEO_ACC_KEY_PASSWORD);
		String pwenc = p
				.getProperty(MrGeoAccumuloConstants.MRGEO_ACC_KEY_PWENCODED64);
		if (pwenc != null) {
			if (pwenc.toLowerCase().equals("true")) {
				pw = new String(Base64.decodeBase64(pw.getBytes()));
			}
		}

		log.warn("Returning connector for " +
						 p.getProperty(MrGeoAccumuloConstants.MRGEO_ACC_KEY_INSTANCE) + ", " +
						 p.getProperty(MrGeoAccumuloConstants.MRGEO_ACC_KEY_ZOOKEEPERS) + ", " +
						 p.getProperty(MrGeoAccumuloConstants.MRGEO_ACC_KEY_USER) + ", " +
						 pw);
		Connector conn =  getConnector(
				p.getProperty(MrGeoAccumuloConstants.MRGEO_ACC_KEY_INSTANCE),
				p.getProperty(MrGeoAccumuloConstants.MRGEO_ACC_KEY_ZOOKEEPERS),
				p.getProperty(MrGeoAccumuloConstants.MRGEO_ACC_KEY_USER), pw);
		log.warn("Got connector successfully");
		return conn;
	} // end getConnector - File

	public static Connector getConnector(String instance, String zookeepers,
			String user, String pass) throws DataProviderException {

		if (conn != null) {
			return conn;
		}
		if (checkMock()) {
			return getMockConnector(instance, user, pass);
		}

		Instance inst = new ZooKeeperInstance(instance, zookeepers);
		try {
			conn = inst.getConnector(user, new PasswordToken(pass.getBytes()));
			return conn;
		} catch (Exception e) {
			e.printStackTrace();
			throw new DataProviderException("problem creating connector "
					+ e.getMessage());
		}
	} // end getConnector

	public static boolean checkMock() {
		if (System.getProperty("mock") != null) {
			return true;
		}

		return false;
	} // end checkMock

	public static Connector getMockConnector() throws DataProviderException {

		return getMockConnector(
				System.getProperty(MrGeoAccumuloConstants.MRGEO_ACC_KEY_INSTANCE),
				System.getProperty(MrGeoAccumuloConstants.MRGEO_ACC_KEY_USER),
				System.getProperty(MrGeoAccumuloConstants.MRGEO_ACC_KEY_PASSWORD));

	} // end getMockConnector

	/**
	 * For testing.
	 * 
	 * @param instance
	 * @param user
	 * @param pass
	 * @return
	 */
	public static Connector getMockConnector(String instance, String user,
			String pass) throws DataProviderException {
		Instance mock = new MockInstance(instance);
		Connector conn = null;
		try {
			conn = mock.getConnector(user, pass.getBytes());
		} catch (Exception e) {
			throw new DataProviderException(
					"problem creating mock connector - " + e.getMessage());
		}

		return conn;
	} // end getMockConnector

	public static String getReadAuthorizations(String curAuths) throws DataProviderException {

		// if the incoming string is valid - use that
		if (curAuths != null) {
			return curAuths;
		}

		String retStr = MrGeoAccumuloConstants.MRGEO_ACC_NOAUTHS;
		// get the properties of the system
		Properties props = AccumuloConnector.getAccumuloProperties();

		// look for items in the properties
		if (props != null
				&& props.containsKey(MrGeoAccumuloConstants.MRGEO_ACC_KEY_DEFAULT_READ_AUTHS)) {
			String a = props
					.getProperty(MrGeoAccumuloConstants.MRGEO_ACC_KEY_DEFAULT_READ_AUTHS);

			// check if the default read is "null"
			if (!a.equals("null")) {
				// ah - valid defaults
				retStr = a;
			}
		}

		return retStr;

	} // end getReadAuthorizations
	
	
	public static boolean deleteTable(String table) throws DataProviderException {
		ArrayList<String> ignore = AccumuloUtils.getIgnoreTables();
		try{
			Connector conn = AccumuloConnector.getConnector();
			conn.tableOperations().delete(table);
			return true;
		} catch(Exception e){
			e.printStackTrace();
		}
		return false;
	} // end deleteTable
	
	

	public static void main(String args[]) throws Exception {
		// String myEnc = AccumuloConnector.encodeAccumuloProperties("rrr");
		// System.out.println("Encoded: " + myEnc);
		// Properties props = AccumuloConnector.decodeAccumuloProperties(myEnc);
		// Enumeration<?> e = props.keys();
		// while(e.hasMoreElements()){
		// String k = (String)e.nextElement();
		// String v = props.getProperty(k);
		// System.out.println("\t" + k + "\t= " + v);
		// }

		// Connector c = AccumuloConnector.getMockConnector("accumulo", "root",
		// "");
		// Connector c2 = AccumuloConnector.getMockConnector("accumulo", "root",
		// "");
		// c.tableOperations().create("junk");
		// SortedSet<String> list = c.tableOperations().list();
		// for(String l : list){
		// System.out.println(l);
		// }
		// SortedSet<String> list2 = c2.tableOperations().list();
		// for(String l2 : list2){
		// System.out.println(l2);
		// }

	} // end main

} // end AccumuloConnector
