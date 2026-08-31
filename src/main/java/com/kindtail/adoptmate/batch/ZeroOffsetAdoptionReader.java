package com.kindtail.adoptmate.batch;

import com.kindtail.adoptmate.adoption.domain.Adoption;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.ItemStreamReader;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class ZeroOffsetAdoptionReader implements ItemStreamReader<Adoption> {

    private final EntityManagerFactory emf;
    private final int pageSize;
    private Long lastId = 0L;
    private final Queue<Adoption> buffer = new ArrayDeque<>();

    public ZeroOffsetAdoptionReader(EntityManagerFactory emf, int pageSize) {
        this.emf = emf;
        this.pageSize = pageSize;
    }

    @Override
    public Adoption read() {
        if (buffer.isEmpty()) {
            fetchNextChunk();
        }
        if (buffer.isEmpty()) {
            return null; // 끝에 도달하면 null 반환하여 Step 종료
        }
        Adoption item = buffer.poll();
        if (item != null) {
            this.lastId = item.getId();
        }
        return item;
    }

    private void fetchNextChunk() {
        EntityManager em = emf.createEntityManager();
        try {
            List<Adoption> results = em.createQuery(
                            "SELECT a FROM Adoption a WHERE a.id > :lastId ORDER BY a.id ASC", Adoption.class)
                    .setParameter("lastId", lastId)
                    .setMaxResults(pageSize)
                    .getResultList();
            buffer.addAll(results);
        } finally {
            em.close();
        }
    }
}
