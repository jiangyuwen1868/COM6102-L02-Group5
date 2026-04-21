package com.jyw.csp.configuration;

import java.util.Properties;

import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfiguration {

	/**
     * 自动识别使用的数据库类型
     * 在mapper.xml中databaseId的值就是跟这里对应，
     * 如果没有databaseId选择则说明该sql适用所有数据库
     * 
     *   <select id="isExist" resultType="Boolean" databaseId="mysql">
	 *      SELECT EXISTS(SELECT 1 FROM `${db}`.test_table WHERE table_id=#{tableId} LIMIT 1)
	 *   </select>
	 *
	 *   <select id="isExist" resultType="Boolean" databaseId="oracle">
	 *      SELECT COUNT(*) FROM ${db}."test_table " WHERE "table_id"=#{tableId}
	 *   </select>
     * 
     * Properties 的key表示数据产品名称productName，可以通过数据库连接来获取
     * 
     * Class.forName(driver);
     * Connection conn = (Connection) DrvierManager.getConnection(url,name,password);
     * DatabaseMetaData metaData = (DatabaseMetaData) conn.getMetaData();
     * 数据库产品名称 = metaData.getDatabaseProductName();
     * 
     * 
     * 配置文件配置如下：
     * database:
	 *	  type: oracle
	 *	
	 *	#mybatis设置
	 *	mybatis:
	 *	  mapper-locations: classpath*:mapper/*.xml
	 *	  configuration:
	 *	    database-id: ${database.type}
	 *	    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
     * */
	@Bean
	public DatabaseIdProvider getDatabaseIdProvider() {
		DatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();
		Properties properties = new Properties();
		properties.setProperty("Oracle","oracle");
        properties.setProperty("MySQL","mysql");
        properties.setProperty("DB2","db2");
        properties.setProperty("Derby","derby");
        properties.setProperty("H2","h2");
        properties.setProperty("HSQL","hsql");
        properties.setProperty("Informix","informix");
        properties.setProperty("MS-SQL","ms-sql");
        properties.setProperty("PostgreSQL","postgresql");
        properties.setProperty("Sybase","sybase");
        properties.setProperty("Hana","hana");
		databaseIdProvider.setProperties(properties);
		return databaseIdProvider;
	}
	
}
