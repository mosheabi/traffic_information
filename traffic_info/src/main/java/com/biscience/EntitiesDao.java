package com.biscience;

import com.biscience.model.PublisherTraffic;
import com.biscience.service.TrafficInfoService;
import com.google.common.collect.Maps;
import db.DbProperties;
import db.SqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import utils.HttpUtil;

import java.sql.*;

public class EntitiesDao {

	private static final String SELECT_COUNTRY_ISO = "SELECT ID,ISO_NUMERIC_CODE,PARENT_ID FROM DEV_ADC2_OPS.COUNTRIES;";

	// logger
	private static Logger log = Logger.getLogger(EntitiesDao.class);

	private static Connection getConnection() throws SQLException {
		try {
			Class.forName(DbProperties.DB_DRIVER_CLASS_MAME.getValue());
		} catch (ClassNotFoundException cnfe) {
			log.fatal("Could not load JDBC driver class " + DbProperties.DB_DRIVER_CLASS_MAME.getValue() + " , ", cnfe);
			System.exit(0);
		}
		return DriverManager.getConnection(DbProperties.DB_URL.getValue(), DbProperties.DB_USERNAME.getValue(), DbProperties.DB_PASSWORD.getValue());
	}



	public static void getRelevantPublishersForSW() {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			pstmt = con.prepareStatement(TrafficInfoProperties.QUERY_TO_RUN.getValue());
			rs = pstmt.executeQuery();
			// get list of countries
			while (rs.next()) {
				int countryId = rs.getInt("country_id");
				int entityId = rs.getInt("entity_id");
				String domain = HttpUtil.getUrlWithProtocol(rs.getString("domain"));
				int channel =  rs.getInt("channel");
				if(isValidData(countryId,entityId,domain.toLowerCase().trim()))
				{

						System.out.println("countryId :" +countryId + " entityId : " +entityId +" domain : "+domain+ " channel : "+channel );

						setDomainTrafficMap(domain, entityId, countryId,channel);

				}
				else{
					System.out.println("invalid data");
				}
			}
		} catch (SQLException sqle) {
			log.error("SQLException, ", sqle);
		} finally {
			SqlUtils.closeAll(con, pstmt, rs);
		}



	}

	private static void setDomainTrafficMap(String domain, int entityId, int countryId, int channel) {
		if(!TrafficInfoService.domainTrafficMap.containsKey(domain)){

			TrafficInfoService.domainTrafficMap.put(domain,new PublisherTraffic());
		}
		PublisherTraffic publisherTraffic = TrafficInfoService.domainTrafficMap.get(domain);
		publisherTraffic.setDomain(domain);
		publisherTraffic.setEntityId(entityId);
		if(!publisherTraffic.getCountryIdChanelStatusMap().containsKey(countryId)) {
			publisherTraffic.getCountryIdChanelStatusMap().put(countryId, Maps.newHashMap());
		}
		publisherTraffic.getCountryIdChanelStatusMap().get(countryId).put(channel,false);
	}




	private static boolean isValidData(int countryId, int entityId, String domain) {
		return countryId!=0 && entityId!=0 && !StringUtils.isEmpty(domain);
	}

	public static void getCountries() {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			pstmt = con.prepareStatement(SELECT_COUNTRY_ISO);
			rs = pstmt.executeQuery();
			// get list of countries
			while (rs.next()) {
				int countryId = rs.getInt("id");
				int isoCountryId = rs.getInt("ISO_NUMERIC_CODE");
				int parentId = rs.getInt("parent_id");
				TrafficInfoService.domainCountriesMap.put(countryId,isoCountryId);
				if(isoCountryId==0) {
					TrafficInfoService.domainParentMap.put(countryId, parentId);
				}
			}
		} catch (Exception e) {
			log.error("SQLException, ", e);

		} finally {
			SqlUtils.closeAll(con, pstmt, rs);
		}
	}
}
