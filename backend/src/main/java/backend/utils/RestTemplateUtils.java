package backend.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class RestTemplateUtils {

	@Autowired
	private RestTemplate restTemplate;

	public <T> ResponseEntity<T> get(String url, Class<T> responseType) throws RestClientException {
		return restTemplate.getForEntity(url, responseType);
	}

	public <T> ResponseEntity<T> get(String url, Class<T> responseType, Object... uriVariables)
			throws RestClientException {
		return restTemplate.getForEntity(url, responseType, uriVariables);
	}

	public <T> ResponseEntity<T> get(String url, HttpHeaders headers, Class<T> responseType)
			throws RestClientException {
		HttpEntity<String> entity = new HttpEntity<>(headers);
		return restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
	}

	public <T> ResponseEntity<T> get(String url, HttpHeaders headers, Class<T> responseType,
			Object... uriVariables) throws RestClientException {
		HttpEntity<String> entity = new HttpEntity<>(headers);
		return restTemplate.exchange(url, HttpMethod.GET, entity, responseType, uriVariables);
	}

	public <T> ResponseEntity<T> post(String url, Class<T> responseType) throws RestClientException {
		return restTemplate.postForEntity(url, HttpEntity.EMPTY, responseType);
	}

	public <T> ResponseEntity<T> post(String url, Object requestBody, Class<T> responseType)
			throws RestClientException {
		return restTemplate.postForEntity(url, requestBody, responseType);
	}


    public <T> ResponseEntity<T> post(String url, Object requestBody, HttpHeaders headers, Class<T> responseType) throws RestClientException {
        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);
        return restTemplate.postForEntity(url, entity, responseType);
    }
    
    public <T> ResponseEntity<T> delete(String url, Class<T> responseType) throws RestClientException {
        return restTemplate.exchange(url, HttpMethod.DELETE, null, responseType);
    }

    public <T> ResponseEntity<T> delete(String url, HttpHeaders headers, Class<T> responseType) throws RestClientException {
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.DELETE, entity, responseType);
    }

    public <T> ResponseEntity<T> delete(String url, HttpHeaders headers, Class<T> responseType, Object... uriVariables) throws RestClientException {
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.DELETE, entity, responseType, uriVariables);
    }

}
