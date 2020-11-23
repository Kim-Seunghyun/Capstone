package practice.capston.security.urlmatcher;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Service;
import practice.capston.domain.entity.Resources;
import practice.capston.repository.ResourceRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Getter @Setter
@Service
@RequiredArgsConstructor
public class SecurityResourceService {
    private final ResourceRepository resourceRepository;

    public LinkedHashMap<RequestMatcher, List<ConfigAttribute>> getResourceList() {
        LinkedHashMap<RequestMatcher, List<ConfigAttribute>> result = new LinkedHashMap<>();
        List<Resources> resources = resourceRepository.findAll();
        resources.forEach(re -> {
            List<ConfigAttribute> configAttributes = new ArrayList<>();
            configAttributes.add(new SecurityConfig(re.getRole()));
            result.put(new AntPathRequestMatcher(re.getResourcePath()), configAttributes);
        });

        return result;
    }
}
