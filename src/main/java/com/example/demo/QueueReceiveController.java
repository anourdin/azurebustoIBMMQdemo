package com.example.demo;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

@Component
public class QueueReceiveController {
    @Autowired
    private JmsTemplate jmsTemplate;
    private static final String QUEUE_NAME = "ngrid_source";
    private static final String QUEUE_NAME_SEND = "ibmtargetq";
    
    private final Logger logger = LoggerFactory.getLogger(QueueReceiveController.class);

    @JmsListener(destination = QUEUE_NAME, containerFactory = "jmsListenerContainerFactory")
    public void receiveMessage(User user) {
        logger.info("Received message: {}", user.getName());
        sendDataToIbmMQ(user.getName());
    }
    
    public void sendMessageToAzure(String message)
    {
    	String connectionString = "sb://nationalgrid.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=PCFO/EnAexuUuR/a6HfIBNEtU9FV9u7Z4hlErdtm3+4=";
        ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .sender()
                .queueName(QUEUE_NAME_SEND)
                .buildClient();
    
        senderClient.sendMessage(new ServiceBusMessage(message));
        System.out.println("Sent a single message to the queue: " + QUEUE_NAME_SEND);        
    }
   

    public void sendDataToIbmMQ(String data) {
		 Connection connection = null;
		 Session session = null;
		 Destination destination = null;
		 MessageProducer producer = null;
		 try {

			 JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
			 JmsConnectionFactory cf = ff.createConnectionFactory();

				/*
				 * System.setProperty("javax.net.ssl.keyStore", "key.jks" );
				 * System.setProperty("javax.net.ssl.keyStorePassword","Mule12345" );
				 * System.setProperty("javax.net.ssl.trustStore", "key.jks");
				 * System.setProperty("javax.net.ssl.trustStorePassword", "Mule12345");
				 * cf.setStringProperty(WMQConstants.WMQ_SSL_CIPHER_SPEC,
				 * "TLS_RSA_WITH_AES_128_CBC_SHA256");
				 * cf.setStringProperty(WMQConstants.WMQ_CHANNEL, "TEST.CONN.SVRCONN");
				 * cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE,
				 * WMQConstants.WMQ_CM_CLIENT); cf.setStringProperty(WMQConstants.WMQ_HOST_NAME,
				 * "ngridqm-9f57.qm.us-south.mq.appdomain.cloud");
				 * cf.setIntProperty(WMQConstants.WMQ_PORT, 32545);
				 * cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, "ngridqm");
				 * cf.setStringProperty(WMQConstants.USERID, "katta");
				 * cf.setStringProperty(WMQConstants.PASSWORD,
				 * "CbU1SZna0_HjAWxj6CV18QQmIXDpXm1mkd-_3UweWvzr");
				 * 
				 * connection = cf.createConnection(); session = connection.createSession(false,
				 * Session.AUTO_ACKNOWLEDGE); destination =
				 * session.createQueue("queue:///demoinbound");
				 */

			  cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, "nationgrid-1de5.qm.au-syd.mq.appdomain.cloud"); 
			  cf.setIntProperty(WMQConstants.WMQ_PORT, 31272);
			  cf.setStringProperty(WMQConstants.WMQ_CHANNEL, "HASANCHANNEL");
			  cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
			  cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, "nationgrid");
			  cf.setStringProperty(WMQConstants.USERID, "alsherif");
			  cf.setStringProperty(WMQConstants.PASSWORD, "8UfIpOdS-bFrxlKh0KMiqicaVdfQjvYhr8wtT_brnaoa");
   			  cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
   			  
			  connection = cf.createConnection();
			  session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			  destination = session.createQueue("queue:///ibmtargetq");

			  producer = session.createProducer(destination);
			  TextMessage message = session.createTextMessage(data);
			  connection.start();
			  producer.send(message);

			  }catch (JMSException jmsex) {
				  jmsex.printStackTrace();
			  }
		 }

}