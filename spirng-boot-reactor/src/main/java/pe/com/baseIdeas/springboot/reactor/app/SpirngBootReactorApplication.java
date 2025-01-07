package pe.com.baseIdeas.springboot.reactor.app;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import pe.com.baseIdeas.springboot.reactor.app.models.Comentarios;
import pe.com.baseIdeas.springboot.reactor.app.models.Usuario;
import pe.com.baseIdeas.springboot.reactor.app.models.UsuarioComentarios;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpirngBootReactorApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(SpirngBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpirngBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// metodoSubscribe();
		// OperadorMapString();
		// OperadorMapClase();
		// OperadorFilter(); //Operador para eliminar elementos
		//ObservablesSonInmutables();
		//ObservableIterable(); // Lista o Iterables, Hashet
		//OperadorFlatMap(); //Operador para modificar el flujo
		//FluxListToFluxString();
		//CollectListFluxToMono(); //Operador collectList()
		//CombinarDosFlujosConFlatMap();
		//CombiarDosFlujosConZipWith();
		//CombiarDosFlujosConZipWith2(); //Otra
		//CombiarDosFlujosZipWithRangos();
		//ejemploIntervalos();  //ejecucion en otro hilo
		//ejemploDelaElements(); //ejecucion en otro hilo
		//ejemploIntervalInfinito();
		//ejemploIntervalDesdeCreate();
		//ejemploContrapresion();
		ejemploContrapresion2();//Operador limitRate
	}

	public static void ejemploContrapresion2 () {
		Flux.range(1, 10)
				.log()
				.limitRate(2)
				.subscribe();
	}
	public static void ejemploContrapresion () {
		Flux.range(1, 10)
				.log() //Para ver la traza completa del Flux
				.subscribe(new Subscriber<Integer>() {

					private Subscription s;

					private Integer limite = 5;
					private Integer consumido = 0;

					@Override
					public void onSubscribe(Subscription s) {
						this.s = s;
						//s.request(Long.MAX_VALUE);//numero maximo posible de elementos
						s.request(limite);
					}

					@Override
					public void onNext(Integer i) {
						log.info(i.toString());
						consumido++;
						if (consumido == limite){
							consumido = 0;
							s.request(limite);
						}
					}

					@Override
					public void onError(Throwable t) {

					}

					@Override
					public void onComplete() {
						//log.info("finalizo");
					}
				});
	}

	public static void ejemploIntervalDesdeCreate() {
		Flux.create(emitter -> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {

				private Integer contador = 0;

				@Override
				public void run() {
					emitter.next(++contador);
					if (contador == 10){
						timer.cancel();
						emitter.complete();
					}
					if (contador == 5){
						timer.cancel();
						emitter.error(new InterruptedException("Error, se ha detenido el Flux en 5."));
					}
				}
			}, 3000, 1000);
		})
				/*.doOnNext(next -> log.info(next.toString()))
				.doOnComplete(() -> log.info("Hemos terminado"))
				.subscribe();*/
				.subscribe(next -> log.info(next.toString()),
						err -> log.error(err.getMessage()),
						() -> log.info("Hemos terminado"));
	}
	public static void ejemploIntervalInfinito() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1); //comienza en 1

		Flux.interval(Duration.ofMillis(1000))
				.doOnTerminate(latch::countDown) //incrementa hasta legar a cero
				.flatMap(i -> {
					if(i>=5){
						return Flux.error(new InterruptedException("Solo hasta 5."));
					}
					return Flux.just(i);
				})
				.map(i -> "Hola " + i)
				.retry(2) //reinterara si es que falla
				.subscribe(s -> log.info(s), e -> log.error(e.getMessage()));

		//OTRA FORMA
		/*Flux.interval(Duration.ofMillis(1000))
				.doOnTerminate(() -> latch.countDown()) //incrementa hasta legar a cero
				.map(i -> "Hola " + i)
				.doOnNext(s -> log.info(s))
				.subscribe();*/

		latch.await();
	}
	public static void ejemploDelaElements() throws InterruptedException {
		Flux<Integer> rango = Flux.range(1, 12)
				.delayElements(Duration.ofMillis(1000))
				.doOnNext(e -> log.info(e.toString()));
		rango.subscribe();

		//esto no debemos hacer, solo lo haremos para ver la  data, ya que se ejecuta en otro hilo
		//rango.blockLast();
		//Thread.sleep(13000);
	}

	public static void ejemploIntervalos() {
		Flux<Integer> rango = Flux.range(1,12);
		Flux<Long> retraso = Flux.interval(Duration.ofMillis(1000));

		rango.zipWith(retraso, (ra, re) -> ra)
				.doOnNext(e -> log.info(e.toString()))
				.subscribe();
				//.blockLast(); //esto no debemos hacer, solo lo haremos para ver la  data, ya que se ejecuta en otro hilo
	}
	public static void CombiarDosFlujosZipWithRangos(){
		Flux<Integer> rangos = Flux.range(0, 4);
		Flux.just(1,2,3,4)
				.map(i -> (i*2))
				.zipWith(rangos, (data1, data2) -> String.format("Primer flux %d, Segundo Flux %d", data1, data2))
				//.zipWith(Flux.range(0, 4), (data1, data2) -> String.format("Primer flux %d, Segundo Flux %d", data1, data2))
				.subscribe(texto -> log.info(texto));
		log.info("------ OTRA FORMA -------");
		Flux.just(1,2,3,4)
				.map(i -> (i*2))
				.zipWith(rangos)
				.map(tuple -> {
					Integer data1 = tuple.getT1();
					Integer data2 = tuple.getT2();
					return String.format("Primer Flux %d, Segundo Flux %d",data1, data2);
				})
				.subscribe(texto -> log.info(texto));
	}
	public static void CombiarDosFlujosConZipWith2(){
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Daniel","Paredes"));

		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola que tal!");
			comentarios.addComentario("Este es comentario 2");
			comentarios.addComentario("Comentario tres");
			return  comentarios;
		});

		Mono<UsuarioComentarios> usuarioComentariosMono = usuarioMono
				.zipWith(comentariosUsuarioMono)
				.map(tuple -> {
					Usuario u = tuple.getT1();
					Comentarios c = tuple.getT2();
					return new UsuarioComentarios(u, c);
				});

		usuarioComentariosMono.subscribe(uc -> log.info(uc.toString()));
	}
	public static void CombiarDosFlujosConZipWith(){
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Daniel","Paredes"));

		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola que tal!");
			comentarios.addComentario("Este es comentario 2");
			comentarios.addComentario("Comentario tres");
			return  comentarios;
		});

		Mono<UsuarioComentarios> usuarioComentariosMono = usuarioMono
				.zipWith(comentariosUsuarioMono, (usuario, comentariosUsuario) -> new UsuarioComentarios(usuario, comentariosUsuario));

		usuarioComentariosMono.subscribe(uc -> log.info(uc.toString()));
	}
	public static Usuario CrearUsuario(){
		return new Usuario("Daniel", "Paredes");
	}
	public static void CombinarDosFlujosConFlatMap(){
		//Mono<Usuario> usuarioMono = Mono.fromCallable(() -> CrearUsuario());
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Daniel", "Paredes"));

		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola que tal!");
			comentarios.addComentario("Este es comentario 2");
			comentarios.addComentario("Comentario tres");
			return comentarios;
		});

		Mono<UsuarioComentarios> usuarioComentariosMono =  usuarioMono.flatMap(u -> comentariosUsuarioMono.map(c ->  new UsuarioComentarios(u,c)));
		usuarioComentariosMono.subscribe(uc -> log.info(uc.toString()));
		//comentariosMono.flatMap(c -> usuarioMono.map(u -> new UsuarioComentarios(u,c)))
		//		.subscribe(uc -> log.info(uc.toString()));
	}

	public static void CollectListFluxToMono() {
		List<Usuario> usuarioList = new ArrayList<>();
		usuarioList.add(new Usuario("Daniel", "Paredes"));
		usuarioList.add(new Usuario("Javier", "Perez"));
		usuarioList.add(new Usuario("Jose", "Lopez"));
		usuarioList.add(new Usuario("Daniel", "Muro"));
		usuarioList.add(new Usuario("Mauricio", "Fuimori"));

		Flux.fromIterable(usuarioList)
				.collectList()
				.subscribe(lista -> {
					log.info("=======Mostrar el Mono completo=======");
					log.info(lista.toString());
					log.info("=======Recorrer el Mono por cada item=======");
					lista.forEach(item -> log.info(item.toString()));
				});


	}

	public static void FluxListToFluxString() {
		List<Usuario> usuarioList = new ArrayList<>();
		usuarioList.add(new Usuario("Daniel", " Paredes"));
		usuarioList.add(new Usuario("Javier", "Perez"));
		usuarioList.add(new Usuario("Jose", "Lopez"));
		usuarioList.add(new Usuario("Daniel", "Muro"));
		usuarioList.add(new Usuario("Mauricio", "Fuimori"));
		
		Flux.fromIterable(usuarioList)
			.map(usuario -> usuario.getNombre().toUpperCase().concat(" ").concat(usuario.getApellido().toUpperCase()))
			.flatMap(nombre -> {
				if(nombre.contains("daniel".toUpperCase())) {
					return Mono.just(nombre);
				} else {
					return Mono.empty();
				}
			})
			//.map(nombre -> nombre.toLowerCase())
			.map(nombre -> {
				return nombre.toLowerCase();
			})
			.subscribe(e -> log.info(e.toString()));
	}
	
	public static void OperadorFlatMap() throws Exception {
		List<String> usuarioList = new ArrayList<>();
		usuarioList.add("Daniel Paredes");
		usuarioList.add("Javier Perez");
		usuarioList.add("Jose Lopez");
		usuarioList.add("Daniel Muro");
		usuarioList.add("Mauricio Fuimori");
		
		Flux.fromIterable(usuarioList)
			.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1]))
			.flatMap(usuario -> {
				if (usuario.getNombre().equalsIgnoreCase("daniel")) {
					return Mono.just(usuario);
				} else {
					/*usuario.setNombre("nombreDiferenteDanbiel");
					return Mono.just(usuario);*/
					return Mono.empty();
				}
			})
			.map(usuario -> {
				String nombre = usuario.getNombre().toLowerCase();
				usuario.setNombre(nombre);
				return usuario;
			})
			.subscribe(u -> log.info(u.toString()));
	}
	
	public static void ObservableIterable() throws Exception {
		List<String> usuarioList = new ArrayList<>();
		usuarioList.add("Daniel Paredes");
		usuarioList.add("Javier Perez");
		usuarioList.add("Jose Lopez");
		usuarioList.add("Daniel Muro");
		usuarioList.add("MAurido Fuimori");

		Flux<String> nombres = Flux.fromIterable(usuarioList);
		
		Flux<Usuario> usuarios = nombres
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.filter(usuario -> usuario.getNombre().equalsIgnoreCase("daniel")).doOnNext(usuario -> {
					if (usuario == null) {
						throw new RuntimeException("Nombres no pueden ser vacios");
					}
					System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
				}).map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				});

		usuarios.subscribe(e -> log.info(e.toString()), error -> log.error(error.getMessage()), new Runnable() {

			@Override
			public void run() {
				log.info("Ha finalizado la ejecucion del observable con exito.");
			}
		});

	}

	public static void ObservablesSonInmutables() throws Exception {
		Flux<String> nombres = Flux.just("Daniel Paredes", "Javier Perez", "Jose Lopez", "Daniel Muro",
				"MAurido Fuimori");

		/*Flux<Usuario> usuarios = nombres
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.filter(usuario -> usuario.getNombre().equalsIgnoreCase("daniel")).doOnNext(usuario -> {
					if (usuario == null) {
						throw new RuntimeException("Nombres no pueden ser vacios");
					}
					System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
				}).map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				});

		usuarios.subscribe(e -> log.info(e.toString()), error -> log.error(error.getMessage()), new Runnable() {

			@Override
			public void run() {
				log.info("Ha finalizado la ejecucion del observable con exito.");
			}
		});*/
		
		nombres
			.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
			.filter(usuario -> usuario.getNombre().equalsIgnoreCase("daniel"))
			.doOnNext(usuario -> {
				if(usuario == null) {
					throw new RuntimeException("Nombres no pueden ser vacios");
				}
				System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
			})
			.map(usuario -> { 
				String nombre = usuario.getNombre().toLowerCase();
				usuario.setNombre(nombre);
				return usuario;
			});
		 

		
		nombres.subscribe(e -> log.info(e.toString()), 
				error -> log.error(error.getMessage()),
				new Runnable() {
					@Override
					public void run() {
						log.info("Ha finalizado la ejecucion del observable con exito.");
					}
		});

	}

	public static void OperadorFilter() throws Exception {
		Flux<Usuario> nombres = Flux
				.just("Daniel Paredes", "Javier Perez", "Jose Lopez", "Daniel Muro", "MAurido Fuimori")
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.filter(usuario -> usuario.getNombre().equalsIgnoreCase("daniel")).doOnNext(usuario -> {
					if (usuario == null) {
						throw new RuntimeException("Nombres no pueden ser vacios");
					}
					System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
				}).map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				});
		nombres.subscribe(e -> log.info(e.toString()), error -> log.error(error.getMessage()), new Runnable() {

			@Override
			public void run() {
				log.info("Ha finalizado la ejecucion del observable con exito.");
			}
		});
	}

	public static void OperadorMapClase() throws Exception {
		Flux<Usuario> nombres = Flux.just("Daniel", "Javier", "Jose", "Junior", "Luis") // null
				.map(nombre -> new Usuario(nombre.toUpperCase(), null)).doOnNext(usuario -> {
					if (usuario == null) {
						throw new RuntimeException("Nombres no pueden ser vacios");
					}
					System.out.println(usuario.getNombre());
				}).map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				});

		nombres.subscribe(e -> log.info(e.toString()), error -> log.error(error.getMessage()), new Runnable() {

			@Override
			public void run() {
				log.info("Ha finalizado la ejecucion del observable con exito.");
			}
		});
	}

	public static void OperadorMapString() throws Exception {
		Flux<String> nombres = Flux.just("Daniel", "Javier", "Jose", "Junior", "Luis") // ""
				.map(nombre -> {
					return nombre.toUpperCase();
				}).doOnNext(e -> {
					if (e.isEmpty()) {
						throw new RuntimeException("Nombres no pueden estar vacios");
					}
					System.out.println(e);
				}).map(nombre -> {
					return nombre.toLowerCase();
				});

		nombres.subscribe(e -> log.info(e), // (log::info)
				error -> log.error(error.getMessage()), new Runnable() {

					@Override
					public void run() {
						log.info("Ha finalizado la ejecucion del observable con exito.");
					}
				});
	}

	public static void metodoSubscribe() throws Exception {
		Flux<String> nombres = Flux.just("Daniel", "Javier", "", "Junior", "Luis")
				.doOnNext(nombre -> System.out.println(nombre));
		// .doOnNext(System.out::println);
		nombres.subscribe(e -> log.info(e));
		// nombres.subscribe(log::info);
	}

}
