package pe.com.baseIdeas.springboot.webflux.app;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import pe.com.baseIdeas.springboot.webflux.app.models.documents.Categoria;
import pe.com.baseIdeas.springboot.webflux.app.models.documents.Producto;
import pe.com.baseIdeas.springboot.webflux.app.models.services.ProductoService;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootWebfluxApplication implements CommandLineRunner {

	@Autowired
	private ReactiveMongoTemplate reactiveMongoTemplate;
	
	@Autowired
	private ProductoService productoService;
	
	private static Logger logger = LoggerFactory.getLogger(SpringBootWebfluxApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		reactiveMongoTemplate.dropCollection("productos").subscribe();
		reactiveMongoTemplate.dropCollection("categorias").subscribe();
		
		Categoria electronico = new Categoria("Electrónico");
		Categoria mueble = new Categoria("Mueble");
		Categoria deporte = new Categoria("Deporte");
		Categoria computacion = new Categoria("Computación");

		Flux.just(electronico, mueble, deporte, computacion)
		.flatMap(productoService::saveCategoria)
		.doOnNext(c -> {
			logger.info("Categoria creada "+ c.getNombre() + ", Id: " + c.getId());
		})
		.thenMany(
				Flux.just(new Producto("TV Panasonic Pantalla LCD", 514.89, electronico),
						new Producto("Sony Camara HD Digital", 132.99, electronico),
						new Producto("Apple iPod", 54.79, electronico),
						new Producto("Melamine para TV", 876.59, mueble),
						new Producto("Comoda", 210.69, mueble),
						new Producto("Bicicleta Montanera", 374.39, deporte),
						new Producto("HP Impresora 345", 345.99, computacion),
						new Producto("Laptop Dell 763", 763.39, computacion),
						new Producto("Teclado logictec", 264.29, computacion))
				.flatMap(producto -> {
					producto.setCreateAt(new Date());
					return productoService.save(producto);
				})
		)
		.subscribe(producto -> logger.info("Insert" + producto.getId() + " " + producto.getNombre()));
	}

}
