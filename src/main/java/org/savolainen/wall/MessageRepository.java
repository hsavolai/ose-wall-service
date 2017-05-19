package org.savolainen.wall;

import org.springframework.data.repository.PagingAndSortingRepository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "message", path = "message")
public interface MessageRepository extends PagingAndSortingRepository<Message, Long> {
}
