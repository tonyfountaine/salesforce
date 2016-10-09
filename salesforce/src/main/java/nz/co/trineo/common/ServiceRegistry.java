package nz.co.trineo.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ServiceRegistry {
	private static final Map<String, Service> registry = new HashMap<>();

	private ServiceRegistry() {
		// final ServiceLoader<ConnectedService> loader = ServiceLoader.load(ConnectedService.class);
		// loader.forEach(t -> {
		// registerService(t);
		// });
	}

	public static void registerService(final Service service) {
		registry.put(service.getName().toLowerCase(), service);
	}

	public static Set<String> listRegistedServices() {
		return registry.keySet();
	}

	public static Service getService(final String name) {
		return registry.get(name.toLowerCase());
	}
}
