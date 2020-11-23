package practice.capston.security.urlmatcher;

import org.apache.coyote.Request;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class CustomFilterInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {
    private LinkedHashMap<RequestMatcher, List<ConfigAttribute>> requestMap = new LinkedHashMap<>();

    public CustomFilterInvocationSecurityMetadataSource(LinkedHashMap<RequestMatcher, List<ConfigAttribute>> requestMap) {
        this.requestMap = requestMap;
    }

    // 사용자의 요청 URL을 확인 한 후 해당 URL과 연관된 권한 값을 전달한다.
    // FilterInvocation은 Url를 얻기 위해서 필터에서 사용.
    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        HttpServletRequest request = ((FilterInvocation) object).getRequest();

        if(requestMap != null) {
            for(Map.Entry<RequestMatcher, List<ConfigAttribute>> entry : requestMap.entrySet()){
                RequestMatcher url = entry.getKey();
                if(url.matches(request)){
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    // 해당 Url과 관련된 모든 권한을 찾아서 반환해준다.
    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        Set<ConfigAttribute> attribute = new HashSet<>();
        Iterator iterator = this.requestMap.entrySet().iterator();

        while(iterator.hasNext()){
            Map.Entry<RequestMatcher, List<ConfigAttribute>> entry = (Map.Entry)iterator.next();
            attribute.addAll((Collection)entry.getValue());
        }
        return attribute;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }
}
