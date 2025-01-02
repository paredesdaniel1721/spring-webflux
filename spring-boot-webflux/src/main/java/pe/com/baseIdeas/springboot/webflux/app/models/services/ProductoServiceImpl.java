package pe.com.baseIdeas.springboot.webflux.app.models.services;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pe.com.baseIdeas.springboot.webflux.app.models.dao.ProductoDao;
import pe.com.baseIdeas.springboot.webflux.app.models.documents.Producto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductoServiceImpl implements ProductoService {
    
	@Autowired
    private ProductoDao productoDao;
	
	@Override
	public Flux<Producto> findAll() {
		return productoDao.findAll();
	}

	@Override
	public Mono<Producto> findById(String id) {
		return productoDao.findById(id);
	}

	@Override
	public Mono<Producto> save(Producto producto) {
		return productoDao.save(producto);
	}

	@Override
	public Mono<Void> delete(Producto producto) {
		return productoDao.delete(producto);
	}

	@Override
	public Flux<Producto> findAllConNombreUpperCase() {
		return productoDao.findAll()
				.map(producto -> {
					producto.setNombre(producto.getNombre().toUpperCase());
					return producto;
				});
	}

	@Override
	public Flux<Producto> findAllConNombreUpperCaseRepeat() {
		return findAllConNombreUpperCase().repeat(5000);
	}

	@Override
	public Flux<Producto> findAllConNombreUpperCaseDelay() {
		//demorara en cargar todo cuando temrine por cad segundo los reistros a motrar, ejemplo si 4 elementos en 4 segundos mostrara toda la pagina
		return findAllConNombreUpperCase().delayElements(Duration.ofMillis(1000));
	}

}
