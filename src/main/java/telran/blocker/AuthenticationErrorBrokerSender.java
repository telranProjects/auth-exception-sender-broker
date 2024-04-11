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
		String web = request.getRemoteHost();
		long timestamp = System.currentTimeMillis();		
		
		IpData ipData = new IpData(IP, web, timestamp);
		streamBridge.send(bindingName, ipData);
		log.debug("IP data: {} has been sent by binding name {}", ipData,bindingName);
		log.trace("context path name : {}", request.getContextPath());
		log.trace("path info : {}", request.getPathInfo());
		
		resolver.resolveException(request, response, null, authException);		
	}

}
