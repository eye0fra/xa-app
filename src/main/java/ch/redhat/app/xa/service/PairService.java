package ch.redhat.app.xa.service;


import ch.redhat.app.xa.entity.Pair;

import java.util.List;

public interface PairService {

    Pair find(String key);

    void set(Pair pair);

    void setWithRollback(Pair pair);

    List<Pair> findAll();

    Pair delete(String key);

    Integer deleteAll();
}
