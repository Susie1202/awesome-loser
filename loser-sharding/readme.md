# 基于路由KEY的分库分表实现

application.properties示例

```properties
#配置多个数据源属性(第一个数据库)
spring.datasource.druid00username=root
spring.datasource.druid00passwrod=123456
spring.datasource.druid00jdbcUrl=jdbc:mysql://47.104.128.12:3306/tuling-multiDs00
spring.datasource.druid00driverClass=com.mysql.jdbc.Driver
#配置第二个数据源
spring.datasource.druid01username=root
spring.datasource.druid01passwrod=123456
spring.datasource.druid01jdbcUrl=jdbc:mysql://47.104.128.12:3306/tuling-multiDs01
spring.datasource.druid01driverClass=com.mysql.jdbc.Driver

#配置第三个数据源
spring.datasource.druid02username=root
spring.datasource.druid02passwrod=123456
spring.datasource.druid02jdbcUrl=jdbc:mysql://47.104.128.12:3306/tuling-multiDs02
spring.datasource.druid02driverClass=com.mysql.jdbc.Driver

mybatis.configuration.map-underscore-to-camel-case=true


#配置分表分库设置属性
#分三个数据库
tuling.dsroutingset.dataSourceNum=3
#每一个库分为5个相同的表结构
tuling.dsroutingset.tableNum=4
#指定路由的字段(必须指定)
tuling.dsroutingset.routingFiled=orderId
tuling.dsroutingset.tableSuffixStyle=%04d
tuling.dsroutingset.tableSuffixConnect=_
tuling.dsroutingset.routingStategy=ROUTING_DS_TABLE_STATEGY
```

