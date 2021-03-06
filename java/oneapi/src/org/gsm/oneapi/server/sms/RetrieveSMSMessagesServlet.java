package org.gsm.oneapi.server.sms;

import java.io.IOException;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.gsm.oneapi.responsebean.sms.InboundSMSMessage;
import org.gsm.oneapi.responsebean.sms.InboundSMSMessageList;
import org.gsm.oneapi.server.OneAPIServlet;
import org.gsm.oneapi.server.ValidationRule;

/**
 * Servlet implementing the OneAPI function for retrieving received SMS messages
 */
public class RetrieveSMSMessagesServlet extends OneAPIServlet {

	private static final long serialVersionUID = 2849235677506318772L;
	
	public static final String DELIVERYIMPOSSIBLE="DeliveryImpossible";
	public static final String DELIVEREDTONETWORK="DeliveredToNetwork";
	public static final String DELIVEREDTOTERMINAL="DeliveredToTerminal";
	public static final String DELIVERYUNCERTAIN="DeliveryUncertain";
	public static final String MESSAGEWAITING="MessageWaiting";
	

	static Logger logger=Logger.getLogger(RetrieveSMSMessagesServlet.class);

	public void init() throws ServletException {
		logger.debug("RetrieveSMSMessagesServlet initialised");
    }

	private final String[] validationRules={"1", "smsmessaging", "inbound", "registrations", "*", "messages"};
	
	public void doGet(HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException{
		dumpRequestDetails(request, logger);

		String[] requestParts=getRequestParts(request);
		
		if (validateRequest(request, response, requestParts, validationRules)) {
			
			/*
			 * Decode the service parameters - in this case it is an HTTP GET request 
			 */			
			String registrationId=requestParts[4];
			int maxBatchSize=parseInt(request.getParameter("maxBatchSize"));
			
			logger.debug("registrationId = "+registrationId);
			logger.debug("maxBatchSize = "+maxBatchSize);

			ValidationRule[] rules={
					new ValidationRule(ValidationRule.VALIDATION_TYPE_MANDATORY, "registrationId", registrationId),
					new ValidationRule(ValidationRule.VALIDATION_TYPE_OPTIONAL_INT_GE_ZERO, "maxBatchSize", Integer.valueOf(maxBatchSize)),
			};

			if (checkRequestParameters(response, rules)) {			
				InboundSMSMessageList inboundSMSMessageList=new InboundSMSMessageList();
				
				String resourceURL=getRequestHostnameAndContext(request)+request.getServletPath()+"/1/smsmessaging/inbound/registrations/"+urlEncode(registrationId)+"/messages"; 

				inboundSMSMessageList.setResourceURL(resourceURL);
				
				java.util.Date message1DT=makeUTCDateTime(2009, Calendar.NOVEMBER, 19, 12, 0, 0);
				String messageId1="msg1";
				String resourceURL1=getRequestHostnameAndContext(request)+request.getServletPath()+"/1/smsmessaging/inbound/registrations/"+urlEncode(registrationId)+"/messages/"+urlEncode(messageId1);
				InboundSMSMessage message1=new InboundSMSMessage(message1DT, "3456", messageId1, "Come on Barca!", resourceURL1, "+447825123456");
				
				java.util.Date message2DT=makeUTCDateTime(2009, Calendar.NOVEMBER, 19, 14, 30, 25);
				String messageId2="msg2";
				String resourceURL2=getRequestHostnameAndContext(request)+request.getServletPath()+"/1/smsmessaging/inbound/registrations/"+urlEncode(registrationId)+"/messages/"+urlEncode(messageId2);
				InboundSMSMessage message2=new InboundSMSMessage(message2DT, "3456", messageId2, "Great goal by Messi", resourceURL2, "+447825789123");
				
				if (maxBatchSize==1) {
					InboundSMSMessage[] messages={message1};
					inboundSMSMessageList.setNumberOfMessagesInThisBatch(1);
					inboundSMSMessageList.setInboundSMSMessage(messages);
				} else {
					InboundSMSMessage[] messages={message1, message2};
					inboundSMSMessageList.setNumberOfMessagesInThisBatch(2);
					inboundSMSMessageList.setInboundSMSMessage(messages);					
				}
				inboundSMSMessageList.setTotalNumberOfPendingMessages(2);
				
				ObjectMapper mapper=new ObjectMapper();			
				String jsonResponse="{\"inboundSMSMessageList\":"+mapper.writeValueAsString(inboundSMSMessageList)+"}";
	
				sendJSONResponse(response, jsonResponse, OK, null);
			}			
		}
	}


}
