package telran.blocker;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import telran.blocker.dto.IpData;

@Component("authenticationErrorBrokerSender")
@Slf4j
public class AuthenticationErrorBrokerSender implements AuthenticationEntryPoint{

	@Autowired
	 StreamBridge streamBridge;
	@Value("${app.handler.binding.name}")
	 String bindingName;
	
	@Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		String IP = request.getRemoteAddr();
		if(IP.contains(".")) {
			log.debug("IP in format Ipv4: {}", IP);
			String[] partsIp = IP.split("[.]");
			IP = String.format("%s.%s.%s", partsIp[0], partsIp[1], partsIp[2]);
			log.debug("get IP: {}", IP);
		}
		String web = request.getRemoteHost();
		long timestamp = System.currentTimeMillis();		
		
		IpData ipData = new IpData(IP, web, timestamp);
		streamBridge.send(bindingName, ipData);
		log.debug("IP data: {} has been sent by binding name {}", ipData,bindingName);
		
		resolver.resolveException(request, response, null, authException);		
	}

}
