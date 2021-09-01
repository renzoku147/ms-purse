package redis.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//Cache centralizada Para las actualizaciones de cualquier cambio que se haga a la BD
//limpiar la cache para que se vaya poblndo los nuevos elementos segun sean necesarios.

@RestController
@RequestMapping("api/v1/caches")
public class CacheController {

	//Autowirear

	private CacheManager cacheManager;
	
	@Autowired
	public CacheController(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
	
	@PatchMapping("/{name}")
	public void evictCache(@PathVariable String name) {
		this.cacheManager.getCache(name).clear();
	}

}
