package pe.com.baseIdeas.springboot.webflux.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import pe.com.baseIdeas.springboot.webflux.app.models.dao.ProductoDao;
import pe.com.baseIdeas.springboot.webflux.app.models.documents.Producto;
import reactor.core.publisher.Flux;

import java.util.Date;

@SpringBootApplication
public class SpringBootWebfluxApplication implements CommandLineRunner {

	@Autowired
	private ReactiveMongoTemplate reactiveMongoTemplate;
	@Autowired
	private ProductoDao productoDao;
	
	private static Logger logger = LoggerFactory.getLogger(SpringBootWebfluxApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		reactiveMongoTemplate.dropCollection("productos").subscribe();

		Flux.just(new Producto("TV Panasonic Pantalla LCD", 514.89),
				new Producto("Sony Camara HD Digital", 132.99),
				new Producto("Apple iPod", 54.79),
				new Producto("Sony Notebook", 876.59),
				new Producto("Dron Panasonic", 210.69),
				new Producto("Bicicleta Montanera", 374.39),
				new Producto("HP Impresora 345", 345.99),
				new Producto("Laptop Dell 763", 763.39),
				new Producto("Teclado logictec", 264.29))
		.flatMap(producto -> {
			producto.setCreateAt(new Date());
			return productoDao.save(producto);
		})
		.subscribe(producto -> logger.info("Insert" + producto.getId() + " " + producto.getNombre()));
	}

}
