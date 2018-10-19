package com.example.image.detection.queue;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "images", path = "images")
public interface QueueMessageRepository extends CrudRepository<QueueMessage, Long> {

	// eg. http://localhost:9000/movies/search/findByTitle?title=Toy%20Story%20(1995)

	// see here for more methods
	// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods

//	List<Movie> findByTitle( @Param("title") String title );
//
//	List<Movie> findByTitleLike( @Param("title") String title );
//
//	List<Movie> findByTitleContaining( @Param("title") String title );
//
//	List<Movie> findByTitleStartingWith( @Param("title") String title );
//
//	@Query("select m from Movie m where m.title like '%199%'")
//	List<Movie> findNinetiesMovies();
//
//	@Query("select m from Movie m where m.title like '%198%'")
//	List<Movie> findEightiesMovies();


}