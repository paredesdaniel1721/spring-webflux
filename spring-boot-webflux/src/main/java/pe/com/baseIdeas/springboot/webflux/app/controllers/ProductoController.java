package pe.com.baseIdeas.springboot.webflux.app.controllers;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

import pe.com.baseIdeas.springboot.webflux.app.models.documents.Producto;
import pe.com.baseIdeas.springboot.webflux.app.models.services.ProductoService;
import reactor.core.publisher.Flux;

@Controller
public class ProductoController {

    private static final Logger logger = LoggerFactory.getLogger(ProductoController.class);
    
    @Autowired    
    private ProductoService productoService;

    @GetMapping({"/listar", "/"})
    public String listar (Model model) {
        Flux<Producto> productos = productoService.findAllConNombreUpperCase();

        productos.subscribe(prod -> logger.info(prod.getNombre()));
        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de productos");

        return "listar";
    }
    @GetMapping("/listar-chunked")
    public String listarChunked (Model model) {
        Flux<Producto> productos = productoService.findAllConNombreUpperCaseRepeat();

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de productos");

        return "listar-chunked";
    }

    @GetMapping("/listar-full")
    public String listarFull (Model model) {
        Flux<Producto> productos = productoService.findAllConNombreUpperCaseRepeat();

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de productos");

        return "listar";
    }
    @GetMapping("/listar-datadriver")
    public String listarDataDriver (Model model) {
		//demorara en cargar todo cuando temrine por cad segundo los reistros a motrar, ejemplo si 4 elementos en 4 segundos mostrara toda la pagina
        Flux<Producto> productos = productoService.findAllConNombreUpperCaseRepeat().delayElements(Duration.ofMillis(1000));
        
        productos.subscribe(prod -> logger.info(prod.getNombre()));
        //model.addAttribute("productos", productos);
        model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos,2));//mostrar 2 elemenetos cada 1000 milisegundos
        model.addAttribute("titulo", "Listado de productos");

        return "listar";
    }

}
