Configuring the SWORD resource in FCRepo 3.5

In the web.xml:

  <servlet>
    <display-name>CXF SWORD Servlet</display-name>
    <servlet-name>CXFSwordServlet</servlet-name>
    <servlet-class>org.apache.cxf.transport.servlet.CXFServlet</servlet-class>
    <init-param>
      <param-name>config-location</param-name>
      <param-value>file:${fedora.home}/server/config/spring/web/jaxrs/sword-jaxrs.xml</param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
  </servlet>

  
  In the *new* file sword-jaxrs.xml:
  
  <?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
  xmlns:sec="http://www.springframework.org/schema/security"
  xmlns:context="http://www.springframework.org/schema/context"
  xmlns:tx="http://www.springframework.org/schema/tx" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:jaxrs="http://cxf.apache.org/jaxrs" xmlns:fedora-types-mtom="http://fedora-commons.org/2011/07/definitions/types/"
  xmlns:fedora-types="http://www.fedora.info/definitions/1/0/types/"
  xmlns:fedora-api-mtom="http://fedora-commons.org/2011/07/definitions/api/"
  xmlns:fedora-api="http://www.fedora.info/definitions/1/0/api/"
  xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/" xmlns:cxf="http://cxf.apache.org/core"
  xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd 
           http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd 
           http://cxf.apache.org/jaxrs http://cxf.apache.org/schemas/jaxrs.xsd 
           http://cxf.apache.org/core http://cxf.apache.org/schemas/core.xsd">

  <context:annotation-config />
  
  <import resource="classpath:META-INF/cxf/cxf.xml"/>
  <import resource="classpath:META-INF/cxf/cxf-servlet.xml" />
  
  <jaxrs:server serviceName="swordService" address="/">
    <jaxrs:serviceBeans>
      <bean class="edu.columbia.cul.sword.SwordResource">
        <constructor-arg ref="org.fcrepo.server.Server" />
      </bean>
    </jaxrs:serviceBeans>
  </jaxrs:server>
  
</beans>