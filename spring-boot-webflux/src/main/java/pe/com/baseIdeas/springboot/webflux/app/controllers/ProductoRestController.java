package pe.com.baseIdeas.springboot.webflux.app.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.com.baseIdeas.springboot.webflux.app.models.dao.ProductoDao;
import pe.com.baseIdeas.springboot.webflux.app.models.documents.Producto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/productos")
public class ProductoRestController {

    private static final Logger logger = LoggerFactory.getLogger(ProductoController.class);
    
    @Autowired
    private ProductoDao productoDao;
    
    @GetMapping()
    public Flux<Producto> index(){
    	Flux<Producto> productos = productoDao.findAll()
    			.doOnNext(prod -> logger.info(prod.getNombre()));
    	
    	return productos;
    }
    
    @GetMapping("/{id}")
    public Mono<Producto> show (@PathVariable String id) {
    	//Mono<Producto> producto = productoDao.findById(id);
    	
    	Flux<Producto> productos = productoDao.findAll();
    	
    	Mono<Producto> producto = productos.filter(p -> p.getId().equals(id))
    			.next() //next para que tome el primero
    			.doOnNext(prod -> logger.info(prod.getNombre()));
    	    	
    	return producto;
    }
	
}
