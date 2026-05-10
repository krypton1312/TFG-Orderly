package com.yebur.backendorderly.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    @Query("""
            SELECT new com.yebur.backendorderly.client.ClientResponse(
                c.id, c.name, SIZE(c.orders))
            FROM Client c
            """)
    List<ClientResponse> findAllClientDTO();

    @Query("""
            SELECT new com.yebur.backendorderly.client.ClientResponse(
                c.id, c.name, SIZE(c.orders))
            FROM Client c WHERE c.id = :id
            """)
    Optional<ClientResponse> findClientDTOById(@Param("id") Long id);
}
