package nz.co.trineo.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ServiceRegistry {
	private final Map<String, ConnectedService> registry = new HashMap<>();

	public ServiceRegistry() {
//		final ServiceLoader<ConnectedService> loader = ServiceLoader.load(ConnectedService.class);
//		loader.forEach(t -> {
//			registerService(t);
//		});
	}

	public void registerService(final ConnectedService service) {
		registry.put(service.getName(), service);
	}

	public Set<String> listRegistedServices() {
		return registry.keySet();
	}
}
